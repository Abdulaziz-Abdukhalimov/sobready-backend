package com.sobready.backend.service;

import com.sobready.backend.entity.Like;
import com.sobready.backend.entity.Product;
import com.sobready.backend.repository.LikeRepository;
import com.sobready.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Toggle like — if already liked, remove it. If not, create it.
     *
     * NestJS equivalent:
     *   async toggleLike(memberId: string, likeRefId: string) {
     *     const existing = await this.likeRepo.findOne({ memberId, likeRefId });
     *     if (existing) {
     *       await this.likeRepo.remove(existing);
     *       product.productLikes--;
     *     } else {
     *       await this.likeRepo.save({ memberId, likeRefId });
     *       product.productLikes++;
     *     }
     *   }
     *
     * @Transactional because we update two tables (likes + products)
     */
    @Transactional
    public boolean toggleLike(Long memberId, Long likeRefId) {
        Optional<Like> existingLike = likeRepository.findByMemberIdAndLikeRefId(memberId, likeRefId);

        Product product = productRepository.findById(likeRefId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (existingLike.isPresent()) {
            // Already liked → remove (unlike)
            likeRepository.delete(existingLike.get());
            product.setProductLikes(Math.max(0, product.getProductLikes() - 1));
            productRepository.save(product);
            return false;  // liked = false (removed)
        } else {
            // Not liked → create (like)
            Like like = Like.builder()
                    .memberId(memberId)
                    .likeRefId(likeRefId)
                    .build();
            likeRepository.save(like);
            product.setProductLikes(product.getProductLikes() + 1);
            productRepository.save(product);
            return true;  // liked = true (added)
        }
    }

    /**
     * Get all products that a member has liked.
     * Your React frontend expects an array of product data (not just like records).
     */
    public List<Product> getMyLikes(Long memberId) {
        List<Like> likes = likeRepository.findByMemberId(memberId);

        List<Product> products = new ArrayList<>();
        for (Like like : likes) {
            productRepository.findById(like.getLikeRefId()).ifPresent(products::add);
        }
        return products;
    }
}
