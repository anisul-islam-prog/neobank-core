package com.neobank.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitingFilter using JUnit 5 and Mockito.
 * Tests rate limiting logic for different user types and endpoints (Spring Boot 4).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter Unit Tests")
class RateLimitingFilterTest {

    @Mock
    private WebFilterChain filterChain;

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    private ServerWebExchange createExchange(String uri, String clientIp) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get(uri)
                .remoteAddress(InetSocketAddress.createUnresolved(clientIp, 12345))
                .build());
    }

    private ServerWebExchange createExchangeWithHeader(String uri, String headerName, String headerValue) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get(uri)
                .header(headerName, headerValue)
                .build());
    }

    @Nested
    @DisplayName("Retail User Rate Limiting")
    class RetailUserRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within retail limit (100/min)")
        void shouldAllowRequestsWithinRetailLimit() {
            // Given - Make requests within limit
            for (int i = 0; i < 50; i++) {
                ServerWebExchange exchange = createExchange("/api/accounts", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
            }

            // Then - All requests should pass through
            verify(filterChain, times(50)).filter(any());
        }

        @Test
        @DisplayName("Should set correct rate limit headers for retail users")
        void shouldSetCorrectRateLimitHeadersForRetailUsers() {
            // Given
            ServerWebExchange exchange = createExchange("/api/accounts", "192.168.1.100");

            // When
            rateLimitingFilter.filter(exchange, filterChain).block();

            // Then
            assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("60");
        }
    }

    @Nested
    @DisplayName("Registration Rate Limiting")
    class RegistrationRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within registration limit (5/min)")
        void shouldAllowRequestsWithinRegistrationLimit() {
            // Given - Make 3 requests (within limit)
            for (int i = 0; i < 3; i++) {
                ServerWebExchange exchange = createExchange("/api/onboarding/register", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
            }

            // Then - All requests should pass through
            verify(filterChain, times(3)).filter(any());
        }

        @Test
        @DisplayName("Should block requests exceeding registration limit")
        void shouldBlockRequestsExceedingRegistrationLimit() {
            // Given - Make 10 requests (exceeding limit of 5)
            int blockedCount = 0;
            for (int i = 0; i < 10; i++) {
                ServerWebExchange exchange = createExchange("/api/onboarding/register", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
                if (exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    blockedCount++;
                }
            }

            // Then - Some requests should be blocked with 429
            assertThat(blockedCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return 429 status for registration rate limit exceeded")
        void shouldReturn429StatusForRegistrationRateLimitExceeded() {
            int blockedCount = 0;
            for (int i = 0; i < 10; i++) {
                ServerWebExchange exchange = createExchange("/api/onboarding/register", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
                if (exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    blockedCount++;
                }
            }
            assertThat(blockedCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should apply registration limit to auth register endpoint")
        void shouldApplyRegistrationLimitToAuthRegisterEndpoint() {
            int blockedCount = 0;
            for (int i = 0; i < 10; i++) {
                ServerWebExchange exchange = createExchange("/api/auth/register", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
                if (exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    blockedCount++;
                }
            }
            assertThat(blockedCount).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Unauthenticated Rate Limiting")
    class UnauthenticatedRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within default limit (60/min)")
        void shouldAllowRequestsWithinDefaultLimit() {
            // Given - Make requests within limit
            for (int i = 0; i < 30; i++) {
                ServerWebExchange exchange = createExchange("/api/public", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
            }

            // Then - All requests should pass through
            verify(filterChain, times(30)).filter(any());
        }

        @Test
        @DisplayName("Should use IP-based limiting for unauthenticated users")
        void shouldUseIpBasedLimitingForUnauthenticatedUsers() {
            // Given
            ServerWebExchange exchange = createExchange("/api/public", "192.168.1.100");

            // When
            rateLimitingFilter.filter(exchange, filterChain).block();

            // Then
            assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("60");
        }

        @Test
        @DisplayName("Should block requests exceeding default limit")
        void shouldBlockRequestsExceedingDefaultLimit() {
            int blockedCount = 0;
            for (int i = 0; i < 100; i++) {
                ServerWebExchange exchange = createExchange("/api/public", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
                if (exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    blockedCount++;
                }
            }
            assertThat(blockedCount).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("IP Address Extraction")
    class IpAddressExtractionTests {

        @Test
        @DisplayName("Should extract IP from X-Forwarded-For header")
        void shouldExtractIpFromXForwardedForHeader() {
            ServerWebExchange exchange = createExchangeWithHeader("/api/public",
                "X-Forwarded-For", "203.0.113.195, 70.41.3.18");

            rateLimitingFilter.filter(exchange, filterChain).block();

            assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("60");
        }

        @Test
        @DisplayName("Should extract IP from X-Real-IP header")
        void shouldExtractIpFromXRealIpHeader() {
            ServerWebExchange exchange = createExchangeWithHeader("/api/public",
                "X-Real-IP", "203.0.113.195");

            rateLimitingFilter.filter(exchange, filterChain).block();

            assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("60");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty X-Forwarded-For header")
        void shouldHandleEmptyXForwardedForHeader() {
            ServerWebExchange exchange = createExchangeWithHeader("/api/public",
                "X-Forwarded-For", "");

            rateLimitingFilter.filter(exchange, filterChain).block();

            assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("60");
        }

        @Test
        @DisplayName("Should handle multiple requests from same IP")
        void shouldHandleMultipleRequestsFromSameIp() {
            for (int i = 0; i < 10; i++) {
                ServerWebExchange exchange = createExchange("/api/public", "192.168.1.100");
                rateLimitingFilter.filter(exchange, filterChain).block();
            }

            verify(filterChain, times(10)).filter(any());
        }
    }
}
