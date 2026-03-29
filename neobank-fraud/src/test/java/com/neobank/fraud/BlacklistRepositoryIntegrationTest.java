package com.neobank.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for BlacklistRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("BlacklistRepository Integration Tests")
class BlacklistRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private BlacklistRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve blacklist entry by ID")
        void shouldSaveAndRetrieveBlacklistEntryById() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);
            BlacklistEntity retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getEntityType()).isEqualTo(blacklistEntity.getEntityType());
            assertThat(retrieved.getEntityValue()).isEqualTo(blacklistEntity.getEntityValue());
        }

        @Test
        @DisplayName("Should save blacklist entry with active flag")
        void shouldSaveBlacklistEntryWithActiveFlag() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity.setActive(true);

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should save blacklist entry with inactive flag")
        void shouldSaveBlacklistEntryWithInactiveFlag() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity.setActive(false);

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should save blacklist entry with expiry date")
        void shouldSaveBlacklistEntryWithExpiryDate() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            Instant expiresAt = Instant.now().plusSeconds(86400);
            blacklistEntity.setExpiresAt(expiresAt);

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("Should save blacklist entry with metadata JSON")
        void shouldSaveBlacklistEntryWithMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity.setMetadataJson("{\"source\":\"automated\"}");

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{\"source\":\"automated\"}");
        }
    }

    @Nested
    @DisplayName("Find By Entity Type and Value")
    class FindByEntityTypeAndValueTests {

        @Test
        @DisplayName("Should find blacklist entry by entity type and value")
        void shouldFindBlacklistEntryByEntityTypeAndValue() {
            // Given
            String entityValue = "192.168.1.100";
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            blacklistEntity.setEntityValue(entityValue);
            repository.save(blacklistEntity);

            // When
            Optional<BlacklistEntity> result = repository.findByEntityTypeAndEntityValue(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    entityValue
            );

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEntityValue()).isEqualTo(entityValue);
        }

        @Test
        @DisplayName("Should return empty when blacklist entry not found")
        void shouldReturnEmptyWhenBlacklistEntryNotFound() {
            // Given
            String nonExistentValue = "192.168.1.200";

            // When
            Optional<BlacklistEntity> result = repository.findByEntityTypeAndEntityValue(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS,
                    nonExistentValue
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Is Blacklisted Checks")
    class IsBlacklistedChecksTests {

        @Test
        @DisplayName("Should return true when IP address is blacklisted")
        void shouldReturnTrueWhenIpAddressIsBlacklisted() {
            // Given
            String ipAddress = "192.168.1.100";
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            blacklistEntity.setEntityValue(ipAddress);
            blacklistEntity.setActive(true);
            repository.save(blacklistEntity);

            // When
            boolean isBlacklisted = repository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should return false when IP address is not blacklisted")
        void shouldReturnFalseWhenIpAddressIsNotBlacklisted() {
            // Given
            String ipAddress = "192.168.1.100";

            // When
            boolean isBlacklisted = repository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isFalse();
        }

        @Test
        @DisplayName("Should return false when IP address blacklist entry is inactive")
        void shouldReturnFalseWhenIpAddressBlacklistEntryIsInactive() {
            // Given
            String ipAddress = "192.168.1.100";
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            blacklistEntity.setEntityValue(ipAddress);
            blacklistEntity.setActive(false);
            repository.save(blacklistEntity);

            // When
            boolean isBlacklisted = repository.isIpAddressBlacklisted(ipAddress);

            // Then
            assertThat(isBlacklisted).isFalse();
        }

        @Test
        @DisplayName("Should return true when account ID is blacklisted")
        void shouldReturnTrueWhenAccountIdIsBlacklisted() {
            // Given
            String accountId = UUID.randomUUID().toString();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            blacklistEntity.setEntityValue(accountId);
            blacklistEntity.setActive(true);
            repository.save(blacklistEntity);

            // When
            boolean isBlacklisted = repository.isAccountIdBlacklisted(accountId);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should return false when account ID is not blacklisted")
        void shouldReturnFalseWhenAccountIdIsNotBlacklisted() {
            // Given
            String accountId = UUID.randomUUID().toString();

            // When
            boolean isBlacklisted = repository.isAccountIdBlacklisted(accountId);

            // Then
            assertThat(isBlacklisted).isFalse();
        }

        @Test
        @DisplayName("Should return true when user ID is blacklisted")
        void shouldReturnTrueWhenUserIdIsBlacklisted() {
            // Given
            String userId = UUID.randomUUID().toString();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.USER_ID);
            blacklistEntity.setEntityValue(userId);
            blacklistEntity.setActive(true);
            repository.save(blacklistEntity);

            // When
            boolean isBlacklisted = repository.isUserIdBlacklisted(userId);

            // Then
            assertThat(isBlacklisted).isTrue();
        }
    }

    @Nested
    @DisplayName("Find By Entity Type")
    class FindByEntityTypeTests {

        @Test
        @DisplayName("Should find all active IP address blacklist entries")
        void shouldFindAllActiveIpAddressBlacklistEntries() {
            // Given
            BlacklistEntity ip1 = createTestBlacklistEntity(UUID.randomUUID());
            ip1.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            ip1.setEntityValue("192.168.1.1");
            ip1.setActive(true);
            repository.save(ip1);

            BlacklistEntity ip2 = createTestBlacklistEntity(UUID.randomUUID());
            ip2.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            ip2.setEntityValue("192.168.1.2");
            ip2.setActive(true);
            repository.save(ip2);

            BlacklistEntity inactive = createTestBlacklistEntity(UUID.randomUUID());
            inactive.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            inactive.setEntityValue("192.168.1.3");
            inactive.setActive(false);
            repository.save(inactive);

            // When
            List<BlacklistEntity> results = repository.findByEntityTypeAndActiveTrue(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS
            );

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(e -> e.getEntityType() == BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            assertThat(results).allMatch(BlacklistEntity::isActive);
        }

        @Test
        @DisplayName("Should find all active blacklist entries")
        void shouldFindAllActiveBlacklistEntries() {
            // Given
            BlacklistEntity active1 = createTestBlacklistEntity(UUID.randomUUID());
            active1.setActive(true);
            repository.save(active1);

            BlacklistEntity active2 = createTestBlacklistEntity(UUID.randomUUID());
            active2.setActive(true);
            repository.save(active2);

            BlacklistEntity inactive = createTestBlacklistEntity(UUID.randomUUID());
            inactive.setActive(false);
            repository.save(inactive);

            // When
            List<BlacklistEntity> results = repository.findByActiveTrueOrderByCreatedAtDesc();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(BlacklistEntity::isActive);
        }

        @Test
        @DisplayName("Should find all high severity blacklist entries")
        void shouldFindAllHighSeverityBlacklistEntries() {
            // Given
            BlacklistEntity high1 = createTestBlacklistEntity(UUID.randomUUID());
            high1.setSeverity(BlacklistEntity.BlacklistSeverity.HIGH);
            high1.setActive(true);
            repository.save(high1);

            BlacklistEntity high2 = createTestBlacklistEntity(UUID.randomUUID());
            high2.setSeverity(BlacklistEntity.BlacklistSeverity.HIGH);
            high2.setActive(true);
            repository.save(high2);

            BlacklistEntity critical = createTestBlacklistEntity(UUID.randomUUID());
            critical.setSeverity(BlacklistEntity.BlacklistSeverity.CRITICAL);
            critical.setActive(true);
            repository.save(critical);

            // When
            List<BlacklistEntity> results = repository.findBySeverityAndActiveTrue(
                    BlacklistEntity.BlacklistSeverity.HIGH
            );

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(e -> e.getSeverity() == BlacklistEntity.BlacklistSeverity.HIGH);
        }
    }

    @Nested
    @DisplayName("Count Operations")
    class CountOperationsTests {

        @Test
        @DisplayName("Should count active blacklist entries")
        void shouldCountActiveBlacklistEntries() {
            // Given
            BlacklistEntity active1 = createTestBlacklistEntity(UUID.randomUUID());
            active1.setActive(true);
            repository.save(active1);

            BlacklistEntity active2 = createTestBlacklistEntity(UUID.randomUUID());
            active2.setActive(true);
            repository.save(active2);

            BlacklistEntity inactive = createTestBlacklistEntity(UUID.randomUUID());
            inactive.setActive(false);
            repository.save(inactive);

            // When
            long count = repository.countByActiveTrue();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count active blacklist entries by entity type")
        void shouldCountActiveBlacklistEntriesByEntityType() {
            // Given
            BlacklistEntity ip1 = createTestBlacklistEntity(UUID.randomUUID());
            ip1.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            ip1.setActive(true);
            repository.save(ip1);

            BlacklistEntity ip2 = createTestBlacklistEntity(UUID.randomUUID());
            ip2.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            ip2.setActive(true);
            repository.save(ip2);

            BlacklistEntity account = createTestBlacklistEntity(UUID.randomUUID());
            account.setEntityType(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            account.setActive(true);
            repository.save(account);

            // When
            long ipCount = repository.countByEntityTypeAndActiveTrue(
                    BlacklistEntity.BlacklistEntityType.IP_ADDRESS
            );

            // Then
            assertThat(ipCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return 0 when no active blacklist entries")
        void shouldReturn0WhenNoActiveBlacklistEntries() {
            // When
            long count = repository.countByActiveTrue();

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity.setMetadataJson(null);

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity.setMetadataJson("{}");

            // When
            BlacklistEntity saved = repository.save(blacklistEntity);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle IPv6 address")
        void shouldHandleIpv6Address() {
            // Given
            String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(UUID.randomUUID());
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            blacklistEntity.setEntityValue(ipv6);
            blacklistEntity.setActive(true);
            repository.save(blacklistEntity);

            // When
            boolean isBlacklisted = repository.isIpAddressBlacklisted(ipv6);

            // Then
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @DisplayName("Should handle deleting blacklist entries")
        void shouldHandleDeletingBlacklistEntries() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity blacklistEntity = createTestBlacklistEntity(id);
            blacklistEntity = repository.save(blacklistEntity);

            // When
            repository.delete(blacklistEntity);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all blacklist entries")
        void shouldHandleDeletingAllBlacklistEntries() {
            // Given
            List<BlacklistEntity> blacklistEntities = List.of(
                    createTestBlacklistEntity(UUID.randomUUID()),
                    createTestBlacklistEntity(UUID.randomUUID()),
                    createTestBlacklistEntity(UUID.randomUUID())
            );
            repository.saveAll(blacklistEntities);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }
    }

    /**
     * Helper method to create a test blacklist entity.
     */
    private BlacklistEntity createTestBlacklistEntity(UUID id) {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(id);
        entity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
        entity.setEntityValue("192.168.1." + (int)(Math.random() * 255));
        entity.setReason("Test blacklist entry");
        entity.setSeverity(BlacklistEntity.BlacklistSeverity.HIGH);
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
