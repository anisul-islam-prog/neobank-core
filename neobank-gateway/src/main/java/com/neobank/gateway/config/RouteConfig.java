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

    @Value("${neobank.gateway.routes.lending-uri:http://localhost:8082}")
    private String lendingUri;

    @Value("${neobank.gateway.routes.cards-uri:http://localhost:8084}")
    private String cardsUri;

    @Value("${neobank.gateway.routes.fraud-uri:http://localhost:8085}")
    private String fraudUri;

    @Value("${neobank.gateway.routes.batch-uri:http://localhost:8086}")
    private String batchUri;

    @Value("${neobank.gateway.routes.analytics-uri:http://localhost:8087}")
    private String analyticsUri;

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
                    .circuitBreaker(config -> config
                        .setName("onboardingCircuitBreaker")
                        .setFallbackUri("forward:/fallback/onboarding")
                    )
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

            // Fraud Detection Service Routes
            .route("fraud-service", r -> r
                .path("/api/fraud/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-fraud")
                    .circuitBreaker(config -> config
                        .setName("fraudCircuitBreaker")
                        .setFallbackUri("forward:/fallback/fraud")
                    )
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                    )
                )
                .uri(fraudUri)
            )

            // Batch Processing Service Routes
            .route("batch-service", r -> r
                .path("/api/batch/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-batch")
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                    )
                )
                .uri(batchUri)
            )

            // Analytics Service Routes
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("X-Service-Name", "neobank-analytics")
                    .circuitBreaker(config -> config
                        .setName("analyticsCircuitBreaker")
                        .setFallbackUri("forward:/fallback/analytics")
                    )
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                    )
                )
                .uri(analyticsUri)
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
