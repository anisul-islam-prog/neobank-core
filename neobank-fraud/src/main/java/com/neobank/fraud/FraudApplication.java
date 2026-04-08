package com.neobank.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NeoBank Fraud Detection Service.
 *
 * Handles fraud detection, velocity checks, blacklisting, AI-powered risk analysis,
 * and suspicious activity monitoring. Depends on neobank-core-banking for transaction data.
 *
 * Port: 8085
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.neobank.fraud", "com.neobank.core"})
@EntityScan(basePackages = {"com.neobank.fraud", "com.neobank.core"})
@EnableJpaRepositories(basePackages = {"com.neobank.fraud", "com.neobank.core"})
public class FraudApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudApplication.class, args);
    }
}
