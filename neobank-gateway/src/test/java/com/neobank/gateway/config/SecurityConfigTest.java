package com.neobank.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SecurityConfig using JUnit 5.
 * Tests CORS configuration, CSRF settings, and security rules for WebFlux.
 */
@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    private ServerWebExchange createMockExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
    }

    @Nested
    @DisplayName("Allowed Origins Configuration")
    class AllowedOriginsConfigurationTests {

        @Test
        @DisplayName("Should have correct production allowed origins")
        void shouldHaveCorrectProductionAllowedOrigins() {
            List<String> expectedOrigins = List.of(
                "https://retail.neobank.com",
                "https://staff.neobank.com",
                "https://admin.neobank.com"
            );
            assertThat(expectedOrigins).hasSize(3);
            assertThat(expectedOrigins).containsExactlyInAnyOrder(
                "https://retail.neobank.com",
                "https://staff.neobank.com",
                "https://admin.neobank.com"
            );
        }

        @Test
        @DisplayName("Should have correct development allowed origins")
        void shouldHaveCorrectDevelopmentAllowedOrigins() {
            List<String> devOrigins = List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:5173",
                "http://localhost:5174"
            );
            assertThat(devOrigins).hasSize(5);
            assertThat(devOrigins).allMatch(origin -> origin.startsWith("http://localhost:"));
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Should allow correct HTTP methods")
        void shouldAllowCorrectHttpMethods() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            );
        }

        @Test
        @DisplayName("Should allow correct headers")
        void shouldAllowCorrectHeaders() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getAllowedHeaders()).containsExactlyInAnyOrder(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers", "X-XSRF-TOKEN"
            );
        }

        @Test
        @DisplayName("Should expose correct headers")
        void shouldExposeCorrectHeaders() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getExposedHeaders()).containsExactlyInAnyOrder(
                "Authorization", "X-Trace-Id", "X-Span-Id",
                "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset"
            );
        }

        @Test
        @DisplayName("Should allow credentials")
        void shouldAllowCredentials() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("Should have correct max age")
        void shouldHaveCorrectMaxAge() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getMaxAge()).isEqualTo(3600L);
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpointsTests {

        @Test
        @DisplayName("Should have correct public endpoints list")
        void shouldHaveCorrectPublicEndpointsList() {
            List<String> publicEndpoints = List.of(
                "/api/auth/login", "/api/auth/register", "/api/onboarding/register",
                "/actuator/health", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**"
            );
            assertThat(publicEndpoints).hasSize(7);
            assertThat(publicEndpoints).contains("/api/auth/login", "/actuator/health");
        }

        @Test
        @DisplayName("Should have correct CSRF ignored endpoints")
        void shouldHaveCorrectCsrfIgnoredEndpoints() {
            List<String> csrfIgnoredEndpoints = List.of(
                "/api/auth/**", "/api/onboarding/register", "/api/transfers/**",
                "/api/accounts/**", "/api/cards/**", "/api/loans/**"
            );
            assertThat(csrfIgnoredEndpoints).hasSize(6);
            assertThat(csrfIgnoredEndpoints).allMatch(endpoint -> endpoint.startsWith("/api/"));
        }
    }

    @Nested
    @DisplayName("Security Headers")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should have correct CSP policy directives")
        void shouldHaveCorrectCspPolicyDirectives() {
            String cspPolicy = "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
                "font-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'";
            assertThat(cspPolicy).contains("default-src 'self'");
            assertThat(cspPolicy).contains("frame-ancestors 'none'");
        }

        @Test
        @DisplayName("Should have correct permissions policy")
        void shouldHaveCorrectPermissionsPolicy() {
            String permissionsPolicy = "camera=(), microphone=(), geolocation=(), payment=()";
            assertThat(permissionsPolicy).contains("camera=()");
            assertThat(permissionsPolicy).contains("payment=()");
        }
    }

    @Nested
    @DisplayName("Development Environment Detection")
    class DevelopmentEnvironmentDetectionTests {

        @Test
        @DisplayName("Should detect dev profile as development")
        void shouldDetectDevProfileAsDevelopment() {
            System.setProperty("spring.profiles.active", "dev");
            try {
                SecurityConfig config = new SecurityConfig();
                CorsConfigurationSource source = config.corsConfigurationSource();
                CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
                assertThat(corsConfig.getAllowedOrigins()).hasSize(8);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should not detect prod profile as development")
        void shouldNotDetectProdProfileAsDevelopment() {
            System.setProperty("spring.profiles.active", "prod");
            try {
                SecurityConfig config = new SecurityConfig();
                CorsConfigurationSource source = config.corsConfigurationSource();
                CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
                assertThat(corsConfig.getAllowedOrigins()).hasSize(3);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should default to production when no profile set")
        void shouldDefaultToProductionWhenNoProfileSet() {
            SecurityConfig config = new SecurityConfig();
            CorsConfigurationSource source = config.corsConfigurationSource();
            CorsConfiguration corsConfig = source.getCorsConfiguration(createMockExchange());
            assertThat(corsConfig.getAllowedOrigins()).hasSize(3);
        }
    }
}
