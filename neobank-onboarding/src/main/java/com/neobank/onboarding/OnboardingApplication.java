package com.neobank.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NeoBank Customer Onboarding Service.
 *
 * Handles new customer registration, KYC initiation, and document collection.
 * Depends on neobank-auth for user creation and role management.
 *
 * Port: 8081
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.neobank.onboarding", "com.neobank.auth", "com.neobank.core"})
@EntityScan(basePackages = {"com.neobank.onboarding", "com.neobank.auth", "com.neobank.core"})
@EnableJpaRepositories(basePackages = {"com.neobank.onboarding", "com.neobank.auth", "com.neobank.core"})
public class OnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnboardingApplication.class, args);
    }
}
