package com.sobready.backend.repository;

import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.*;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications = dynamic WHERE clauses.
 *
 * In NestJS + TypeORM you'd write:
 *   const where: any = { productStatus: "ACTIVE" };
 *   if (productType) where.productType = productType;
 *   if (search) where.productName = Like(`%${search}%`);
 *
 * In Spring, each Specification is a reusable filter that can be combined with AND/OR.
 * Think of them as building blocks — you snap them together like Lego.
 */
public class ProductSpecification {

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("productStatus"), status);
    }

    public static Specification<Product> hasType(ProductType type) {
        return (root, query, cb) -> cb.equal(root.get("productType"), type);
    }

    public static Specification<Product> hasFragrance(ProductFragrance fragrance) {
        return (root, query, cb) -> cb.equal(root.get("productFragrance"), fragrance);
    }

    public static Specification<Product> hasGender(ProductGender gender) {
        return (root, query, cb) -> cb.equal(root.get("productGender"), gender);
    }

    /**
     * Search by name — case-insensitive LIKE query.
     * cb.lower() = SQL LOWER() function
     * cb.like()  = SQL LIKE operator
     * "%" + search + "%" = matches anywhere in the string
     *
     * SQL result: WHERE LOWER(product_name) LIKE LOWER('%search%')
     */
    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("productName")), "%" + search.toLowerCase() + "%");
    }
}
