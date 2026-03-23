package com.neobank.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Core Banking Module - Accounts, Transfers, Branches
 * Uses schema_core for database isolation
 */
@SpringBootApplication
@Modulithic
public class CoreBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingApplication.class, args);
    }
}
