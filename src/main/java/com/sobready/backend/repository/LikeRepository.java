package com.sobready.backend.repository;

import com.sobready.backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * Find a specific like by member + product.
     * Used to check: "has this member already liked this product?"
     */
    Optional<Like> findByMemberIdAndLikeRefId(Long memberId, Long likeRefId);

    /**
     * Get all likes for a member.
     * Used for the "My Likes" / wishlist page.
     */
    List<Like> findByMemberId(Long memberId);
}
