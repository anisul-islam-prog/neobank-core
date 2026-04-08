package com.neobank.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NeoBank Authentication & Authorization Service.
 * 
 * Handles user registration, login, JWT token issuance, and role management.
 * Depends on neobank-core-banking for user account references.
 * 
 * Port: 8081
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.neobank.auth", "com.neobank.core"})
@EntityScan(basePackages = {"com.neobank.auth", "com.neobank.core"})
@EnableJpaRepositories(basePackages = {"com.neobank.auth", "com.neobank.core"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
