package com.neobank.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CookieSecurityConfig using JUnit 5.
 * Tests secure cookie creation and management for WebFlux (Spring Boot 4).
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
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "test.jwt.token");
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("Should create JWT cookie with Secure flag")
        void shouldCreateJwtCookieWithSecureFlag() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "test.jwt.token");
            assertThat(cookie.isSecure()).isTrue();
        }

        @Test
        @DisplayName("Should create JWT cookie with SameSite=Strict")
        void shouldCreateJwtCookieWithSamesiteStrict() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "test.jwt.token");
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
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
            ResponseCookie cookie = cookieSecurityConfig.createSecureRefreshCookie("refresh_token_value");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(7 * 24 * 60 * 60);
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
            ResponseCookie cookie = cookieSecurityConfig.createCsrfCookie("csrf_token_value");
            assertThat(cookie.isHttpOnly()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cookie Deletion")
    class CookieDeletionTests {

        @Test
        @DisplayName("Should create delete cookie with max age 0")
        void shouldCreateDeleteCookieWithMaxAge0() {
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie("jwt_token");
            assertThat(cookie.getName()).isEqualTo("jwt_token");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create delete cookie with secure attributes")
        void shouldCreateDeleteCookieWithSecureAttributes() {
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie("jwt_token");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("Should create delete cookie with empty value")
        void shouldCreateDeleteCookieWithEmptyValue() {
            ResponseCookie cookie = cookieSecurityConfig.deleteCookie("jwt_token");
            assertThat(cookie.getValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Session Cookie Creation")
    class SessionCookieCreationTests {

        @Test
        @DisplayName("Should create session cookie with correct attributes")
        void shouldCreateSessionCookieWithCorrectAttributes() {
            ResponseCookie cookie = cookieSecurityConfig.createSessionCookie("session_12345");
            assertThat(cookie.getName()).isEqualTo("SESSION");
            assertThat(cookie.getValue()).isEqualTo("session_12345");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Strict");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(1800); // 30 minutes
        }
    }

    @Nested
    @DisplayName("Add Cookie to Response")
    class AddCookieToResponseTests {

        @Test
        @DisplayName("Should add cookie to reactive HTTP response")
        void shouldAddCookieToReactiveHttpResponse() {
            // Given
            ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/").build());
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token_value");

            // When
            cookieSecurityConfig.addCookieToResponse(exchange, cookie).block();

            // Then
            assertThat(exchange.getResponse().getCookies().getFirst("jwt_token")).isNotNull();
        }

        @Test
        @DisplayName("Should add multiple cookies to response")
        void shouldAddMultipleCookiesToResponse() {
            // Given
            ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/").build());
            ResponseCookie cookie1 = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token1");
            ResponseCookie cookie2 = cookieSecurityConfig.createSecureRefreshCookie("refresh1");

            // When
            cookieSecurityConfig.addCookieToResponse(exchange, cookie1).block();
            cookieSecurityConfig.addCookieToResponse(exchange, cookie2).block();

            // Then
            assertThat(exchange.getResponse().getCookies().getFirst("jwt_token")).isNotNull();
            assertThat(exchange.getResponse().getCookies().getFirst("refresh_token")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty JWT token")
        void shouldHandleEmptyJwtToken() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "");
            assertThat(cookie.getName()).isEqualTo("jwt_token");
            assertThat(cookie.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Should handle long JWT token")
        void shouldHandleLongJwtToken() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "a".repeat(1000));
            assertThat(cookie.getValue()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle custom cookie max age")
        void shouldHandleCustomCookieMaxAge() {
            ReflectionTestUtils.setField(cookieSecurityConfig, "cookieMaxAge", 7200);
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "test.token");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(7200);
        }

        @Test
        @DisplayName("Should handle custom cookie domain")
        void shouldHandleCustomCookieDomain() {
            ReflectionTestUtils.setField(cookieSecurityConfig, "cookieDomain", "custom.com");
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "test.token");
            assertThat(cookie.getDomain()).isEqualTo("custom.com");
        }
    }

    @Nested
    @DisplayName("Cookie toString Format")
    class CookieToStringFormatTests {

        @Test
        @DisplayName("Should include Secure in cookie string")
        void shouldIncludeSecureInCookieString() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");
            assertThat(cookie.toString()).contains("Secure");
        }

        @Test
        @DisplayName("Should include HttpOnly in cookie string")
        void shouldIncludeHttpOnlyInCookieString() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");
            assertThat(cookie.toString()).contains("HttpOnly");
        }

        @Test
        @DisplayName("Should include SameSite in cookie string")
        void shouldIncludeSamesiteInCookieString() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");
            assertThat(cookie.toString()).contains("SameSite=Strict");
        }

        @Test
        @DisplayName("Should include Path in cookie string")
        void shouldIncludePathInCookieString() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");
            assertThat(cookie.toString()).contains("Path=/");
        }

        @Test
        @DisplayName("Should include Domain in cookie string")
        void shouldIncludeDomainInCookieString() {
            ResponseCookie cookie = cookieSecurityConfig.createSecureJwtCookie("jwt_token", "token");
            assertThat(cookie.toString()).contains("Domain=neobank.com");
        }
    }
}
