/**
 * Cards module for payment card lifecycle and spending management.
 * Supports virtual and physical card issuance with status controls.
 * 
 * Access Control: Only ACTIVE users can issue or manage cards.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"accounts :: account-api", "auth :: auth-api"}
)
package com.neobank.cards;
