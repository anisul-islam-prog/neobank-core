package com.neobank.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * NeoBank Gateway Application.
 * Single entry point for all external traffic.
 *
 * Note: Gateway does not use @Modulithic as it serves as an aggregator
 * for business modules rather than hosting domain modules itself.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.neobank.gateway",
    "com.neobank",
    "com.neobank.core"
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
