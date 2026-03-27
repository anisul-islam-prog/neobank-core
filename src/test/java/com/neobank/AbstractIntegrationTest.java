package com.neobank;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.time.Duration;

/**
 * Abstract base class for integration tests using Testcontainers.
 * Provides Docker availability check and PostgreSQL container configuration.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    /**
     * Shared PostgreSQL container for all integration tests.
     * Uses @ServiceConnection for automatic Spring Boot DataSource configuration.
     */
    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
            .withReuse(true);

    /**
     * Check Docker availability before running tests.
     * Fails fast with helpful error message if Docker is not available.
     */
    @BeforeAll
    static void checkDockerAvailability() {
        try {
            DockerClientFactory.instance().client();
            log.info("Docker is available - proceeding with integration tests");
        } catch (Exception e) {
            String errorMessage = buildDockerErrorMessage(e);
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    /**
     * Build helpful error message when Docker is not available.
     */
    private static String buildDockerErrorMessage(Exception cause) {
        StringBuilder message = new StringBuilder();
        message.append("\n\n");
        message.append("═══════════════════════════════════════════════════════════\n");
        message.append("           DOCKER NOT AVAILABLE - TESTS CANNOT RUN\n");
        message.append("═══════════════════════════════════════════════════════════\n\n");
        message.append("Error: ").append(cause.getMessage()).append("\n\n");
        message.append("Please check the following:\n\n");
        message.append("1. Docker Desktop is running:\n");
        message.append("   - macOS/Windows: Open Docker Desktop application\n");
        message.append("   - Linux: Run 'sudo systemctl start docker'\n\n");
        message.append("2. Check DOCKER_HOST environment variable:\n");
        message.append("   - Current value: ").append(System.getenv("DOCKER_HOST")).append("\n");
        message.append("   - Default: unix:///var/run/docker.sock (Linux/Mac)\n");
        message.append("   - Default: npipe:////./pipe/docker_engine (Windows)\n\n");
        message.append("3. Verify Docker socket permissions:\n");
        message.append("   - Run: ls -la /var/run/docker.sock\n");
        message.append("   - If needed: sudo chmod 666 /var/run/docker.sock\n\n");
        message.append("4. Add your user to the docker group (Linux):\n");
        message.append("   - Run: sudo usermod -aG docker $USER\n");
        message.append("   - Then log out and log back in\n\n");
        message.append("5. For CI/CD environments:\n");
        message.append("   - Ensure Docker-in-Docker (dind) is enabled\n");
        message.append("   - Or use Docker socket binding: -v /var/run/docker.sock:/var/run/docker.sock\n\n");
        message.append("═══════════════════════════════════════════════════════════\n");
        return message.toString();
    }

    /**
     * Get the JDBC URL for the PostgreSQL container.
     * Can be used for custom database configuration if needed.
     */
    protected static String getJdbcUrl() {
        return postgres.getJdbcUrl();
    }

    /**
     * Get the username for the PostgreSQL container.
     */
    protected static String getUsername() {
        return postgres.getUsername();
    }

    /**
     * Get the password for the PostgreSQL container.
     */
    protected static String getPassword() {
        return postgres.getPassword();
    }
}
