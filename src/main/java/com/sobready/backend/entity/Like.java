package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A "like" = a member liking a product.
 * Toggle logic: if like exists → remove it, if not → create it.
 *
 * NestJS equivalent:
 *
 *   @Entity()
 *   @Unique(['memberId', 'likeRefId'])   ← one like per member per product
 *   export class Like {
 *     @Column() memberId: string;
 *     @Column() likeRefId: string;
 *   }
 *
 * @UniqueConstraint = prevents duplicate likes
 *   A member can only like a product once. Database enforces this.
 */
@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "like_ref_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * likeRefId = the ID of the product being liked.
     * Named "likeRefId" to match your React frontend's interface.
     */
    @Column(name = "like_ref_id", nullable = false)
    private Long likeRefId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
