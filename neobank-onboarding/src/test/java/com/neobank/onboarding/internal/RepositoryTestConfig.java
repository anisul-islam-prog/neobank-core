package com.neobank.onboarding.internal;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for repository integration tests.
 * Provides a modular test root for @DataJpaTest.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("com.neobank.onboarding")
@EnableJpaRepositories("com.neobank.onboarding")
class RepositoryTestConfig {
    // No beans needed here; this just serves as a "Root" for the slice test
}
