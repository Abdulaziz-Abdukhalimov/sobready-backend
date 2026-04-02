package com.sobready.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sobready.backend.enums.MemberStatus;
import com.sobready.backend.enums.MemberType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * NestJS + TypeORM equivalent:
 *
 *   @Entity()
 *   export class Member {
 *     @PrimaryGeneratedColumn()
 *     _id: string;
 *     @Column({ unique: true })
 *     memberNick: string;
 *     @Column({ select: false })    ← hides password from queries
 *     memberPassword: string;
 *   }
 *
 * Key security detail:
 *   @JsonIgnore on memberPassword = NEVER send password in API responses
 *   This is like { select: false } in TypeORM or @Exclude() in class-transformer
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberType memberType = MemberType.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    /**
     * unique = true → no two members can have the same nickname
     * This creates a UNIQUE constraint in PostgreSQL
     */
    @Column(nullable = false, unique = true)
    private String memberNick;

    @Column(nullable = false)
    private String memberPhone;

    /**
     * @JsonIgnore = NEVER include this field in JSON responses
     * This is CRITICAL for security — you never want to send
     * the hashed password to the frontend.
     *
     * In NestJS: @Column({ select: false }) or @Exclude()
     */
    @JsonIgnore
    @Column(nullable = false)
    private String memberPassword;

    private String memberAddress;

    @Column(columnDefinition = "TEXT")
    private String memberDesc;

    private String memberImage;

    @Column(nullable = false)
    @Builder.Default
    private Integer memberPoints = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
