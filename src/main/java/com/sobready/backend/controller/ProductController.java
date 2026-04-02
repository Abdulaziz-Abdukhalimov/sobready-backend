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

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        Product product = productService.getProduct(productId);
        return ResponseEntity.ok(product);
    }

    // ===================== ADMIN ENDPOINTS =====================

    /**
     * POST /product/create (ADMIN only)
     *
     * Now uses DTO — clean, one object instead of 10 parameters.
     *
     * NestJS equivalent:
     *   @Post('create')
     *   async create(@Body() dto: CreateProductDto, @UploadedFiles() files) { ... }
     *
     * Since we accept both JSON fields + file uploads, we use @RequestPart:
     *   - @RequestPart("data") = the JSON DTO
     *   - @RequestPart("productImages") = the uploaded files
     */
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal Member currentMember,
            @RequestPart("data") ProductCreateDto dto,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages
    ) throws IOException {

        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        Product product = productService.createProduct(dto, productImages);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /product/update (ADMIN only)
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateProduct(
            @AuthenticationPrincipal Member currentMember,
            @RequestPart("data") ProductUpdateDto dto,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages
    ) throws IOException {

        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        Product product = productService.updateProduct(dto, productImages);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /product/delete (ADMIN only)
     * Soft delete — sets status to INACTIVE.
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
