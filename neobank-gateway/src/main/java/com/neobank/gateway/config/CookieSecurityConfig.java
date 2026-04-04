package com.neobank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive cookie security utilities for Spring Cloud Gateway.
 * Ensures all cookies are HttpOnly, Secure, and SameSite=Strict.
 * Uses WebFlux's ResponseCookie and ServerWebExchange instead of servlet APIs.
 */
@Component
public class CookieSecurityConfig {

    @Value("${neobank.security.cookie.max-age:3600}")
    private int cookieMaxAge;

    @Value("${neobank.security.cookie.domain:neobank.com}")
    private String cookieDomain;

    /**
     * Create a secure JWT token cookie with HttpOnly, Secure, and SameSite=Strict.
     */
    public ResponseCookie createSecureJwtCookie(String tokenName, String jwtToken) {
        return ResponseCookie.from(tokenName, jwtToken)
            .httpOnly(true)
            .secure(true)  // Only send over HTTPS
            .sameSite("Strict")
            .path("/")
            .domain(cookieDomain)
            .maxAge(cookieMaxAge)
            .build();
    }

    /**
     * Create a secure refresh token cookie.
     */
    public ResponseCookie createSecureRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .domain(cookieDomain)
            .maxAge(7 * 24 * 60 * 60)  // 7 days
            .build();
    }

    /**
     * Create a CSRF token cookie (not HttpOnly so JavaScript can read it).
     */
    public ResponseCookie createCsrfCookie(String csrfToken) {
        return ResponseCookie.from("XSRF-TOKEN", csrfToken)
            .httpOnly(false)  // JavaScript needs to read this
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(3600)
            .build();
    }

    /**
     * Delete a cookie by setting max-age to 0.
     */
    public ResponseCookie deleteCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();
    }

    /**
     * Add secure cookie to reactive HTTP response.
     * Uses ServerWebExchange's response.addCookie() for WebFlux compatibility.
     */
    public Mono<Void> addCookieToResponse(ServerWebExchange exchange, ResponseCookie cookie) {
        exchange.getResponse().addCookie(cookie);
        return Mono.empty();
    }

    /**
     * Create a session tracking cookie with security flags.
     */
    public ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie.from("SESSION", sessionId)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(1800)  // 30 minutes
            .build();
    }
}
