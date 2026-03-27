package com.neobank.auth.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration for auth module WebMvc tests.
 * Only scans web layer components, excludes services and repositories.
 * Located in web package to prevent auto-detection by @DataJpaTest.
 */
@SpringBootApplication(scanBasePackages = "com.neobank.auth.web")
@ComponentScan(
    basePackages = "com.neobank.auth",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.neobank\\.auth\\.internal\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.neobank\\.auth\\.api\\..*"
        )
    }
)
public class AuthWebMvcTestConfig {
}
