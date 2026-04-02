package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sobready.backend.enums.OrderStatus;
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
 *   export class Order {
 *     @ManyToOne(() => Member)
 *     member: Member;
 *
 *     @OneToMany(() => OrderItem, item => item.order)
 *     orderItems: OrderItem[];
 *   }
 *
 * Key relationship:
 *   Order (1) ←→ (*) OrderItem
 *   "One order has many items"
 *
 * @OneToMany(cascade = ALL) = when you save/delete an order,
 *   automatically save/delete its items too.
 *   Like { cascade: true } in TypeORM.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(nullable = false)
    private Integer orderTotal;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderDelivery = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    /**
     * Which member placed this order.
     * We store just the ID — not a full @ManyToOne relationship.
     * Simpler and matches your React frontend's data shape.
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * @OneToMany = "one order has many order items"
     * cascade = ALL → saving the order also saves its items
     * orphanRemoval = true → removing an item from the list deletes it from DB
     *
     * NestJS: @OneToMany(() => OrderItem, item => item.order, { cascade: true })
     *
     * mappedBy = "order" → the OrderItem entity has the @ManyToOne("order") field
     *   that owns the relationship (has the foreign key).
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Not stored in DB — populated in the service layer.
     * Carries the product details for each order item.
     * Your React frontend expects: order.productsData = [Product, Product, ...]
     *
     * @Transient = "this field is NOT a database column"
     */
    @Transient
    private List<Product> productsData;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
