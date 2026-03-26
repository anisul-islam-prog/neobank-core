package com.neobank.gateway.config;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CookieSecurityConfig using JUnit 5 and Mockito.
 * Tests secure cookie creation and management.
 */
@DisplayName("CookieSecurityConfig Unit Tests")
class CookieSecurityConfigTest {

    private CookieSecurityConfig cookieSecurityConfig;

    @BeforeEach
    void setUp() {
        cookieSecurityConfig = new CookieSecurityConfig();
        ReflectionTestUtils.setField(cookieSecurityConfig, "cookieMaxAge", 3600);
        ReflectionTestUtils.setField(cookieSecurityConfig, "cookieDomain", "neobank.com");
    }

    @Nested
    @DisplayName("JWT Cookie Creation")
    class JwtCookieCreationTests {

        @Test
        @DisplayName("Should create secure JWT cookie with correct attributes")
        void shouldCreateSecureJwtCookieWithCorrectAttributes() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getName()).isEqualTo(tokenName);
            assertThat(cookie.getValue()).isEqualTo(jwtToken);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getDomain()).isEqualTo("neobank.com");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        }

        @Test
        @DisplayName("Should create JWT cookie with HttpOnly flag")
        void shouldCreateJwtCookieWithHttpOnlyFlag() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("Should create JWT cookie with Secure flag")
        void shouldCreateJwtCookieWithSecureFlag() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.isSecure()).isTrue();
        }

        @Test
        @DisplayName("Should create JWT cookie with SameSite=Strict")
        void shouldCreateJwtCookieWithSamesiteStrict() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
        }

        @Test
        @DisplayName("Should create JWT cookie with correct path")
        void shouldCreateJwtCookieWithCorrectPath() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("Should create JWT cookie with correct domain")
        void shouldCreateJwtCookieWithCorrectDomain() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getDomain()).isEqualTo("neobank.com");
        }

        @Test
        @DisplayName("Should create JWT cookie with correct max age")
        void shouldCreateJwtCookieWithCorrectMaxAge() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "test.jwt.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        }
    }

    @Nested
    @DisplayName("Refresh Cookie Creation")
    class RefreshCookieCreationTests {

        @Test
        @DisplayName("Should create secure refresh cookie with correct attributes")
        void shouldCreateSecureRefreshCookieWithCorrectAttributes() {
            // Given
            String refreshToken = "refresh_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureRefreshCookie(refreshToken);

            // Then
            assertThat(cookie.getName()).isEqualTo("refresh_token");
            assertThat(cookie.getValue()).isEqualTo(refreshToken);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getDomain()).isEqualTo("neobank.com");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(604800); // 7 days
        }

        @Test
        @DisplayName("Should create refresh cookie with 7 days max age")
        void shouldCreateRefreshCookieWith7DaysMaxAge() {
            // Given
            String refreshToken = "refresh_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureRefreshCookie(refreshToken);

            // Then
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(7 * 24 * 60 * 60);
        }

        @Test
        @DisplayName("Should create refresh cookie with HttpOnly flag")
        void shouldCreateRefreshCookieWithHttpOnlyFlag() {
            // Given
            String refreshToken = "refresh_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureRefreshCookie(refreshToken);

            // Then
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("Should create refresh cookie with Secure flag")
        void shouldCreateRefreshCookieWithSecureFlag() {
            // Given
            String refreshToken = "refresh_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureRefreshCookie(refreshToken);

            // Then
            assertThat(cookie.isSecure()).isTrue();
        }
    }

    @Nested
    @DisplayName("CSRF Cookie Creation")
    class CsrfCookieCreationTests {

        @Test
        @DisplayName("Should create CSRF cookie with correct attributes")
        void shouldCreateCsrfCookieWithCorrectAttributes() {
            // Given
            String csrfToken = "csrf_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createCsrfCookie(csrfToken);

            // Then
            assertThat(cookie.getName()).isEqualTo("XSRF-TOKEN");
            assertThat(cookie.getValue()).isEqualTo(csrfToken);
            assertThat(cookie.isHttpOnly()).isFalse(); // JavaScript needs to read this
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        }

        @Test
        @DisplayName("Should create CSRF cookie without HttpOnly flag")
        void shouldCreateCsrfCookieWithoutHttpOnlyFlag() {
            // Given
            String csrfToken = "csrf_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createCsrfCookie(csrfToken);

            // Then
            assertThat(cookie.isHttpOnly()).isFalse();
        }

        @Test
        @DisplayName("Should create CSRF cookie with Secure flag")
        void shouldCreateCsrfCookieWithSecureFlag() {
            // Given
            String csrfToken = "csrf_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createCsrfCookie(csrfToken);

            // Then
            assertThat(cookie.isSecure()).isTrue();
        }

        @Test
        @DisplayName("Should create CSRF cookie with 1 hour max age")
        void shouldCreateCsrfCookieWith1HourMaxAge() {
            // Given
            String csrfToken = "csrf_token_value";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createCsrfCookie(csrfToken);

            // Then
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        }
    }

    @Nested
    @DisplayName("Cookie Deletion")
    class CookieDeletionTests {

        @Test
        @DisplayName("Should create delete cookie with max age 0")
        void shouldCreateDeleteCookieWithMaxAge0() {
            // Given
            String cookieName = "jwt_token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie(cookieName);

            // Then
            assertThat(cookie.getName()).isEqualTo(cookieName);
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create delete cookie with secure attributes")
        void shouldCreateDeleteCookieWithSecureAttributes() {
            // Given
            String cookieName = "jwt_token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie(cookieName);

            // Then
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("Should create delete cookie with empty value")
        void shouldCreateDeleteCookieWithEmptyValue() {
            // Given
            String cookieName = "jwt_token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie(cookieName);

            // Then
            assertThat(cookie.getValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Session Cookie Creation")
    class SessionCookieCreationTests {

        @Test
        @DisplayName("Should create session cookie with correct attributes")
        void shouldCreateSessionCookieWithCorrectAttributes() {
            // Given
            String sessionId = "session_12345";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSessionCookie(sessionId);

            // Then
            assertThat(cookie.getName()).isEqualTo("SESSION");
            assertThat(cookie.getValue()).isEqualTo(sessionId);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(1800); // 30 minutes
        }

        @Test
        @DisplayName("Should create session cookie with 30 minutes max age")
        void shouldCreateSessionCookieWith30MinutesMaxAge() {
            // Given
            String sessionId = "session_12345";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSessionCookie(sessionId);

            // Then
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(1800);
        }

        @Test
        @DisplayName("Should create session cookie with HttpOnly flag")
        void shouldCreateSessionCookieWithHttpOnlyFlag() {
            // Given
            String sessionId = "session_12345";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSessionCookie(sessionId);

            // Then
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("Should create session cookie with Secure flag")
        void shouldCreateSessionCookieWithSecureFlag() {
            // Given
            String sessionId = "session_12345";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSessionCookie(sessionId);

            // Then
            assertThat(cookie.isSecure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Add Cookie to Response")
    class AddCookieToResponseTests {

        @Test
        @DisplayName("Should add cookie to HTTP response")
        void shouldAddCookieToHttpResponse() {
            // Given
            HttpServletResponse response = mock(HttpServletResponse.class);
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token_value");

            // When
            cookieSecurityConfig.addCookieToResponse(response, cookie);

            // Then
            verify(response).addHeader("Set-Cookie", cookie.toString());
        }

        @Test
        @DisplayName("Should add multiple cookies to response")
        void shouldAddMultipleCookiesToResponse() {
            // Given
            HttpServletResponse response = mock(HttpServletResponse.class);
            ResponseCookie cookie1 = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token1");
            ResponseCookie cookie2 = cookieSecurityConfig.createSecureRefreshCookie("refresh1");

            // When
            cookieSecurityConfig.addCookieToResponse(response, cookie1);
            cookieSecurityConfig.addCookieToResponse(response, cookie2);

            // Then
            verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty JWT token")
        void shouldHandleEmptyJwtToken() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getName()).isEqualTo(tokenName);
            assertThat(cookie.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long JWT token")
        void shouldHandleLongJwtToken() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "a".repeat(1000);

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getValue()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle JWT token with special characters")
        void shouldHandleJwtTokenWithSpecialCharacters() {
            // Given
            String tokenName = "jwt_token";
            String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getValue()).isEqualTo(jwtToken);
        }

        @Test
        @DisplayName("Should handle custom cookie max age")
        void shouldHandleCustomCookieMaxAge() {
            // Given
            ReflectionTestUtils.setField(cookieSecurityConfig, "cookieMaxAge", 7200);
            String tokenName = "jwt_token";
            String jwtToken = "test.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(7200);
        }

        @Test
        @DisplayName("Should handle custom cookie domain")
        void shouldHandleCustomCookieDomain() {
            // Given
            ReflectionTestUtils.setField(cookieSecurityConfig, "cookieDomain", "custom.com");
            String tokenName = "jwt_token";
            String jwtToken = "test.token";

            // When
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie(tokenName, jwtToken);

            // Then
            assertThat(cookie.getDomain()).isEqualTo("custom.com");
        }

        @Test
        @DisplayName("Should handle null response gracefully")
        void shouldHandleNullResponseGracefully() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // When/Then - Should not throw exception
            cookieSecurityConfig.addCookieToResponse(null, cookie);
        }
    }

    @Nested
    @DisplayName("Cookie toString Format")
    class CookieToStringFormatTests {

        @Test
        @DisplayName("Should include Secure in cookie string")
        void shouldIncludeSecureInCookieString() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // Then
            assertThat(cookie.toString()).contains("Secure");
        }

        @Test
        @DisplayName("Should include HttpOnly in cookie string")
        void shouldIncludeHttpOnlyInCookieString() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // Then
            assertThat(cookie.toString()).contains("HttpOnly");
        }

        @Test
        @DisplayName("Should include SameSite in cookie string")
        void shouldIncludeSamesiteInCookieString() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // Then
            assertThat(cookie.toString()).contains("SameSite=Strict");
        }

        @Test
        @DisplayName("Should include Path in cookie string")
        void shouldIncludePathInCookieString() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // Then
            assertThat(cookie.toString()).contains("Path=/");
        }

        @Test
        @DisplayName("Should include Domain in cookie string")
        void shouldIncludeDomainInCookieString() {
            // Given
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");

            // Then
            assertThat(cookie.toString()).contains("Domain=neobank.com");
        }
    }
}
