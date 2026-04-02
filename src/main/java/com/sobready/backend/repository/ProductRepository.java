package com.sobready.backend.repository;

import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * NestJS + TypeORM equivalent:
 *
 *   @InjectRepository(Product)
 *   private productRepo: Repository<Product>;
 *   await this.productRepo.find({ where: { productStatus: "ACTIVE" } });
 *
 * In Spring Boot, you just DEFINE AN INTERFACE — no implementation needed!
 * Spring auto-generates all the SQL queries based on the method name.
 *
 * JpaRepository<Product, Long> means:
 *   - Product = the entity type
 *   - Long = the type of the primary key (id)
 *
 * JpaSpecificationExecutor = enables dynamic filtering (we'll use this for search/filter)
 *
 * You get these methods FOR FREE (no code needed):
 *   - findAll()          → SELECT * FROM products
 *   - findById(id)       → SELECT * FROM products WHERE id = ?
 *   - save(product)      → INSERT or UPDATE
 *   - deleteById(id)     → DELETE FROM products WHERE id = ?
 *   - count()            → SELECT COUNT(*) FROM products
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /**
     * Spring reads the method name and generates the query:
     * "findByProductStatus" → SELECT * FROM products WHERE product_status = ?
     *
     * The Pageable parameter adds LIMIT and OFFSET automatically.
     * This is like: .find({ where: { status }, take: limit, skip: offset })
     */
    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);
}
