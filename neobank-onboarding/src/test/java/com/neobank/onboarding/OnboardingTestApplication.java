package com.neobank.onboarding;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Onboarding module test configuration.
 * Serves as a marker for test context customization.
 * Note: Using @TestConfiguration instead of @SpringBootConfiguration to avoid
 * interfering with @WebMvcTest slice tests.
 */
@TestConfiguration
public class OnboardingTestApplication {
    // Test configuration class for onboarding module - no beans needed
}
