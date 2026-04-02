package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sobready.backend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NestJS + TypeORM equivalent:
 *
 *   @Entity()
 *   export class Product {
 *     @PrimaryGeneratedColumn()
 *     _id: string;
 *     @Column()
 *     productName: string;
 *     ...
 *   }
 *
 * In Spring Boot + JPA, it's almost identical — just Java syntax.
 *
 * @Entity = marks this class as a database table
 * @Table  = customize the table name
 * @Getter/@Setter = Lombok auto-generates getters/setters (like TypeScript public fields)
 * @NoArgsConstructor = generates empty constructor (required by JPA)
 * @AllArgsConstructor = generates constructor with all fields
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /**
     * Your React frontend uses "_id" (MongoDB style).
     * We use Long (auto-increment) in PostgreSQL, but serialize it as "_id" in JSON.
     *
     * @Id = this is the primary key (like @PrimaryGeneratedColumn() in TypeORM)
     * @GeneratedValue(IDENTITY) = auto-increment (PostgreSQL handles the numbering)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("_id")  // Serialize as "_id" in JSON to match your React frontend
    private Long id;

    /**
     * @Enumerated(STRING) = store the enum as text in DB, not as a number.
     * So "ACTIVE" is stored as the string "ACTIVE", not as 0.
     * This matches your React enum: ProductStatus.ACTIVE = "ACTIVE"
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductFragrance productFragrance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productBrand;

    @Column(nullable = false)
    private Integer productPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer productStock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductGender productGender;

    @Column(nullable = false)
    @Builder.Default
    private Integer productSoldCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductVolume productVolume;

    @Column(columnDefinition = "TEXT")
    private String productDesc;

    /**
     * Product images stored as a list of file paths.
     * @ElementCollection = creates a separate table "product_images" to store the array.
     * This is how JPA handles arrays (since SQL doesn't have native array columns).
     *
     * In TypeORM you'd use: @Column("simple-array") productImages: string[]
     */
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    @Builder.Default
    private List<String> productImages = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Integer productView = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer productLikes = 0;

    /**
     * @CreationTimestamp = automatically set when the row is first created
     * @UpdateTimestamp = automatically updated on every save
     * Same as @CreateDateColumn() and @UpdateDateColumn() in TypeORM
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
