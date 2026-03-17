package com.neobank.auth.internal;

import com.neobank.auth.internal.docs.DocAccessTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration with JWT authentication and documentation access control.
 * Protects /api/** endpoints while restricting Swagger UI to token holders.
 * Disabled during tests.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configure security filter chain with JWT authentication and role-based authorization.
     * Documentation endpoints require valid access token or authenticated admin user.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, DocAccessTokenFilter docAccessTokenFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required (register, login)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/accounts/**"
                        ).permitAll()
                        // Documentation endpoints - require DOC_ACCESS role or authenticated admin
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/swagger-ui/**"
                        ).hasAnyRole("DOC_ACCESS", "SYSTEM_ADMIN", "MANAGER")
                        // Staff onboarding - MANAGER, RELATIONSHIP_OFFICER, or SYSTEM_ADMIN
                        .requestMatchers("/api/auth/onboard").hasAnyRole("MANAGER", "RELATIONSHIP_OFFICER", "SYSTEM_ADMIN")
                        // User approval - MANAGER or RELATIONSHIP_OFFICER
                        .requestMatchers("/api/auth/users/*/approve").hasAnyRole("MANAGER", "RELATIONSHIP_OFFICER", "SYSTEM_ADMIN")
                        // Staff approval - SYSTEM_ADMIN only
                        .requestMatchers("/api/auth/staff/*/approve").hasRole("SYSTEM_ADMIN")
                        // User status update - MANAGER or SYSTEM_ADMIN
                        .requestMatchers("/api/auth/users/*/status").hasAnyRole("MANAGER", "SYSTEM_ADMIN")
                        // Documentation token management - SYSTEM_ADMIN only
                        .requestMatchers("/api/auth/admin/docs/**").hasRole("SYSTEM_ADMIN")
                        // Audit endpoints - AUDITOR only
                        .requestMatchers("/api/audit/**").hasRole("AUDITOR")
                        // Loan approval - MANAGER or SYSTEM_ADMIN only
                        .requestMatchers("/api/loans/approve/**").hasAnyRole("MANAGER", "SYSTEM_ADMIN")
                        // Account search (non-owned) - TELLER or above
                        .requestMatchers("/api/accounts/search/**").hasAnyRole("TELLER", "RELATIONSHIP_OFFICER", "MANAGER", "AUDITOR", "SYSTEM_ADMIN")
                        // All other /api/** endpoints require authentication
                        .requestMatchers("/api/loans/**", "/api/cards/**").authenticated()
                        .requestMatchers("/api/transfers/**").authenticated()
                        // All other requests
                        .anyRequest().permitAll()
                )
                .addFilterBefore(docAccessTokenFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Security context repository for session storage.
     */
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * BCrypt password encoder.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS configuration for frontend access.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
