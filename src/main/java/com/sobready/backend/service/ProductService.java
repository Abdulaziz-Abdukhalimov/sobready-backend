package com.sobready.backend.service;

import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.*;
import com.sobready.backend.repository.ProductRepository;
import com.sobready.backend.repository.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
     * Also increments the view count (your React frontend tracks product views).
     */
    public Product getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Increment view count
        product.setProductView(product.getProductView() + 1);
        return productRepository.save(product);
    }
}
