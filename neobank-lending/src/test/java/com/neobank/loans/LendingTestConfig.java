package com.neobank.loans;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal test configuration for Lending module slice tests.
 * Provides a modular test root that avoids scanning the main application class.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("com.neobank.loans")
@EnableJpaRepositories("com.neobank.loans")
public class LendingTestConfig {
    // No beans needed here; this just serves as a "Root" for the slice test
}
