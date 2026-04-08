package com.neobank.cards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NeoBank Cards Service.
 *
 * Handles debit/virtual card issuance, card management, card controls, and lifecycle.
 * Depends on neobank-auth for user verification and neobank-core-banking for account references.
 *
 * Port: 8084
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.neobank.cards", "com.neobank.auth", "com.neobank.core"})
@EntityScan(basePackages = {"com.neobank.cards", "com.neobank.auth", "com.neobank.core"})
@EnableJpaRepositories(basePackages = {"com.neobank.cards", "com.neobank.auth", "com.neobank.core"})
public class CardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardsApplication.class, args);
    }
}
