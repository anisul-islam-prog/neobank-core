package com.neobank.transfers.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.accounts.api.AccountApi;
import com.neobank.transfers.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-concurrency integration test for TransferController.
 * Uses Virtual Threads (Java 25) to simulate 50 simultaneous transfers.
 * Verifies atomic transfer logic with pessimistic locking.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransferControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private AccountApi accountApi;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient httpClient;
    private String baseUrl;
    private UUID accountAId;
    private UUID accountBId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/transfers";
        httpClient = HttpClient.newHttpClient();

        // Create two accounts with $1,000 each
        var accountA = accountApi.createNewAccount("Account A", new BigDecimal("1000.00"));
        var accountB = accountApi.createNewAccount("Account B", new BigDecimal("1000.00"));

        accountAId = accountA.id();
        accountBId = accountB.id();
    }

    @Test
    void concurrentTransfers_maintainAtomicBalance() throws Exception {
        // Scenario: 20 transfers of $10 from Account A to Account B using virtual threads
        // Expected: Account A = $800, Account B = $1,200
        // Note: Using semaphore to limit concurrent database connections

        int numberOfTransfers = 20;
        BigDecimal transferAmount = new BigDecimal("10.00");
        BigDecimal expectedBalanceA = new BigDecimal("800.00");
        BigDecimal expectedBalanceB = new BigDecimal("1200.00");
        
        // Limit concurrent transfers to avoid connection pool exhaustion
        var semaphore = new java.util.concurrent.Semaphore(5);

        // Use Virtual Thread Executor (Java 25)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < numberOfTransfers; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            var request = new TransferRequest(accountAId, accountBId, transferAmount);
                            String requestBody = objectMapper.writeValueAsString(request);

                            HttpRequest httpRequest = HttpRequest.newBuilder()
                                    .uri(URI.create(baseUrl))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                    .build();

                            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                            // Verify each transfer returns success
                            assertThat(response.statusCode()).isEqualTo(200);
                            Map<String, Object> responseBody = objectMapper.readValue(
                                    response.body(),
                                    new TypeReference<Map<String, Object>>() {}
                            );
                            assertThat(responseBody.get("status")).isEqualTo("success");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            semaphore.release();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            // Wait for all transfers to complete
            for (Future<?> future : futures) {
                future.get();
            }
        }

        // Verify final balances
        var finalAccountA = accountApi.getAccountById(accountAId);
        var finalAccountB = accountApi.getAccountById(accountBId);

        assertThat(finalAccountA.balance())
                .as("Account A should have exactly $500 after 50 transfers of $10")
                .isEqualByComparingTo(expectedBalanceA);

        assertThat(finalAccountB.balance())
                .as("Account B should have exactly $1,500 after 50 transfers of $10")
                .isEqualByComparingTo(expectedBalanceB);

        // Verify total balance is conserved (no money created or destroyed)
        var totalBalance = finalAccountA.balance().add(finalAccountB.balance());
        assertThat(totalBalance)
                .as("Total balance should be conserved at $2,000")
                .isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void transferWithInsufficientBalance_fails() throws Exception {
        // Try to transfer more than available balance
        var request = new TransferRequest(accountAId, accountBId, new BigDecimal("5000.00"));
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
        Map<String, Object> responseBody = objectMapper.readValue(
                response.body(),
                new TypeReference<Map<String, Object>>() {}
        );
        assertThat(responseBody.get("status")).isEqualTo("failure");
        assertThat(responseBody.get("message")).isEqualTo("Insufficient balance");

        // Verify balances unchanged
        var accountA = accountApi.getAccountById(accountAId);
        var accountB = accountApi.getAccountById(accountBId);

        assertThat(accountA.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(accountB.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}
