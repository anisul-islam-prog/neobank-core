package com.neobank.cards.web;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Dedicated @SpringBootConfiguration for WebMvc slice tests.
 * Excludes all JPA/Data/Liquibase auto-configuration so that the test context
 * remains lightweight and doesn't require a DataSource or EntityManagerFactory.
 *
 * Spring Boot 4 @WebMvcTest picks up the nearest @SpringBootConfiguration on the
 * classpath. Without this class it would pick up CardsTestConfig (which enables
 * JPA), causing "No bean named 'entityManagerFactory'" errors in the sliced
 * context.
 *
 * Note: Spring Boot 4 reorganized auto-configuration classes into separate
 * modules (spring-boot-jdbc, spring-boot-data-jpa, etc.) under new package
 * namespaces (org.springframework.boot.*.autoconfigure instead of
 * org.springframework.boot.autoconfigure.*).
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackageClasses = CardController.class)
public class CardsWebMvcTestBootConfig {
    // No beans — just a root config for @WebMvcTest that avoids JPA
}
