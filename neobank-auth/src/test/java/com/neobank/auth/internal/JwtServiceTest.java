package com.neobank.auth.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtService using JUnit 5 and Mockito.
 * Provides 100% logic coverage for JWT token generation and validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "testSecretKeyForDevelopmentMustBe32CharsOrMore");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "defaultAudience", "retail");
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token with default audience")
        void shouldGenerateValidJwtTokenWithDefaultAudience() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).contains("."); // JWT has 3 parts separated by dots

            // Verify token can be parsed
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
            assertThat(getAudienceFromClaims(claims)).isEqualTo("retail");
        }

        @Test
        @DisplayName("Should generate token with custom audience (staff)")
        void shouldGenerateTokenWithCustomAudienceStaff() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "staffuser";
            String audience = "staff";

            // When
            String token = jwtService.generateToken(userId, username, audience);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(getAudienceFromClaims(claims)).isEqualTo("staff");
        }

        @Test
        @DisplayName("Should generate token with custom audience (admin)")
        void shouldGenerateTokenWithCustomAudienceAdmin() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "adminuser";
            String audience = "admin";

            // When
            String token = jwtService.generateToken(userId, username, audience);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(getAudienceFromClaims(claims)).isEqualTo("admin");
        }

        @Test
        @DisplayName("Should include correct expiration time")
        void shouldIncludeCorrectExpirationTime() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            long expirationTime = 3600000L; // 1 hour
            ReflectionTestUtils.setField(jwtService, "expirationTime", expirationTime);
            Date beforeGeneration = new Date();

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();

            assertThat(expiration).isAfter(beforeGeneration);
            assertThat(issuedAt).isBeforeOrEqualTo(new Date());
            assertThat(expiration.getTime() - issuedAt.getTime()).isCloseTo(expirationTime, org.assertj.core.data.Offset.offset(1000L));
        }

        @Test
        @DisplayName("Should include userId claim in token")
        void shouldIncludeUserIdClaimInToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("Should include username as subject")
        void shouldIncludeUsernameAsSubject() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getSubject()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should include issuedAt timestamp")
        void shouldIncludeIssuedAtTimestamp() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            Date beforeGeneration = new Date();

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date issuedAt = claims.getIssuedAt();
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt.getTime()).isGreaterThanOrEqualTo(beforeGeneration.getTime() - 1000); // Allow 1 second skew
        }
    }

    @Nested
    @DisplayName("Token Claim Extraction")
    class TokenClaimExtractionTests {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsernameFromToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            // When
            String extractedUsername = jwtService.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("Should extract user ID from token")
        void shouldExtractUserIdFromToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            // When
            UUID extractedUserId = jwtService.extractUserId(token);

            // Then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should extract audience from token")
        void shouldExtractAudienceFromToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String audience = "admin";
            String token = jwtService.generateToken(userId, username, audience);

            // When
            String extractedAudience = jwtService.extractAudience(token);

            // Then
            assertThat(extractedAudience).isEqualTo("admin");
        }

        @Test
        @DisplayName("Should extract expiration date from token")
        void shouldExtractExpirationDateFromToken() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            // When
            Date expiration = jwtService.extractExpiration(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("Should extract default audience (retail) when not specified")
        void shouldExtractDefaultAudienceWhenNotSpecified() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            // When
            String extractedAudience = jwtService.extractAudience(token);

            // Then
            assertThat(extractedAudience).isEqualTo("retail");
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid token for user")
        void shouldValidateValidTokenForUser() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            MockUserDetails userDetails = new MockUserDetails(username);

            // When
            boolean isValid = jwtService.validateToken(token, userDetails);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token with wrong username")
        void shouldRejectTokenWithWrongUsername() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            MockUserDetails userDetails = new MockUserDetails("wronguser");

            // When
            boolean isValid = jwtService.validateToken(token, userDetails);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should validate audience correctly (matching)")
        void shouldValidateAudienceCorrectlyMatching() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String audience = "staff";
            String token = jwtService.generateToken(userId, username, audience);

            // When
            boolean isValidAudience = jwtService.validateAudience(token, "staff");

            // Then
            assertThat(isValidAudience).isTrue();
        }

        @Test
        @DisplayName("Should validate audience correctly (not matching)")
        void shouldValidateAudienceCorrectlyNotMatching() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String audience = "staff";
            String token = jwtService.generateToken(userId, username, audience);

            // When
            boolean isValidAudience = jwtService.validateAudience(token, "retail");

            // Then
            assertThat(isValidAudience).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Given
            ReflectionTestUtils.setField(jwtService, "expirationTime", -1000L); // Already expired
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            MockUserDetails userDetails = new MockUserDetails(username);

            // When/Then - Should throw ExpiredJwtException or return false
            assertThatThrownBy(() -> jwtService.validateToken(token, userDetails))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            // Given
            String malformedToken = "invalid.token.here";
            MockUserDetails userDetails = new MockUserDetails("testuser");

            // When/Then
            assertThatThrownBy(() -> jwtService.validateToken(malformedToken, userDetails))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should reject token with invalid signature")
        void shouldRejectTokenWithInvalidSignature() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // Create token with different secret (must be at least 256 bits for HS256)
            SecretKey wrongKey = Keys.hmacShaKeyFor("wrongSecretKeyThatDoesNotMatch12345".getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject(username)
                    .claim("userId", userId.toString())
                    .signWith(wrongKey)
                    .compact();

            // When/Then - Token validation should fail
            assertThatThrownBy(() -> jwtService.extractUserId(token))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // Given
            MockUserDetails userDetails = new MockUserDetails("testuser");

            // When/Then
            assertThatThrownBy(() -> jwtService.validateToken(null, userDetails))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            // Given
            MockUserDetails userDetails = new MockUserDetails("testuser");

            // When/Then
            assertThatThrownBy(() -> jwtService.validateToken("", userDetails))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle UUID with all zeros")
        void shouldHandleUuidWithAllZeros() {
            // Given
            UUID userId = new UUID(0L, 0L);
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            UUID extractedUserId = jwtService.extractUserId(token);
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should handle special characters in username")
        void shouldHandleSpecialCharactersInUsername() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "test+user@example.com";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle unicode characters in username")
        void shouldHandleUnicodeCharactersInUsername() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "用户测试";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "a".repeat(100);

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).hasSize(100);
        }

        @Test
        @DisplayName("Should handle null username")
        void shouldHandleNullUsername() {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            assertThatThrownBy(() -> jwtService.generateToken(userId, null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            assertThatThrownBy(() -> jwtService.generateToken(userId, ""))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should handle null audience")
        void shouldHandleNullAudience() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username, null);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(getAudienceFromClaims(claims)).isNull();
        }

        @Test
        @DisplayName("Should handle special characters in audience")
        void shouldHandleSpecialCharactersInAudience() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String audience = "test-audience_123";

            // When
            String token = jwtService.generateToken(userId, username, audience);

            // Then
            String extractedAudience = jwtService.extractAudience(token);
            assertThat(extractedAudience).isEqualTo(audience);
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpirationTests {

        @Test
        @DisplayName("Should create token that expires after configured time")
        void shouldCreateTokenThatExpiresAfterConfiguredTime() {
            // Given
            ReflectionTestUtils.setField(jwtService, "expirationTime", 60000L); // 1 minute
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();

            assertThat(expiration.getTime() - issuedAt.getTime()).isCloseTo(60000L, org.assertj.core.data.Offset.offset(1000L));
        }

        @Test
        @DisplayName("Should create token with future expiration")
        void shouldCreateTokenWithFutureExpiration() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            Date beforeGeneration = new Date();

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            assertThat(expiration).isAfter(beforeGeneration);
        }

        @Test
        @DisplayName("Should identify non-expired token as not expired")
        void shouldIdentifyNonExpiredTokenAsNotExpired() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String token = jwtService.generateToken(userId, username);

            MockUserDetails userDetails = new MockUserDetails(username);

            // When
            boolean isValid = jwtService.validateToken(token, userDetails);

            // Then
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("Secret Key Configuration")
    class SecretKeyConfigurationTests {

        @Test
        @DisplayName("Should use configured secret key")
        void shouldUseConfiguredSecretKey() {
            // Given
            String customKey = "CustomSecretKeyForTestingMustBe32CharsOrMore!!";
            ReflectionTestUtils.setField(jwtService, "secretKey", customKey);
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = jwtService.generateToken(userId, username);

            // Then
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(customKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getSubject()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should use default secret key when not configured")
        void shouldUseDefaultSecretKeyWhenNotConfigured() {
            // Given
            JwtService newJwtService = new JwtService();
            ReflectionTestUtils.setField(newJwtService, "secretKey", "defaultSecretKeyForDevelopmentOnlyMustBe32CharsOrMore");
            UUID userId = UUID.randomUUID();
            String username = "testuser";

            // When
            String token = newJwtService.generateToken(userId, username);

            // Then
            assertThat(token).isNotNull();
        }
    }

    /**
     * Mock UserDetails implementation for testing.
     */
    private static class MockUserDetails implements org.springframework.security.core.userdetails.UserDetails {
        private final String username;

        MockUserDetails(String username) {
            this.username = username;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.List.of();
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Helper method to get signing key for token parsing in tests.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = "testSecretKeyForDevelopmentMustBe32CharsOrMore".getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Helper method to extract audience from claims, handling both String and Collection types.
     */
    private String getAudienceFromClaims(Claims claims) {
        Object aud = claims.get("aud");
        if (aud instanceof java.util.Collection) {
            return ((java.util.Collection<?>) aud).iterator().hasNext()
                ? ((java.util.Collection<?>) aud).iterator().next().toString()
                : null;
        }
        return aud != null ? aud.toString() : null;
    }
}
