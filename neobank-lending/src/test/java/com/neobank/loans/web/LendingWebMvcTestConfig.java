package com.neobank.loans.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration for lending module WebMvc tests.
 * Provides only the ObjectMapper bean needed by @WebMvcTest slice.
 * Security is auto-configured by @WebMvcTest + spring-security-test.
 */
@Configuration
public class LendingWebMvcTestConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
