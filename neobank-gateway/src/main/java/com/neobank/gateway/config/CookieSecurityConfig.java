package com.neobank.gateway.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie security utilities for Phase 7: Security Hardening.
 * Ensures all cookies are HttpOnly, Secure, and SameSite=Strict.
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
     * Add secure cookie to HTTP response.
     */
    public void addCookieToResponse(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader("Set-Cookie", cookie.toString());
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
