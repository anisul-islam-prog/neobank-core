package com.neobank.analytics.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

/**
 * Test configuration for Analytics module using the "Slim Root" pattern.
 * Provides minimal test infrastructure without full application context.
 */
@TestConfiguration
public class AnalyticsTestConfig {

    /**
     * Dynamic property registrar for test container configuration.
     * Allows tests to override properties based on Testcontainers.
     */
    @Bean
    public DynamicPropertyRegistrar dynamicPropertyRegistrar() {
        return registry -> {
            // PostgreSQL Testcontainer properties are registered via @ServiceConnection
            // This bean is a placeholder for future test property customization
        };
    }
}
