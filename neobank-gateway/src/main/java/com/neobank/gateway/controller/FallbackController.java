package com.neobank.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback controller for Spring Cloud Gateway circuit breakers.
 * Returns graceful degradation responses when downstream services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/core-banking")
    public ResponseEntity<Map<String, Object>> coreBankingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Core Banking Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-core-banking"
            ));
    }

    @GetMapping("/lending")
    public ResponseEntity<Map<String, Object>> lendingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Lending Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-lending"
            ));
    }

    @GetMapping("/cards")
    public ResponseEntity<Map<String, Object>> cardsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Cards Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-cards"
            ));
    }

    @GetMapping("/onboarding")
    public ResponseEntity<Map<String, Object>> onboardingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Onboarding Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-onboarding"
            ));
    }

    @GetMapping("/fraud")
    public ResponseEntity<Map<String, Object>> fraudFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Fraud Detection Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-fraud"
            ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analyticsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Analytics Service Temporarily Unavailable",
                "message", "Please try again later",
                "service", "neobank-analytics"
            ));
    }
}
