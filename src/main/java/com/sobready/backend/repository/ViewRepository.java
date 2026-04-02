package com.sobready.backend.repository;

import com.sobready.backend.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

    /**
     * Check if this member already viewed this product.
     * If true → don't increment view count again.
     */
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}
