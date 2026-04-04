package com.neobank.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Spring Cloud Gateway Route Configuration.
 * 
 * Defines routing rules for all API endpoints, proxying requests
 * to the appropriate downstream microservices.
 */
@Configuration
public class RouteConfig {

    @Value("${neobank.gateway.routes.auth-uri:http://localhost:8081}")
    private String authUri;

    @Value("${neobank.gateway.routes.onboarding-uri:http://localhost:8082}")
    private String onboardingUri;

    @Value("${neobank.gateway.routes.core-banking-uri:http://localhost:8083}")
    private String coreBankingUri;

    @Value("${neobank.gateway.routes.lending-uri:http://localhost:8084}")
    private String lendingUri;

    @Value("${neobank.gateway.routes.cards-uri:http://localhost:8085}")
    private String cardsUri;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service Routes
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-auth")
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                    )
                )
                .uri(authUri)
            )

            // Onboarding Service Routes
            .route("onboarding-service", r -> r
                .path("/api/onboarding/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-onboarding")
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                    )
                )
                .uri(onboardingUri)
            )

            // Core Banking Service Routes (Accounts, Transfers)
            .route("core-banking-service", r -> r
                .path("/api/accounts/**", "/api/transfers/**", "/api/branches/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-core-banking")
                    .circuitBreaker(config -> config
                        .setName("coreBankingCircuitBreaker")
                        .setFallbackUri("forward:/fallback/core-banking")
                    )
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                    )
                )
                .uri(coreBankingUri)
            )

            // Lending Service Routes (Loans)
            .route("lending-service", r -> r
                .path("/api/loans/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-lending")
                    .circuitBreaker(config -> config
                        .setName("lendingCircuitBreaker")
                        .setFallbackUri("forward:/fallback/lending")
                    )
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT)
                    )
                )
                .uri(lendingUri)
            )

            // Cards Service Routes
            .route("cards-service", r -> r
                .path("/api/cards/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-cards")
                    .circuitBreaker(config -> config
                        .setName("cardsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/cards")
                    )
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                    )
                )
                .uri(cardsUri)
            )

            // Actuator Routes
            .route("actuator", r -> r
                .path("/actuator/**")
                .filters(f -> f
                    .addResponseHeader("X-Service-Name", "neobank-gateway")
                )
                .uri("no://op")  // Handled locally by gateway
            )

            // OpenAPI/Swagger Routes
            .route("swagger-ui", r -> r
                .path("/swagger-ui/**", "/v3/api-docs/**")
                .filters(f -> f
                    .addResponseHeader("X-Service-Name", "neobank-gateway")
                )
                .uri("no://op")  // Handled locally by gateway
            )

            .build();
    }
}
