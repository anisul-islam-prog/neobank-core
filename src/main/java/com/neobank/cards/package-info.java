/**
 * Cards module for payment card lifecycle and spending management.
 * Supports virtual and physical card issuance with status controls.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"accounts :: account-api"}
)
package com.neobank.cards;
