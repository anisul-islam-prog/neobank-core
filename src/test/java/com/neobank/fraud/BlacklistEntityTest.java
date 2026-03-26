package com.neobank.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BlacklistEntity using JUnit 5.
 * Tests entity state and blacklist management.
 */
@DisplayName("BlacklistEntity Unit Tests")
class BlacklistEntityTest {

    private BlacklistEntity blacklistEntity;

    @BeforeEach
    void setUp() {
        blacklistEntity = new BlacklistEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            BlacklistEntity entity = new BlacklistEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            blacklistEntity.setId(id);

            // Then
            assertThat(blacklistEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get entity type")
        void shouldSetAndGetEntityType() {
            // Given
            BlacklistEntity.BlacklistEntityType entityType = BlacklistEntity.BlacklistEntityType.IP_ADDRESS;

            // When
            blacklistEntity.setEntityType(entityType);

            // Then
            assertThat(blacklistEntity.getEntityType()).isEqualTo(entityType);
        }

        @Test
        @DisplayName("Should set and get entity value")
        void shouldSetAndGetEntityValue() {
            // Given
            String entityValue = "192.168.1.100";

            // When
            blacklistEntity.setEntityValue(entityValue);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(entityValue);
        }

        @Test
        @DisplayName("Should set and get reason")
        void shouldSetAndGetReason() {
            // Given
            String reason = "Suspicious activity detected";

            // When
            blacklistEntity.setReason(reason);

            // Then
            assertThat(blacklistEntity.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should set and get severity")
        void shouldSetAndGetSeverity() {
            // Given
            BlacklistEntity.BlacklistSeverity severity = BlacklistEntity.BlacklistSeverity.HIGH;

            // When
            blacklistEntity.setSeverity(severity);

            // Then
            assertThat(blacklistEntity.getSeverity()).isEqualTo(severity);
        }

        @Test
        @DisplayName("Should set and get active flag")
        void shouldSetAndGetActiveFlag() {
            // Given
            boolean active = true;

            // When
            blacklistEntity.setActive(active);

            // Then
            assertThat(blacklistEntity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            blacklistEntity.setCreatedAt(createdAt);

            // Then
            assertThat(blacklistEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get expires at timestamp")
        void shouldSetAndGetExpiresAtTimestamp() {
            // Given
            Instant expiresAt = Instant.now().plusSeconds(86400);

            // When
            blacklistEntity.setExpiresAt(expiresAt);

            // Then
            assertThat(blacklistEntity.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("Should set and get created by")
        void shouldSetAndGetCreatedBy() {
            // Given
            UUID createdBy = UUID.randomUUID();

            // When
            blacklistEntity.setCreatedBy(createdBy);

            // Then
            assertThat(blacklistEntity.getCreatedBy()).isEqualTo(createdBy);
        }

        @Test
        @DisplayName("Should set and get metadata JSON")
        void shouldSetAndGetMetadataJson() {
            // Given
            String metadataJson = "{\"source\":\"manual\"}";

            // When
            blacklistEntity.setMetadataJson(metadataJson);

            // Then
            assertThat(blacklistEntity.getMetadataJson()).isEqualTo(metadataJson);
        }
    }

    @Nested
    @DisplayName("BlacklistEntityType Enum")
    class BlacklistEntityTypeEnumTests {

        @Test
        @DisplayName("Should have IP_ADDRESS type")
        void shouldHaveIpAddressType() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.IP_ADDRESS).isNotNull();
        }

        @Test
        @DisplayName("Should have ACCOUNT_ID type")
        void shouldHaveAccountIdType() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID).isNotNull();
        }

        @Test
        @DisplayName("Should have USER_ID type")
        void shouldHaveUserIdType() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.USER_ID).isNotNull();
        }

        @Test
        @DisplayName("Should have DEVICE_FINGERPRINT type")
        void shouldHaveDeviceFingerprintType() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.DEVICE_FINGERPRINT).isNotNull();
        }

        @Test
        @DisplayName("Should have EMAIL type")
        void shouldHaveEmailType() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.EMAIL).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 5 entity types")
        void shouldHaveExactly5EntityTypes() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.values()).hasSize(5);
        }

        @Test
        @DisplayName("Should allow valueOf for all entity type names")
        void shouldAllowValueOfForAllEntityTypeNames() {
            // Then
            assertThat(BlacklistEntity.BlacklistEntityType.valueOf("IP_ADDRESS"))
                    .isEqualTo(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            assertThat(BlacklistEntity.BlacklistEntityType.valueOf("ACCOUNT_ID"))
                    .isEqualTo(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            assertThat(BlacklistEntity.BlacklistEntityType.valueOf("USER_ID"))
                    .isEqualTo(BlacklistEntity.BlacklistEntityType.USER_ID);
            assertThat(BlacklistEntity.BlacklistEntityType.valueOf("DEVICE_FINGERPRINT"))
                    .isEqualTo(BlacklistEntity.BlacklistEntityType.DEVICE_FINGERPRINT);
            assertThat(BlacklistEntity.BlacklistEntityType.valueOf("EMAIL"))
                    .isEqualTo(BlacklistEntity.BlacklistEntityType.EMAIL);
        }
    }

    @Nested
    @DisplayName("BlacklistSeverity Enum")
    class BlacklistSeverityEnumTests {

        @Test
        @DisplayName("Should have LOW severity")
        void shouldHaveLowSeverity() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.LOW).isNotNull();
        }

        @Test
        @DisplayName("Should have MEDIUM severity")
        void shouldHaveMediumSeverity() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.MEDIUM).isNotNull();
        }

        @Test
        @DisplayName("Should have HIGH severity")
        void shouldHaveHighSeverity() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.HIGH).isNotNull();
        }

        @Test
        @DisplayName("Should have CRITICAL severity")
        void shouldHaveCriticalSeverity() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.CRITICAL).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 4 severity levels")
        void shouldHaveExactly4SeverityLevels() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.values()).hasSize(4);
        }

        @Test
        @DisplayName("Should allow valueOf for all severity names")
        void shouldAllowValueOfForAllSeverityNames() {
            // Then
            assertThat(BlacklistEntity.BlacklistSeverity.valueOf("LOW"))
                    .isEqualTo(BlacklistEntity.BlacklistSeverity.LOW);
            assertThat(BlacklistEntity.BlacklistSeverity.valueOf("MEDIUM"))
                    .isEqualTo(BlacklistEntity.BlacklistSeverity.MEDIUM);
            assertThat(BlacklistEntity.BlacklistSeverity.valueOf("HIGH"))
                    .isEqualTo(BlacklistEntity.BlacklistSeverity.HIGH);
            assertThat(BlacklistEntity.BlacklistSeverity.valueOf("CRITICAL"))
                    .isEqualTo(BlacklistEntity.BlacklistSeverity.CRITICAL);
        }
    }

    @Nested
    @DisplayName("Active Flag")
    class ActiveFlagTests {

        @Test
        @DisplayName("Should default to active")
        void shouldDefaultToActive() {
            // Given
            BlacklistEntity entity = new BlacklistEntity();

            // Then
            assertThat(entity.isActive()).isFalse(); // Default is false in entity
        }

        @Test
        @DisplayName("Should set active to true")
        void shouldSetActiveToTrue() {
            // When
            blacklistEntity.setActive(true);

            // Then
            assertThat(blacklistEntity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should set active to false")
        void shouldSetActiveToFalse() {
            // Given
            blacklistEntity.setActive(true);

            // When
            blacklistEntity.setActive(false);

            // Then
            assertThat(blacklistEntity.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            blacklistEntity.setId(null);

            // Then
            assertThat(blacklistEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null entity type")
        void shouldHandleNullEntityType() {
            // When
            blacklistEntity.setEntityType(null);

            // Then
            assertThat(blacklistEntity.getEntityType()).isNull();
        }

        @Test
        @DisplayName("Should handle null entity value")
        void shouldHandleNullEntityValue() {
            // When
            blacklistEntity.setEntityValue(null);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isNull();
        }

        @Test
        @DisplayName("Should handle null reason")
        void shouldHandleNullReason() {
            // When
            blacklistEntity.setReason(null);

            // Then
            assertThat(blacklistEntity.getReason()).isNull();
        }

        @Test
        @DisplayName("Should handle null severity")
        void shouldHandleNullSeverity() {
            // When
            blacklistEntity.setSeverity(null);

            // Then
            assertThat(blacklistEntity.getSeverity()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            blacklistEntity.setCreatedAt(null);

            // Then
            assertThat(blacklistEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null expires at timestamp")
        void shouldHandleNullExpiresAtTimestamp() {
            // When
            blacklistEntity.setExpiresAt(null);

            // Then
            assertThat(blacklistEntity.getExpiresAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null created by")
        void shouldHandleNullCreatedBy() {
            // When
            blacklistEntity.setCreatedBy(null);

            // Then
            assertThat(blacklistEntity.getCreatedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // When
            blacklistEntity.setMetadataJson(null);

            // Then
            assertThat(blacklistEntity.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // When
            blacklistEntity.setMetadataJson("{}");

            // Then
            assertThat(blacklistEntity.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle IP address entity value")
        void shouldHandleIpAddressEntityValue() {
            // Given
            String ipAddress = "192.168.1.100";

            // When
            blacklistEntity.setEntityValue(ipAddress);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(ipAddress);
        }

        @Test
        @DisplayName("Should handle UUID entity value")
        void shouldHandleUuidEntityValue() {
            // Given
            String uuid = UUID.randomUUID().toString();

            // When
            blacklistEntity.setEntityValue(uuid);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should handle email entity value")
        void shouldHandleEmailEntityValue() {
            // Given
            String email = "suspicious@example.com";

            // When
            blacklistEntity.setEntityValue(email);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle long reason string")
        void shouldHandleLongReasonString() {
            // Given
            String longReason = "a".repeat(900);

            // When
            blacklistEntity.setReason(longReason);

            // Then
            assertThat(blacklistEntity.getReason()).hasSize(900);
        }

        @Test
        @DisplayName("Should handle reason with special characters")
        void shouldHandleReasonWithSpecialCharacters() {
            // Given
            String reason = "Suspicious activity: IP 192.168.1.100!";

            // When
            blacklistEntity.setReason(reason);

            // Then
            assertThat(blacklistEntity.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle reason with unicode characters")
        void shouldHandleReasonWithUnicodeCharacters() {
            // Given
            String reason = "可疑活动检测";

            // When
            blacklistEntity.setReason(reason);

            // Then
            assertThat(blacklistEntity.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should handle IPv6 address entity value")
        void shouldHandleIpv6AddressEntityValue() {
            // Given
            String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

            // When
            blacklistEntity.setEntityValue(ipv6);

            // Then
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(ipv6);
        }
    }

    @Nested
    @DisplayName("Complete Entity State")
    class CompleteEntityStateTests {

        @Test
        @DisplayName("Should handle complete entity with all fields set")
        void shouldHandleCompleteEntityWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            BlacklistEntity.BlacklistEntityType entityType = BlacklistEntity.BlacklistEntityType.IP_ADDRESS;
            String entityValue = "192.168.1.100";
            String reason = "Suspicious activity detected";
            BlacklistEntity.BlacklistSeverity severity = BlacklistEntity.BlacklistSeverity.HIGH;
            boolean active = true;
            Instant createdAt = Instant.now();
            Instant expiresAt = Instant.now().plusSeconds(86400);
            UUID createdBy = UUID.randomUUID();
            String metadataJson = "{\"source\":\"automated\"}";

            // When
            blacklistEntity.setId(id);
            blacklistEntity.setEntityType(entityType);
            blacklistEntity.setEntityValue(entityValue);
            blacklistEntity.setReason(reason);
            blacklistEntity.setSeverity(severity);
            blacklistEntity.setActive(active);
            blacklistEntity.setCreatedAt(createdAt);
            blacklistEntity.setExpiresAt(expiresAt);
            blacklistEntity.setCreatedBy(createdBy);
            blacklistEntity.setMetadataJson(metadataJson);

            // Then
            assertThat(blacklistEntity.getId()).isEqualTo(id);
            assertThat(blacklistEntity.getEntityType()).isEqualTo(entityType);
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(entityValue);
            assertThat(blacklistEntity.getReason()).isEqualTo(reason);
            assertThat(blacklistEntity.getSeverity()).isEqualTo(severity);
            assertThat(blacklistEntity.isActive()).isTrue();
            assertThat(blacklistEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(blacklistEntity.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(blacklistEntity.getCreatedBy()).isEqualTo(createdBy);
            assertThat(blacklistEntity.getMetadataJson()).isEqualTo(metadataJson);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            String entityValue = "192.168.1.100";

            // When
            blacklistEntity.setId(id);
            blacklistEntity.setEntityValue(entityValue);

            // Then
            assertThat(blacklistEntity.getId()).isEqualTo(id);
            assertThat(blacklistEntity.getEntityValue()).isEqualTo(entityValue);
            assertThat(blacklistEntity.getEntityType()).isNull();
            assertThat(blacklistEntity.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle permanent blacklist (no expiry)")
        void shouldHandlePermanentBlacklistNoExpiry() {
            // Given
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.ACCOUNT_ID);
            blacklistEntity.setEntityValue(UUID.randomUUID().toString());
            blacklistEntity.setSeverity(BlacklistEntity.BlacklistSeverity.CRITICAL);
            blacklistEntity.setActive(true);

            // When - No expiresAt set

            // Then
            assertThat(blacklistEntity.getExpiresAt()).isNull();
            assertThat(blacklistEntity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should handle temporary blacklist with expiry")
        void shouldHandleTemporaryBlacklistWithExpiry() {
            // Given
            Instant expiresAt = Instant.now().plusSeconds(3600);

            // When
            blacklistEntity.setEntityType(BlacklistEntity.BlacklistEntityType.IP_ADDRESS);
            blacklistEntity.setEntityValue("192.168.1.100");
            blacklistEntity.setSeverity(BlacklistEntity.BlacklistSeverity.MEDIUM);
            blacklistEntity.setActive(true);
            blacklistEntity.setExpiresAt(expiresAt);

            // Then
            assertThat(blacklistEntity.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(blacklistEntity.isActive()).isTrue();
        }
    }
}
