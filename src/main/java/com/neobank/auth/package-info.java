/**
 * Auth module for user authentication and authorization.
 * Provides JWT-based stateless authentication with BCrypt password hashing.
 * Publishes UserCreatedEvent when new users register.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {}
)
package com.neobank.auth;
