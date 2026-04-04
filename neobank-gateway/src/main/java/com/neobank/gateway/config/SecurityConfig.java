package com.neobank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reactive Security configuration for Spring Cloud Gateway.
 * Implements CORS policies, JWT authentication, and CSRF protection for WebFlux.
 */
@Configuration
@EnableWebFluxSecurity
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
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter) {
        
        return http
            // CSRF Configuration - Disable for API endpoints that use JWT authentication
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(
                    ServerWebExchangeMatchers.pathMatchers(
                        "/api/auth/**",
                        "/api/onboarding/register",
                        "/api/transfers/**",
                        "/api/accounts/**",
                        "/api/cards/**",
                        "/api/loans/**"
                    )
                )
            )

            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Session Management - Stateless for APIs
            .securityContextRepository(
                org.springframework.security.web.server.context.NoOpServerSecurityContextRepository.getInstance()
            )

            // JWT Authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )

            // Authorization Rules
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/onboarding/register",
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
                ).permitAll()

                // All other API endpoints require authentication
                .pathMatchers("/api/**").authenticated()

                // Actuator endpoints (except health/info) require admin role
                .pathMatchers("/actuator/**").hasRole("ADMIN")

                // Default rule
                .anyExchange().authenticated()
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
                .frameOptions(options -> {})  // Use default DENY
                .contentTypeOptions(customizer -> {})
                .referrerPolicy(referrer -> referrer.policy(
                    org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                ))
                .permissionsPolicy(permissions -> permissions.policy(
                    "camera=(), microphone=(), geolocation=(), payment=()"
                ))
            )

            .build();
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
     * Reactive JWT Authentication Converter.
     * Converts JWT claims to Spring Security Authentication object.
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
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
