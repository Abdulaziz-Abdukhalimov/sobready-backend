package com.sobready.backend.controller;

import com.sobready.backend.dto.ProductCreateDto;
import com.sobready.backend.dto.ProductUpdateDto;
import com.sobready.backend.entity.Member;
import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.*;
import com.sobready.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ===================== PUBLIC ENDPOINTS =====================

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductFragrance productFragrance,
            @RequestParam(required = false) ProductGender productGender,
            @RequestParam(required = false) String search
    ) {
        Page<Product> productPage = productService.getProducts(
                order, page, limit, productType, productFragrance, productGender, search
        );

        Map<String, Object> response = new HashMap<>();
        response.put("list", productPage.getContent());
        response.put("total", productPage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    /**
     * @AuthenticationPrincipal is nullable here — product page is public,
     * but if the user is logged in we track their view.
     * Guest users can still see the product, just no view tracking.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal Member currentMember
    ) {
        Long memberId = currentMember != null ? currentMember.getId() : null;
        Product product = productService.getProduct(productId, memberId);
        return ResponseEntity.ok(product);
    }

    // ===================== ADMIN ENDPOINTS =====================

    /**
     * POST /product/create (ADMIN only)
     *
     * Uses form-data because we need to upload files + send data together.
     * Controller receives individual fields → packs them into a DTO → passes to service.
     *
     * This is the standard pattern for multipart endpoints:
     *   Controller: receives raw form fields (Postman-friendly)
     *   Service: receives clean DTO (business logic stays clean)
     *
     * NestJS equivalent:
     *   @Post('create')
     *   @UseInterceptors(FilesInterceptor('productImages'))
     *   async create(
     *     @Body() body,
     *     @UploadedFiles() files: Express.Multer.File[]
     *   ) { ... }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam String productName,
            @RequestParam String productBrand,
            @RequestParam Integer productPrice,
            @RequestParam(defaultValue = "0") Integer productStock,
            @RequestParam ProductType productType,
            @RequestParam ProductFragrance productFragrance,
            @RequestParam ProductGender productGender,
            @RequestParam ProductVolume productVolume,
            @RequestParam(required = false) String productDesc,
            @RequestParam(required = false) List<MultipartFile> productImages
    ) throws IOException {

        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        // Pack form fields into DTO — keeps service layer clean
        ProductCreateDto dto = new ProductCreateDto();
        dto.setProductName(productName);
        dto.setProductBrand(productBrand);
        dto.setProductPrice(productPrice);
        dto.setProductStock(productStock);
        dto.setProductType(productType);
        dto.setProductFragrance(productFragrance);
        dto.setProductGender(productGender);
        dto.setProductVolume(productVolume);
        dto.setProductDesc(productDesc);

        Product product = productService.createProduct(dto, productImages);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /product/update (ADMIN only)
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateProduct(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productBrand,
            @RequestParam(required = false) Integer productPrice,
            @RequestParam(required = false) Integer productStock,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductFragrance productFragrance,
            @RequestParam(required = false) ProductGender productGender,
            @RequestParam(required = false) ProductVolume productVolume,
            @RequestParam(required = false) String productDesc,
            @RequestParam(required = false) ProductStatus productStatus,
            @RequestParam(required = false) List<MultipartFile> productImages
    ) throws IOException {

        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setProductId(productId);
        dto.setProductName(productName);
        dto.setProductBrand(productBrand);
        dto.setProductPrice(productPrice);
        dto.setProductStock(productStock);
        dto.setProductType(productType);
        dto.setProductFragrance(productFragrance);
        dto.setProductGender(productGender);
        dto.setProductVolume(productVolume);
        dto.setProductDesc(productDesc);
        dto.setProductStatus(productStatus);

        Product product = productService.updateProduct(dto, productImages);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /product/delete (ADMIN only)
     */
    @PostMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @AuthenticationPrincipal Member currentMember,
            @RequestBody Map<String, Long> input
    ) {
        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        productService.deleteProduct(input.get("productId"));
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }
}
