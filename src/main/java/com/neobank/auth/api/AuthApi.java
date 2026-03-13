package com.neobank.auth.api;

import com.neobank.auth.AuthenticationRequest;
import com.neobank.auth.AuthenticationResult;
import com.neobank.auth.RegistrationRequest;
import com.neobank.auth.RegistrationResult;

import java.util.UUID;

/**
 * Public API interface for the auth module.
 * Defines the contract for user authentication and registration.
 */
public interface AuthApi {

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the registration result with user ID
     */
    RegistrationResult register(RegistrationRequest request);

    /**
     * Authenticate a user and return JWT token.
     *
     * @param request the authentication request
     * @return the authentication result with token
     */
    AuthenticationResult authenticate(AuthenticationRequest request);

    /**
     * Get user ID from JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    UUID getUserIdFromToken(String token);
}
