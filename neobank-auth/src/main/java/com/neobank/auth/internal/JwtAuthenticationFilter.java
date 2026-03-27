package com.neobank.auth.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT authentication filter that extracts and validates tokens from requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class JwtAuthenticationFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Check if authentication already exists (e.g., from @WithMockUser in tests)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);
            String requestPath = request.getRequestURI();

            if (StringUtils.hasText(jwt)) {
                UUID userId = jwtService.extractUserId(jwt);
                String username = jwtService.extractUsername(jwt);
                String audience = jwtService.extractAudience(jwt);

                // Validate audience based on request path
                String expectedAudience = determineExpectedAudience(requestPath);
                if (expectedAudience != null && !jwtService.validateAudience(jwt, expectedAudience)) {
                    log.warn("Audience mismatch: expected {}, got {} for user {} on path {}",
                             expectedAudience, audience, username, requestPath);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                       "Invalid token audience for this portal");
                    return;
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authenticated user: {} ({}) with audience: {}", username, userId, audience);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determine expected audience based on request path.
     * Returns null if no specific audience is required.
     */
    private String determineExpectedAudience(String path) {
        if (path.startsWith("/api/retail/") || path.startsWith("/api/accounts/") || 
            path.startsWith("/api/transfers/")) {
            return "retail";
        } else if (path.startsWith("/api/staff/") || path.startsWith("/api/onboarding/") ||
                   path.startsWith("/api/loans/")) {
            return "staff";
        } else if (path.startsWith("/api/admin/") || path.startsWith("/api/audit/") ||
                   path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return "admin";
        }
        return null;  // No specific audience required
    }

    /**
     * Extract JWT token from Authorization header.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
