/**
 * Accounts module for account management.
 * Listens for UserCreatedEvent to create default savings accounts.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"auth :: auth-api"}
)
package com.neobank.accounts;
