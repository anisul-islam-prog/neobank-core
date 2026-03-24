package com.neobank.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Rate Limiting Filter using Bucket4j.
 * Implements request rate limiting at the gateway level.
 * 
 * Rules:
 * - retail-app users: 100 requests per minute
 * - staff-portal users: 500 requests per minute
 * - Public registration: 5 requests per minute (to prevent bot spam)
 * - Default (unauthenticated): 60 requests per minute
 */
@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Bandwidth configurations
    private static final Bandwidth RETAIL_LIMIT = Bandwidth.classic(
        100,
        Refill.greedy(100, Duration.ofMinutes(1))
    );

    private static final Bandwidth STAFF_LIMIT = Bandwidth.classic(
        500,
        Refill.greedy(500, Duration.ofMinutes(1))
    );

    private static final Bandwidth REGISTRATION_LIMIT = Bandwidth.classic(
        5,
        Refill.greedy(5, Duration.ofMinutes(1))
    );

    private static final Bandwidth DEFAULT_LIMIT = Bandwidth.classic(
        60,
        Refill.greedy(60, Duration.ofMinutes(1))
    );

    // Token buckets per user/IP
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registrationBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        // Check if this is a registration endpoint (stricter limit)
        if (uri.contains("/api/onboarding/register") || uri.contains("/api/auth/register")) {
            if (!tryConsume(registrationBuckets, clientIp, REGISTRATION_LIMIT, response)) {
                log.warn("Rate limit exceeded for registration from IP: {}", clientIp);
                response.setStatus(429);
                response.setHeader("X-RateLimit-Limit", "5");
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", "60");
                response.getWriter().write("{\"error\": \"Too many registration requests. Please try again later.\"}");
                return;
            }
        }

        // Try to get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rateLimitKey;
        Bandwidth limit;

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            rateLimitKey = "user:" + user.getUsername();
            
            // Determine limit based on user role
            if (user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("STAFF") || 
                                   a.getAuthority().contains("ADMIN") ||
                                   a.getAuthority().contains("MANAGER"))) {
                limit = STAFF_LIMIT;
            } else {
                limit = RETAIL_LIMIT;
            }

            if (!tryConsume(userBuckets, rateLimitKey, limit, response)) {
                log.warn("Rate limit exceeded for user: {}", user.getUsername());
                response.setStatus(429);
                response.setHeader("X-RateLimit-Limit", String.valueOf(limit.getCapacity()));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", "60");
                response.getWriter().write("{\"error\": \"Rate limit exceeded. Please slow down.\"}");
                return;
            }
        } else {
            // Unauthenticated - use IP-based limiting with default limit
            rateLimitKey = "ip:" + clientIp;
            
            if (!tryConsume(ipBuckets, rateLimitKey, DEFAULT_LIMIT, response)) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(429);
                response.setHeader("X-RateLimit-Limit", "60");
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", "60");
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        // Add rate limit headers to successful responses
        response.setHeader("X-RateLimit-Limit", 
            String.valueOf(getLimitForUser(auth)));

        filterChain.doFilter(request, response);
    }

    /**
     * Try to consume a token from the bucket.
     * Creates a new bucket if one doesn't exist for the key.
     */
    private boolean tryConsume(
            Map<String, Bucket> buckets,
            String key,
            Bandwidth limit,
            HttpServletResponse response
    ) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> 
            Bucket.builder().addLimit(limit).build()
        );

        // Try to consume a token
        return bucket.tryConsume(1);
    }

    /**
     * Get the rate limit for the current user (for header purposes).
     */
    private long getLimitForUser(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            if (user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("STAFF") || 
                                   a.getAuthority().contains("ADMIN") ||
                                   a.getAuthority().contains("MANAGER"))) {
                return 500;
            } else {
                return 100;
            }
        }
        return 60;
    }

    /**
     * Extract client IP address from request.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
