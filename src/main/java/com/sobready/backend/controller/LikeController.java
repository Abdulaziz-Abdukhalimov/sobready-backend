package com.sobready.backend.controller;

import com.sobready.backend.entity.Member;
import com.sobready.backend.entity.Product;
import com.sobready.backend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NestJS equivalent:
 *
 *   @Controller()
 *   @UseGuards(JwtAuthGuard)
 *   export class LikeController {
 *     @Post(':likeRefId/like')
 *     async toggleLike(@Req() req, @Param('likeRefId') id: string) { ... }
 *
 *     @Get('mylikes')
 *     async getMyLikes(@Req() req) { ... }
 *   }
 *
 * Your React LikeService uses Bearer token (Authorization header),
 * which our JwtAuthenticationFilter already handles.
 */
@RestController
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * POST /{likeRefId}/like — toggle like on a product
     *
     * Your React calls: POST `${serverApi}/${likeRefId}/like`
     * with Authorization: Bearer token
     */
    @PostMapping("/{likeRefId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal Member currentMember,
            @PathVariable Long likeRefId
    ) {
        boolean liked = likeService.toggleLike(currentMember.getId(), likeRefId);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "message", liked ? "Product liked" : "Product unliked"
        ));
    }

    /**
     * GET /mylikes — get all liked products
     *
     * Your React calls: GET `${serverApi}/mylikes`
     * with Authorization: Bearer token
     */
    @GetMapping("/mylikes")
    public ResponseEntity<List<Product>> getMyLikes(
            @AuthenticationPrincipal Member currentMember
    ) {
        List<Product> likedProducts = likeService.getMyLikes(currentMember.getId());
        return ResponseEntity.ok(likedProducts);
    }
}
