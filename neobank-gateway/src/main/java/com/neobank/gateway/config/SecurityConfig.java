package com.neobank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Phase 7: Security Hardening.
 * Implements CORS policies, CSRF protection, and secure cookie settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Allowed frontend domains - only these 3 specific domains are permitted
    private static final List<String> ALLOWED_ORIGINS = List.of(
        "https://retail.neobank.com",      // Retail banking portal
        "https://staff.neobank.com",       // Staff portal
        "https://admin.neobank.com"        // Admin console
    );

    // Development origins (only for local development)
    private static final List<String> DEV_ORIGINS = List.of(
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002",
        "http://localhost:5173",
        "http://localhost:5174"
    );

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF token handler for SPA applications
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            // Disable CSRF for API endpoints that use JWT authentication
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/auth/**",
                    "/api/onboarding/register",
                    "/api/transfers/**",
                    "/api/accounts/**",
                    "/api/cards/**",
                    "/api/loans/**"
                )
                // Enable CSRF for browser-based operations
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )
            
            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Session Management - Stateless for APIs
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/onboarding/register",
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
                ).permitAll()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Actuator endpoints (except health/info) require admin role
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // Default rule
                .anyRequest().authenticated()
            )
            
            // Security Headers
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(customizer -> {})
                .cacheControl(cache -> cache.disable())
                .referrerPolicy(referrer -> referrer.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                ))
                .permissionsPolicy(permissions -> permissions.policy(
                    "camera=(), microphone=(), geolocation=(), payment=()"
                ))
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Combine production and development origins
        List<String> allOrigins = new java.util.ArrayList<>(ALLOWED_ORIGINS);
        if (isDevelopmentEnvironment()) {
            allOrigins.addAll(DEV_ORIGINS);
        }
        
        configuration.setAllowedOrigins(allOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-XSRF-TOKEN"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Trace-Id",
            "X-Span-Id",
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset"
        ));
        
        // Security settings
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Check if running in development environment.
     * In production, only the 3 specific frontend domains are allowed.
     */
    private boolean isDevelopmentEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active", 
            System.getenv("SPRING_PROFILES_ACTIVE"));
        return activeProfile != null && (
            activeProfile.contains("dev") || 
            activeProfile.contains("local") ||
            activeProfile.contains("test")
        );
    }
}
