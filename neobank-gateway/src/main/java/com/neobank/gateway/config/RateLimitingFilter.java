package com.neobank.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reactive API Rate Limiting Filter using Bucket4j.
 * Implements request rate limiting at the gateway level using WebFlux.
 *
 * Rules:
 * - retail-app users: 100 requests per minute
 * - staff-portal users: 500 requests per minute
 * - Public registration: 5 requests per minute (to prevent bot spam)
 * - Default (unauthenticated): 60 requests per minute
 */
@Component
@Order(2)
public class RateLimitingFilter implements WebFilter {

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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();
        String clientIp = getClientIp(exchange);

        // Check if this is a registration endpoint (stricter limit)
        if (uri.contains("/api/onboarding/register") || uri.contains("/api/auth/register")) {
            return handleRegistrationRateLimit(exchange, chain, clientIp);
        }

        // Try to get authenticated user from ReactiveSecurityContextHolder
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .flatMap(auth -> handleAuthenticatedRateLimit(exchange, chain, auth))
            .switchIfEmpty(Mono.defer(() -> handleUnauthenticatedRateLimit(exchange, chain, clientIp)));
    }

    /**
     * Handle rate limiting for registration endpoints.
     */
    private Mono<Void> handleRegistrationRateLimit(
            ServerWebExchange exchange,
            WebFilterChain chain,
            String clientIp) {
        
        String rateLimitKey = "registration:" + clientIp;
        Bucket bucket = registrationBuckets.computeIfAbsent(rateLimitKey,
            k -> Bucket.builder().addLimit(REGISTRATION_LIMIT).build());

        if (bucket.tryConsume(1)) {
            // Add rate limit headers and continue
            exchange.getResponse().getHeaders().set("X-RateLimit-Limit", "5");
            exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                String.valueOf(bucket.getAvailableTokens()));
            exchange.getResponse().getHeaders().set("X-RateLimit-Reset", "60");
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for registration from IP: {}", clientIp);
        return writeRateLimitResponse(exchange,
            "{\"error\": \"Too many registration requests. Please try again later.\"}",
            5, "60");
    }

    /**
     * Handle rate limiting for authenticated users.
     */
    private Mono<Void> handleAuthenticatedRateLimit(
            ServerWebExchange exchange,
            WebFilterChain chain,
            Authentication auth) {
        
        String rateLimitKey = extractUserKey(auth);
        Bandwidth limit = getLimitForUser(auth);

        Bucket bucket = userBuckets.computeIfAbsent(rateLimitKey,
            k -> Bucket.builder().addLimit(limit).build());

        if (bucket.tryConsume(1)) {
            // Add rate limit headers and continue
            exchange.getResponse().getHeaders().set("X-RateLimit-Limit",
                String.valueOf(limit.getCapacity()));
            exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                String.valueOf(bucket.getAvailableTokens()));
            exchange.getResponse().getHeaders().set("X-RateLimit-Reset", "60");
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for user: {}", rateLimitKey);
        return writeRateLimitResponse(exchange,
            "{\"error\": \"Rate limit exceeded. Please slow down.\"}",
            limit.getCapacity(), "60");
    }

    /**
     * Handle rate limiting for unauthenticated users.
     */
    private Mono<Void> handleUnauthenticatedRateLimit(
            ServerWebExchange exchange,
            WebFilterChain chain,
            String clientIp) {
        
        String rateLimitKey = "ip:" + clientIp;

        Bucket bucket = ipBuckets.computeIfAbsent(rateLimitKey,
            k -> Bucket.builder().addLimit(DEFAULT_LIMIT).build());

        if (bucket.tryConsume(1)) {
            // Add rate limit headers and continue
            exchange.getResponse().getHeaders().set("X-RateLimit-Limit", "60");
            exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                String.valueOf(bucket.getAvailableTokens()));
            exchange.getResponse().getHeaders().set("X-RateLimit-Reset", "60");
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for IP: {}", clientIp);
        return writeRateLimitResponse(exchange,
            "{\"error\": \"Too many requests. Please try again later.\"}",
            60, "60");
    }

    /**
     * Write a 429 Too Many Requests response.
     */
    private Mono<Void> writeRateLimitResponse(
            ServerWebExchange exchange,
            String errorMessage,
            long limit,
            String resetSeconds) {
        
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().set("X-RateLimit-Reset", resetSeconds);

        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    /**
     * Extract user key from authentication token.
     */
    private String extractUserKey(Authentication auth) {
        return "user:" + auth.getName();
    }

    /**
     * Determine rate limit based on user role.
     */
    private Bandwidth getLimitForUser(Authentication auth) {
        boolean isStaff = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().contains("STAFF") ||
                           a.getAuthority().contains("ADMIN") ||
                           a.getAuthority().contains("MANAGER"));
        return isStaff ? STAFF_LIMIT : RETAIL_LIMIT;
    }

    /**
     * Extract client IP address from request.
     */
    private String getClientIp(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        
        String xForwardedFor = headers.getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = headers.getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return remoteAddress != null ? remoteAddress.getHostString() : "unknown";
    }
}
