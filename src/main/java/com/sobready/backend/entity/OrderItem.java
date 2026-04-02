package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * One row in the order = one product + quantity.
 * Like a single line item on a receipt.
 *
 * NestJS + TypeORM equivalent:
 *
 *   @Entity()
 *   export class OrderItem {
 *     @ManyToOne(() => Order, order => order.orderItems)
 *     order: Order;
 *
 *     @ManyToOne(() => Product)
 *     product: Product;
 *   }
 *
 * @ManyToOne = "many OrderItems belong to one Order"
 *   Same concept in both frameworks.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(nullable = false)
    private Integer itemQuantity;

    @Column(nullable = false)
    private Integer itemPrice;

    /**
     * @ManyToOne = "many order items belong to one order"
     * @JoinColumn = the foreign key column in this table
     *
     * NestJS: @ManyToOne(() => Order, order => order.orderItems)
     *
     * @JsonIgnore = don't include the full order object in JSON
     *   (prevents infinite loop: order → items → order → items → ...)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    /**
     * We store just the product ID, not the full relationship.
     * The React frontend sends productId and we'll load product data separately.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
