package com.neobank.loans;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Minimal test configuration for Lending module slice tests.
 * Provides a @SpringBootConfiguration root for @WebMvcTest without JPA.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration.class
})
public class LendingTestConfig {
    // No beans needed; serves as @SpringBootConfiguration root for slice tests
}
