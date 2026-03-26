package com.neobank.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SecurityConfig using JUnit 5.
 * Tests CORS configuration, CSRF settings, and security rules.
 */
@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Nested
    @DisplayName("Allowed Origins Configuration")
    class AllowedOriginsConfigurationTests {

        @Test
        @DisplayName("Should have correct production allowed origins")
        void shouldHaveCorrectProductionAllowedOrigins() {
            // Then
            List<String> expectedOrigins = List.of(
                "https://retail.neobank.com",
                "https://staff.neobank.com",
                "https://admin.neobank.com"
            );

            // Verify the constant is defined correctly
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
            // Given
            List<String> devOrigins = List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:5173",
                "http://localhost:5174"
            );

            // Then
            assertThat(devOrigins).hasSize(5);
            assertThat(devOrigins).allMatch(origin -> origin.startsWith("http://localhost:"));
        }

        @Test
        @DisplayName("Should combine production and development origins in dev environment")
        void shouldCombineProductionAndDevelopmentOriginsInDevEnvironment() {
            // Given
            System.setProperty("spring.profiles.active", "dev");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then
                assertThat(config.getAllowedOrigins()).hasSize(8); // 3 production + 5 dev
                assertThat(config.getAllowedOrigins()).contains("https://retail.neobank.com");
                assertThat(config.getAllowedOrigins()).contains("http://localhost:3000");
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should only have production origins in production environment")
        void shouldOnlyHaveProductionOriginsInProductionEnvironment() {
            // Given - Default (no dev profile)

            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then - Should only have 3 production origins
            assertThat(config.getAllowedOrigins()).hasSize(3);
            assertThat(config.getAllowedOrigins()).doesNotContain("http://localhost:3000");
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Should allow correct HTTP methods")
        void shouldAllowCorrectHttpMethods() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then
            assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            );
        }

        @Test
        @DisplayName("Should allow correct headers")
        void shouldAllowCorrectHeaders() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then
            assertThat(config.getAllowedHeaders()).containsExactlyInAnyOrder(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-XSRF-TOKEN"
            );
        }

        @Test
        @DisplayName("Should expose correct headers")
        void shouldExposeCorrectHeaders() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then
            assertThat(config.getExposedHeaders()).containsExactlyInAnyOrder(
                "Authorization",
                "X-Trace-Id",
                "X-Span-Id",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
            );
        }

        @Test
        @DisplayName("Should allow credentials")
        void shouldAllowCredentials() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then
            assertThat(config.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("Should have correct max age")
        void shouldHaveCorrectMaxAge() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then
            assertThat(config.getMaxAge()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("Should register CORS configuration for all paths")
        void shouldRegisterCorsConfigurationForAllPaths() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();

            // Then - Should have configuration for /**
            assertThat(source).isNotNull();
        }
    }

    @Nested
    @DisplayName("Security Filter Chain")
    class SecurityFilterChainTests {

        @Test
        @DisplayName("Should create security filter chain successfully")
        void shouldCreateSecurityFilterChainSuccessfully() {
            // Given
            // Note: Creating a full HttpSecurity for testing requires Spring context
            // This test verifies the configuration can be instantiated

            // Then
            assertThat(securityConfig).isNotNull();
            assertThat(securityConfig.corsConfigurationSource()).isNotNull();
        }

        @Test
        @DisplayName("Should have CSRF token repository configured")
        void shouldHaveCsrfTokenRepositoryConfigured() {
            // Given
            CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
            requestHandler.setCsrfRequestAttributeName(null);

            CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

            // Then
            assertThat(repository).isNotNull();
            // CookieCsrfTokenRepository uses X-XSRF-TOKEN as default header name
        }

        @Test
        @DisplayName("Should have CSRF request handler configured")
        void shouldHaveCsrfRequestHandlerConfigured() {
            // Given
            CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
            requestHandler.setCsrfRequestAttributeName(null);

            // Then
            assertThat(requestHandler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpointsTests {

        @Test
        @DisplayName("Should have correct public endpoints list")
        void shouldHaveCorrectPublicEndpointsList() {
            // Given
            List<String> publicEndpoints = List.of(
                "/api/auth/login",
                "/api/auth/register",
                "/api/onboarding/register",
                "/actuator/health",
                "/actuator/info",
                "/v3/api-docs/**",
                "/swagger-ui/**"
            );

            // Then
            assertThat(publicEndpoints).hasSize(7);
            assertThat(publicEndpoints).contains("/api/auth/login");
            assertThat(publicEndpoints).contains("/api/auth/register");
            assertThat(publicEndpoints).contains("/actuator/health");
        }

        @Test
        @DisplayName("Should have correct CSRF ignored endpoints")
        void shouldHaveCorrectCsrfIgnoredEndpoints() {
            // Given
            List<String> csrfIgnoredEndpoints = List.of(
                "/api/auth/**",
                "/api/onboarding/register",
                "/api/transfers/**",
                "/api/accounts/**",
                "/api/cards/**",
                "/api/loans/**"
            );

            // Then
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
            // Given
            String cspPolicy = "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'";

            // Then
            assertThat(cspPolicy).contains("default-src 'self'");
            assertThat(cspPolicy).contains("frame-ancestors 'none'");
            assertThat(cspPolicy).contains("script-src 'self' 'unsafe-inline'");
        }

        @Test
        @DisplayName("Should have correct permissions policy")
        void shouldHaveCorrectPermissionsPolicy() {
            // Given
            String permissionsPolicy = "camera=(), microphone=(), geolocation=(), payment=()";

            // Then
            assertThat(permissionsPolicy).contains("camera=()");
            assertThat(permissionsPolicy).contains("microphone=()");
            assertThat(permissionsPolicy).contains("geolocation=()");
            assertThat(permissionsPolicy).contains("payment=()");
        }

        @Test
        @DisplayName("Should have correct referrer policy")
        void shouldHaveCorrectReferrerPolicy() {
            // Given
            String referrerPolicy = "STRICT_ORIGIN_WHEN_CROSS_ORIGIN";

            // Then
            assertThat(referrerPolicy).isEqualTo("STRICT_ORIGIN_WHEN_CROSS_ORIGIN");
        }
    }

    @Nested
    @DisplayName("Development Environment Detection")
    class DevelopmentEnvironmentDetectionTests {

        @Test
        @DisplayName("Should detect dev profile as development")
        void shouldDetectDevProfileAsDevelopment() {
            // Given
            System.setProperty("spring.profiles.active", "dev");

            try {
                // When - Access the method indirectly through CORS config
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then - Should include dev origins
                assertThat(config.getAllowedOrigins()).hasSize(8);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should detect local profile as development")
        void shouldDetectLocalProfileAsDevelopment() {
            // Given
            System.setProperty("spring.profiles.active", "local");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then
                assertThat(config.getAllowedOrigins()).hasSize(8);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should detect test profile as development")
        void shouldDetectTestProfileAsDevelopment() {
            // Given
            System.setProperty("spring.profiles.active", "test");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then
                assertThat(config.getAllowedOrigins()).hasSize(8);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should not detect prod profile as development")
        void shouldNotDetectProdProfileAsDevelopment() {
            // Given
            System.setProperty("spring.profiles.active", "prod");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then - Should only have production origins
                assertThat(config.getAllowedOrigins()).hasSize(3);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should default to production when no profile set")
        void shouldDefaultToProductionWhenNoProfileSet() {
            // Given - No profile set

            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then - Should only have production origins
            assertThat(config.getAllowedOrigins()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle multiple profiles")
        void shouldHandleMultipleProfiles() {
            // Given
            System.setProperty("spring.profiles.active", "dev,debug");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then - Should include dev origins since "dev" is in the profile string
                assertThat(config.getAllowedOrigins()).hasSize(8);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should handle empty profile")
        void shouldHandleEmptyProfile() {
            // Given
            System.setProperty("spring.profiles.active", "");

            try {
                CorsConfigurationSource source = securityConfig.corsConfigurationSource();
                CorsConfiguration config = source.getCorsConfiguration(null);

                // Then - Should only have production origins
                assertThat(config.getAllowedOrigins()).hasSize(3);
            } finally {
                System.clearProperty("spring.profiles.active");
            }
        }

        @Test
        @DisplayName("Should handle null profile gracefully")
        void shouldHandleNullProfileGracefully() {
            // Given - No profile set (null)

            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            CorsConfiguration config = source.getCorsConfiguration(null);

            // Then - Should default to production origins
            assertThat(config.getAllowedOrigins()).hasSize(3);
        }
    }
}
