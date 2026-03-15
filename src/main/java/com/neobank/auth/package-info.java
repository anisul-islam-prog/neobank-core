/**
 * Auth module for user authentication and authorization.
 * Provides JWT-based stateless authentication with BCrypt password hashing.
 * Publishes UserCreatedEvent when new users register.
 * 
 * Dependencies:
 * - branches: For branch assignment during user registration
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"branches"}
)
package com.neobank.auth;
