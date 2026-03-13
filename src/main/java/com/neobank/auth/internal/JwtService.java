package com.neobank.auth.internal;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT service for token generation and validation.
 * Uses JJWT (Java JWT) library with HS256 signing.
 */
@Component
class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /**
     * Secret key for JWT signing (minimum 256 bits for HS256).
     */
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnlyMustBe32CharsOrMore}")
    private String secretKey;

    /**
     * Token expiration time in milliseconds (24 hours).
     */
    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    /**
     * Generate a JWT token for a user.
     *
     * @param userId the user ID
     * @param username the username
     * @return the JWT token
     */
    String generateToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("username", username);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username from token.
     *
     * @param token the JWT token
     * @return the username
     */
    String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    /**
     * Extract expiration date from token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validate token for a user.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if valid
     */
    boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired.
     *
     * @param token the JWT token
     * @return true if expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract a claim from the token.
     *
     * @param token the JWT token
     * @param claimsResolver the claim resolver function
     * @param <T> the claim type
     * @return the claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the signing key from the secret.
     *
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
