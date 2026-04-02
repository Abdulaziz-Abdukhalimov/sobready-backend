package com.sobready.backend.service;

import com.sobready.backend.dto.ProductCreateDto;
import com.sobready.backend.dto.ProductUpdateDto;
import com.sobready.backend.entity.Product;
import com.sobready.backend.entity.View;
import com.sobready.backend.enums.*;
import com.sobready.backend.repository.ProductRepository;
import com.sobready.backend.repository.ProductSpecification;
import com.sobready.backend.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NestJS equivalent:
 *
 *   @Injectable()
 *   export class ProductService {
 *     constructor(
 *       @InjectRepository(Product)
 *       private productRepo: Repository<Product>,
 *     ) {}
 *   }
 *
 * @Service = tells Spring "this is a service class, manage it for me"
 *   Same as @Injectable() in NestJS — it registers the class in the DI container.
 *
 * @Autowired = "inject this dependency"
 *   Same as constructor injection in NestJS.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * Get products with filtering, sorting, and pagination.
     * Your React frontend calls: GET /product/all?order=X&page=1&limit=8&productType=X...
     */
    public Page<Product> getProducts(String order, int page, int limit,
                                     ProductType productType,
                                     ProductFragrance productFragrance,
                                     ProductGender productGender,
                                     String search) {

        // Build dynamic filter — start with "only ACTIVE products"
        Specification<Product> spec = Specification.where(
                ProductSpecification.hasStatus(ProductStatus.ACTIVE)
        );

        // Add filters only if provided (like: if (productType) where.productType = productType)
        if (productType != null) {
            spec = spec.and(ProductSpecification.hasType(productType));
        }
        if (productFragrance != null) {
            spec = spec.and(ProductSpecification.hasFragrance(productFragrance));
        }
        if (productGender != null) {
            spec = spec.and(ProductSpecification.hasGender(productGender));
        }
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.nameContains(search.trim()));
        }

        // Sorting — your React frontend sends "createdAt", "productPrice", "productLikes"
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // default: newest first
        if (order != null) {
            switch (order) {
                case "productPrice" -> sort = Sort.by(Sort.Direction.ASC, "productPrice");
                case "productLikes" -> sort = Sort.by(Sort.Direction.DESC, "productLikes");
                case "productView" -> sort = Sort.by(Sort.Direction.DESC, "productView");
                default -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }

        // page - 1 because Spring pages start at 0, but your frontend sends page starting at 1
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return productRepository.findAll(spec, pageable);
    }

    /**
     * Get a single product by ID.
     * Only increments view count if this member hasn't viewed it before.
     *
     * @param memberId null if user is not logged in (still show the product, just don't track view)
     */
    public Product getProduct(Long productId, Long memberId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Only count unique views from logged-in users
        if (memberId != null && !viewRepository.existsByMemberIdAndProductId(memberId, productId)) {
            View view = View.builder()
                    .memberId(memberId)
                    .productId(productId)
                    .build();
            viewRepository.save(view);

            product.setProductView(product.getProductView() + 1);
            product = productRepository.save(product);
        }

        return product;
    }

    // ===================== ADMIN ENDPOINTS =====================

    private final String uploadDir = "src/main/resources/static/uploads/";

    /**
     * Create a new product (ADMIN only).
     *
     * NestJS equivalent:
     *   @UseGuards(JwtAuthGuard, RolesGuard)
     *   @Roles('ADMIN')
     *   @Post('create')
     *   async createProduct(@Body() dto: CreateProductDto, @UploadedFiles() files) { ... }
     */
    public Product createProduct(ProductCreateDto dto, List<MultipartFile> images) throws IOException {

        // Build the product from DTO — clean and readable
        Product product = Product.builder()
                .productName(dto.getProductName())
                .productBrand(dto.getProductBrand())
                .productPrice(dto.getProductPrice())
                .productStock(dto.getProductStock() != null ? dto.getProductStock() : 0)
                .productType(dto.getProductType())
                .productFragrance(dto.getProductFragrance())
                .productGender(dto.getProductGender())
                .productVolume(dto.getProductVolume())
                .productDesc(dto.getProductDesc())
                .build();

        // Save images if provided
        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : images) {
                String path = saveImage(image);
                imagePaths.add(path);
            }
            product.setProductImages(imagePaths);
        }

        return productRepository.save(product);
    }

    /**
     * Update an existing product (ADMIN only).
     * Only updates fields that are provided (non-null).
     */
    public Product updateProduct(ProductUpdateDto dto, List<MultipartFile> images) throws IOException {

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update only provided fields — like PATCH in REST
        if (dto.getProductName() != null) product.setProductName(dto.getProductName());
        if (dto.getProductBrand() != null) product.setProductBrand(dto.getProductBrand());
        if (dto.getProductPrice() != null) product.setProductPrice(dto.getProductPrice());
        if (dto.getProductStock() != null) product.setProductStock(dto.getProductStock());
        if (dto.getProductType() != null) product.setProductType(dto.getProductType());
        if (dto.getProductFragrance() != null) product.setProductFragrance(dto.getProductFragrance());
        if (dto.getProductGender() != null) product.setProductGender(dto.getProductGender());
        if (dto.getProductVolume() != null) product.setProductVolume(dto.getProductVolume());
        if (dto.getProductDesc() != null) product.setProductDesc(dto.getProductDesc());
        if (dto.getProductStatus() != null) product.setProductStatus(dto.getProductStatus());

        // If new images are uploaded, replace old ones
        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : images) {
                String path = saveImage(image);
                imagePaths.add(path);
            }
            product.setProductImages(imagePaths);
        }

        return productRepository.save(product);
    }

    /**
     * Delete a product (ADMIN only).
     * We don't actually delete — we set status to INACTIVE (soft delete).
     * This preserves order history that references this product.
     */
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setProductStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    /**
     * Save uploaded image to disk. Returns relative path like "uploads/abc123.jpg"
     */
    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        return "uploads/" + newFilename;
    }
}
