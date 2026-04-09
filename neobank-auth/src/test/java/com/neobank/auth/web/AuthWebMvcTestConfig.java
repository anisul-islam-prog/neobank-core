package com.neobank.auth.web;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Test configuration for auth module WebMvc tests.
 * Excludes heavy auto-configuration (security, database) for fast slice tests.
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.neobank.auth.web",
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
