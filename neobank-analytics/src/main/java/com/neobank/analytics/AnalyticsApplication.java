package com.neobank.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Analytics/BI Module - CQRS Implementation
 * Listens to events from Core, Loans, and Cards modules.
 * Saves data into read-optimized flat tables for business dashboards.
 */
@SpringBootApplication
@Modulithic
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
