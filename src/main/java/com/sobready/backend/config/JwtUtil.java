package com.sobready.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT = JSON Web Token — a signed "pass" that proves who you are.
 *
 * NestJS equivalent (using @nestjs/jwt):
 *
 *   @Injectable()
 *   export class JwtService {
 *     constructor(private jwt: JwtService) {}
 *
 *     generateToken(userId: string) {
 *       return this.jwt.sign({ id: userId }, { expiresIn: '24h' });
 *     }
 *
 *     verifyToken(token: string) {
 *       return this.jwt.verify(token);
 *     }
 *   }
 *
 * How JWT works:
 * 1. User logs in → backend creates a token containing { id: 5, nick: "john" }
 * 2. Token is signed with a SECRET key (only the server knows it)
 * 3. Token is sent to frontend (via cookie)
 * 4. Frontend sends token with every request
 * 5. Backend verifies the token signature → if valid, we know who the user is
 *
 * @Component = like @Injectable() in NestJS — Spring manages this class
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    /**
     * @Value = reads from application.properties
     * Like process.env.JWT_SECRET in NestJS
     */
    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * Create a JWT token for a user.
     * The token contains the user's ID and nickname — NOT the password!
     */
    public String generateToken(Long memberId, String memberNick) {
        return Jwts.builder()
                .subject(memberId.toString())       // Main claim: user ID
                .claim("memberNick", memberNick)    // Extra data in the token
                .issuedAt(new Date())               // When the token was created
                .expiration(new Date(System.currentTimeMillis() + expiration))  // When it expires
                .signWith(key)                      // Sign with our secret key
                .compact();                         // Build the token string
    }

    /**
     * Extract the member ID from a token.
     * If the token is invalid or expired, this throws an exception.
     */
    public Long getMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract the member nickname from a token.
     */
    public String getMemberNickFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("memberNick", String.class);
    }

    /**
     * Check if a token is valid (not expired, not tampered with).
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parse and verify the token. If someone tampered with it, this fails.
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)       // Verify with our secret key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
