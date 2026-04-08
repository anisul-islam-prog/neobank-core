package com.neobank.loans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NeoBank Lending & Loan Service.
 *
 * Handles loan applications, credit scoring, AI-powered risk assessment, approvals, and repayments.
 * Depends on neobank-auth for user verification and neobank-core-banking for account references.
 *
 * Port: 8082
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.neobank.loans", "com.neobank.auth", "com.neobank.core"})
@EntityScan(basePackages = {"com.neobank.loans", "com.neobank.auth", "com.neobank.core"})
@EnableJpaRepositories(basePackages = {"com.neobank.loans", "com.neobank.auth", "com.neobank.core"})
public class LendingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LendingApplication.class, args);
    }
}
