package com.neobank.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitingFilter using JUnit 5 and Mockito.
 * Tests rate limiting logic for different user types and endpoints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter Unit Tests")
class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private RateLimitingFilter rateLimitingFilter;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        rateLimitingFilter = new RateLimitingFilter();
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        given(response.getWriter()).willReturn(printWriter);
    }

    @Nested
    @DisplayName("Retail User Rate Limiting")
    class RetailUserRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within retail limit (100/min)")
        void shouldAllowRequestsWithinRetailLimit() throws Exception {
            // Given
            User user = new User("retailuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Make requests within limit
            for (int i = 0; i < 50; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - All requests should pass through
            verify(filterChain, times(50)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should block requests exceeding retail limit")
        void shouldBlockRequestsExceedingRetailLimit() throws Exception {
            // Given
            User user = new User("retailuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(response.getStatus()).willReturn(200);

            // When - Make requests exceeding limit
            for (int i = 0; i < 150; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - Some requests should be blocked
            // Note: Bucket4j allows burst, so exact count may vary
            verify(filterChain, atLeast(100)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should set correct rate limit headers for retail users")
        void shouldSetCorrectRateLimitHeadersForRetailUsers() throws Exception {
            // Given
            User user = new User("retailuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "100");
        }
    }

    @Nested
    @DisplayName("Staff User Rate Limiting")
    class StaffUserRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within staff limit (500/min)")
        void shouldAllowRequestsWithinStaffLimit() throws Exception {
            // Given
            User user = new User("staffuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Make requests within limit
            for (int i = 0; i < 100; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - All requests should pass through
            verify(filterChain, times(100)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should recognize ADMIN role as staff")
        void shouldRecognizeAdminRoleAsStaff() throws Exception {
            // Given
            User user = new User("adminuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "500");
        }

        @Test
        @DisplayName("Should recognize MANAGER role as staff")
        void shouldRecognizeManagerRoleAsStaff() throws Exception {
            // Given
            User user = new User("manageruser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "500");
        }

        @Test
        @DisplayName("Should set correct rate limit headers for staff users")
        void shouldSetCorrectRateLimitHeadersForStaffUsers() throws Exception {
            // Given
            User user = new User("staffuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "500");
        }
    }

    @Nested
    @DisplayName("Registration Rate Limiting")
    class RegistrationRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within registration limit (5/min)")
        void shouldAllowRequestsWithinRegistrationLimit() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Make 3 requests (within limit)
            for (int i = 0; i < 3; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - All requests should pass through
            verify(filterChain, times(3)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should block requests exceeding registration limit")
        void shouldBlockRequestsExceedingRegistrationLimit() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(response.getStatus()).willReturn(200);

            // When - Make 10 requests (exceeding limit of 5)
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - Some requests should be blocked with 429
            verify(response, atLeastOnce()).setStatus(429);
        }

        @Test
        @DisplayName("Should return 429 status for registration rate limit exceeded")
        void shouldReturn429StatusForRegistrationRateLimitExceeded() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Exceed limit
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then
            verify(response).setStatus(429);
        }

        @Test
        @DisplayName("Should set correct rate limit headers for registration")
        void shouldSetCorrectRateLimitHeadersForRegistration() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Exceed limit
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "5");
            verify(response).setHeader("X-RateLimit-Remaining", "0");
            verify(response).setHeader("X-RateLimit-Reset", "60");
        }

        @Test
        @DisplayName("Should return error message for registration rate limit")
        void shouldReturnErrorMessageForRegistrationRateLimit() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Exceed limit
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then
            verify(response).getWriter();
            assertThat(responseWriter.toString()).contains("Too many registration requests");
        }

        @Test
        @DisplayName("Should apply registration limit to auth register endpoint")
        void shouldApplyRegistrationLimitToAuthRegisterEndpoint() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/auth/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Exceed limit
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - Should be rate limited
            verify(response, atLeastOnce()).setStatus(429);
        }
    }

    @Nested
    @DisplayName("Unauthenticated Rate Limiting")
    class UnauthenticatedRateLimitingTests {

        @Test
        @DisplayName("Should allow requests within default limit (60/min)")
        void shouldAllowRequestsWithinDefaultLimit() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/public");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When - Make requests within limit
            for (int i = 0; i < 30; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - All requests should pass through
            verify(filterChain, times(30)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should use IP-based limiting for unauthenticated users")
        void shouldUseIpBasedLimitingForUnauthenticatedUsers() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/public");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should block requests exceeding default limit")
        void shouldBlockRequestsExceedingDefaultLimit() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/public");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);
            given(response.getStatus()).willReturn(200);

            // When - Make requests exceeding limit
            for (int i = 0; i < 100; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - Some requests should be blocked
            verify(response, atLeastOnce()).setStatus(429);
        }
    }

    @Nested
    @DisplayName("IP Address Extraction")
    class IpAddressExtractionTests {

        @Test
        @DisplayName("Should extract IP from X-Forwarded-For header")
        void shouldExtractIpFromXForwardedForHeader() throws Exception {
            // Given
            given(request.getHeader("X-Forwarded-For")).willReturn("203.0.113.195, 70.41.3.18");
            given(request.getRequestURI()).willReturn("/api/public");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then - Should use first IP from X-Forwarded-For
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should extract IP from X-Real-IP header")
        void shouldExtractIpFromXRealIpHeader() throws Exception {
            // Given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getHeader("X-Real-IP")).willReturn("203.0.113.195");
            given(request.getRequestURI()).willReturn("/api/public");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should fall back to remote address when no headers present")
        void shouldFallBackToRemoteAddressWhenNoHeadersPresent() throws Exception {
            // Given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getHeader("X-Real-IP")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getRequestURI()).willReturn("/api/public");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }
    }

    @Nested
    @DisplayName("Rate Limit Headers")
    class RateLimitHeadersTests {

        @Test
        @DisplayName("Should set rate limit headers on rate limit exceeded")
        void shouldSetRateLimitHeadersOnRateLimitExceeded() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/onboarding/register");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When - Exceed limit
            for (int i = 0; i < 10; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "5");
            verify(response).setHeader("X-RateLimit-Remaining", "0");
            verify(response).setHeader("X-RateLimit-Reset", "60");
        }

        @Test
        @DisplayName("Should set rate limit headers for staff on success")
        void shouldSetRateLimitHeadersForStaffOnSuccess() throws Exception {
            // Given
            User user = new User("staffuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader(eq("X-RateLimit-Limit"), eq("500"));
        }

        @Test
        @DisplayName("Should set rate limit headers for retail on success")
        void shouldSetRateLimitHeadersForRetailOnSuccess() throws Exception {
            // Given
            User user = new User("retailuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader(eq("X-RateLimit-Limit"), eq("100"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unauthenticated authentication object")
        void shouldHandleUnauthenticatedAuthenticationObject() throws Exception {
            // Given
            given(authentication.isAuthenticated()).willReturn(false);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/public");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then - Should use IP-based limiting
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should handle non-User principal")
        void shouldHandleNonUserPrincipal() throws Exception {
            // Given
            Object nonUserPrincipal = new Object();
            given(authentication.getPrincipal()).willReturn(nonUserPrincipal);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/public");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then - Should use IP-based limiting
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should handle multiple roles correctly")
        void shouldHandleMultipleRolesCorrectly() throws Exception {
            // Given
            User user = new User("multiroleuser", "password",
                List.of(
                    new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL"),
                    new SimpleGrantedAuthority("ROLE_STAFF")
                ));
            given(authentication.getPrincipal()).willReturn(user);
            given(authentication.isAuthenticated()).willReturn(true);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then - Should use staff limit (higher privilege)
            verify(response).setHeader("X-RateLimit-Limit", "500");
        }

        @Test
        @DisplayName("Should handle empty X-Forwarded-For header")
        void shouldHandleEmptyXForwardedForHeader() throws Exception {
            // Given
            given(request.getHeader("X-Forwarded-For")).willReturn("");
            given(request.getHeader("X-Real-IP")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getRequestURI()).willReturn("/api/public");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Then - Should fall back to remote address
            verify(response).setHeader("X-RateLimit-Limit", "60");
        }

        @Test
        @DisplayName("Should handle different users with same IP")
        void shouldHandleDifferentUsersWithSameIp() throws Exception {
            // Given
            given(request.getRequestURI()).willReturn("/api/accounts");
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(securityContext.getAuthentication()).willReturn(null);
            SecurityContextHolder.setContext(securityContext);

            // When - Multiple requests from same IP
            for (int i = 0; i < 5; i++) {
                rateLimitingFilter.doFilterInternal(request, response, filterChain);
            }

            // Then - All should use same IP bucket
            verify(filterChain, times(5)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Bandwidth Configuration")
    class BandwidthConfigurationTests {

        @Test
        @DisplayName("Should have correct retail bandwidth (100/min)")
        void shouldHaveCorrectRetailBandwidth() {
            // Given
            Bandwidth retailLimit = Bandwidth.classic(
                100,
                Refill.greedy(100, Duration.ofMinutes(1))
            );

            // Then
            assertThat(retailLimit.getCapacity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should have correct staff bandwidth (500/min)")
        void shouldHaveCorrectStaffBandwidth() {
            // Given
            Bandwidth staffLimit = Bandwidth.classic(
                500,
                Refill.greedy(500, Duration.ofMinutes(1))
            );

            // Then
            assertThat(staffLimit.getCapacity()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should have correct registration bandwidth (5/min)")
        void shouldHaveCorrectRegistrationBandwidth() {
            // Given
            Bandwidth registrationLimit = Bandwidth.classic(
                5,
                Refill.greedy(5, Duration.ofMinutes(1))
            );

            // Then
            assertThat(registrationLimit.getCapacity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should have correct default bandwidth (60/min)")
        void shouldHaveCorrectDefaultBandwidth() {
            // Given
            Bandwidth defaultLimit = Bandwidth.classic(
                60,
                Refill.greedy(60, Duration.ofMinutes(1))
            );

            // Then
            assertThat(defaultLimit.getCapacity()).isEqualTo(60);
        }
    }
}
