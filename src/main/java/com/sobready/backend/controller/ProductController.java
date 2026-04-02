package com.sobready.backend.controller;

import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.*;
import com.sobready.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * NestJS equivalent:
 *
 *   @Controller('product')
 *   export class ProductController {
 *     constructor(private productService: ProductService) {}
 *
 *     @Get('all')
 *     async getProducts(@Query() query: ProductInquiry) {
 *       return this.productService.getProducts(query);
 *     }
 *   }
 *
 * @RestController = @Controller + @ResponseBody
 *   Means: "this handles HTTP requests AND returns JSON automatically"
 *   In NestJS, @Controller() returns JSON by default — same idea.
 *
 * @RequestMapping("/product") = base URL path
 *   Same as @Controller('product') in NestJS
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * GET /product/all?order=X&page=1&limit=8&productType=X&productFragrance=X&productGender=X&search=X
     *
     * @RequestParam = @Query() in NestJS
     * (required = false) = optional parameter
     * (defaultValue = "1") = default if not provided
     */
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

        // Return format that your React frontend expects
        Map<String, Object> response = new HashMap<>();
        response.put("list", productPage.getContent());
        response.put("total", productPage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /product/{productId}
     *
     * @PathVariable = @Param('productId') in NestJS
     * Your React calls: GET /product/${productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        Product product = productService.getProduct(productId);
        return ResponseEntity.ok(product);
    }
}
