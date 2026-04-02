package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks which member viewed which product.
 * Used to prevent duplicate view counts.
 *
 * UniqueConstraint on (member_id, product_id) =
 *   one member can only have ONE view record per product.
 */
@Entity
@Table(
        name = "views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
