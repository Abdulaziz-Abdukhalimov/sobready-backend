package com.sobready.backend.config;

import com.sobready.backend.entity.Member;
import com.sobready.backend.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * This filter runs BEFORE every request reaches your controller.
 *
 * NestJS equivalent — AuthGuard:
 *
 *   @Injectable()
 *   export class JwtAuthGuard implements CanActivate {
 *     canActivate(context: ExecutionContext) {
 *       const token = context.getRequest().cookies['accessToken'];
 *       const user = this.jwtService.verify(token);
 *       context.getRequest().user = user;
 *       return true;
 *     }
 *   }
 *
 * Flow:
 * 1. Extract token from cookie OR Authorization header
 * 2. Verify the token
 * 3. Load the user from database
 * 4. Set the user in Spring Security context (like req.user in Express/NestJS)
 * 5. Continue to the controller
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long memberId = jwtUtil.getMemberIdFromToken(token);

            Member member = memberRepository.findById(memberId).orElse(null);

            if (member != null) {
                // Create authentication with user's role (USER or ADMIN)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                member,          // principal = the logged-in user (like req.user)
                                null,            // credentials (not needed, we already verified)
                                List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemberType().name()))
                        );

                // Set in SecurityContext — now any controller can access the current user
                // Like: req.user = member in Express/NestJS
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue to the next filter / controller
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from:
     * 1. Cookie named "accessToken" (used by most endpoints)
     * 2. Authorization header "Bearer xxx" (used by like endpoints)
     *
     * Your React app sends both depending on the endpoint.
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Try cookie first (your React uses withCredentials: true)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Try Authorization header (your React LikeService uses Bearer token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
