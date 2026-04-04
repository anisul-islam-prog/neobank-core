package com.neobank.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * NeoBank Gateway Application - Reactive API Gateway.
 * 
 * Single entry point for all external traffic.
 * Routes requests to internal microservices via Spring Cloud Gateway.
 * 
 * Note: This gateway uses WebFlux (reactive) and does NOT directly
 * host domain modules. It proxies requests to downstream services.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.neobank.gateway"
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
