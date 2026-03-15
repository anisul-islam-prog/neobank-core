package com.neobank.auth.internal;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration with JWT Bearer Authentication support.
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * 
 * To authenticate:
 * 1. Click the "Authorize" button (lock icon) in Swagger UI
 * 2. Enter your JWT token (obtained from /api/auth/login)
 * 3. Click "Authorize"
 * 4. All secured endpoints will now include your token automatically
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configure OpenAPI with JWT Bearer Authentication scheme.
     */
    @Bean
    public OpenAPI neobankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NeoBank API")
                        .version("1.0.0")
                        .description("""
                                ## NeoBank Core Banking API
                                
                                A modern, cloud-native banking platform featuring:
                                - 🏦 Instant account opening with automatic savings accounts
                                - 💳 Smart card management with real-time controls
                                - 💰 AI-powered loans with instant decisions
                                - 🔒 Bank-grade security with JWT authentication
                                
                                ### Authentication
                                Most endpoints require JWT authentication. Click the **Authorize** button 
                                and enter your JWT token (obtained from `/api/auth/login`).
                                
                                ### Quick Start
                                1. Register: `POST /api/auth/register`
                                2. Login: `POST /api/auth/login` → receive JWT token
                                3. Authorize: Click lock icon and paste your token
                                4. Explore: Test all authenticated endpoints
                                """)
                        .contact(new Contact()
                                .name("NeoBank Support")
                                .email("support@neobank.com")
                                .url("https://github.com/anisul-islam-prog/neobank-core"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Production Server (OpenAI profile)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("""
                                                Enter your JWT token here.
                                                
                                                **How to get a token:**
                                                1. Call `POST /api/auth/login` with your credentials
                                                2. Copy the `token` value from the response
                                                3. Paste it here (without 'Bearer ' prefix)
                                                
                                                **Example:**
                                                ```
                                                eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                                ```
                                                """)));
    }
}
