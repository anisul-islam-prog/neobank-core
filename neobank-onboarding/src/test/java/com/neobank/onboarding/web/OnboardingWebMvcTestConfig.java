package com.neobank.onboarding.web;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Minimal Spring Boot configuration for WebMvc tests.
 * Only scans for RestController beans, excludes repositories and entities.
 * Note: @WebMvcTest provides MVC auto-configuration, we just need to scan for controllers.
 */
@SpringBootConfiguration
@ComponentScan(
    basePackages = "com.neobank.onboarding",
    includeFilters = @Filter(
        type = FilterType.ANNOTATION,
        classes = org.springframework.web.bind.annotation.RestController.class
    ),
    excludeFilters = {
        @Filter(type = FilterType.REGEX, pattern = "com\\.neobank\\.onboarding\\.internal\\..*"),
        @Filter(type = FilterType.ANNOTATION, classes = org.springframework.stereotype.Repository.class)
    }
)
public class OnboardingWebMvcTestConfig {
    // Minimal configuration for WebMvc tests
}
