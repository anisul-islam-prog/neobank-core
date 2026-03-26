package com.neobank.core.branches;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BranchEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("BranchEntity Unit Tests")
class BranchEntityTest {

    private BranchEntity branchEntity;

    @BeforeEach
    void setUp() {
        branchEntity = new BranchEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            BranchEntity entity = new BranchEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            branchEntity.setId(id);

            // Then
            assertThat(branchEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get code")
        void shouldSetAndGetCode() {
            // Given
            String code = "BR001";

            // When
            branchEntity.setCode(code);

            // Then
            assertThat(branchEntity.getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("Should set and get name")
        void shouldSetGetName() {
            // Given
            String name = "Test Branch";

            // When
            branchEntity.setName(name);

            // Then
            assertThat(branchEntity.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should set and get address line 1")
        void shouldSetAndGetAddressLine1() {
            // Given
            String addressLine1 = "123 Main Street";

            // When
            branchEntity.setAddressLine1(addressLine1);

            // Then
            assertThat(branchEntity.getAddressLine1()).isEqualTo(addressLine1);
        }

        @Test
        @DisplayName("Should set and get address line 2")
        void shouldSetAndGetAddressLine2() {
            // Given
            String addressLine2 = "Suite 100";

            // When
            branchEntity.setAddressLine2(addressLine2);

            // Then
            assertThat(branchEntity.getAddressLine2()).isEqualTo(addressLine2);
        }

        @Test
        @DisplayName("Should set and get city")
        void shouldSetAndGetCity() {
            // Given
            String city = "New York";

            // When
            branchEntity.setCity(city);

            // Then
            assertThat(branchEntity.getCity()).isEqualTo(city);
        }

        @Test
        @DisplayName("Should set and get state")
        void shouldSetAndGetState() {
            // Given
            String state = "NY";

            // When
            branchEntity.setState(state);

            // Then
            assertThat(branchEntity.getState()).isEqualTo(state);
        }

        @Test
        @DisplayName("Should set and get postal code")
        void shouldSetAndGetPostalCode() {
            // Given
            String postalCode = "10001";

            // When
            branchEntity.setPostalCode(postalCode);

            // Then
            assertThat(branchEntity.getPostalCode()).isEqualTo(postalCode);
        }

        @Test
        @DisplayName("Should set and get country")
        void shouldSetAndGetCountry() {
            // Given
            String country = "US";

            // When
            branchEntity.setCountry(country);

            // Then
            assertThat(branchEntity.getCountry()).isEqualTo(country);
        }

        @Test
        @DisplayName("Should set and get phone")
        void shouldSetAndGetPhone() {
            // Given
            String phone = "+1-555-0100";

            // When
            branchEntity.setPhone(phone);

            // Then
            assertThat(branchEntity.getPhone()).isEqualTo(phone);
        }

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            // Given
            String email = "branch@example.com";

            // When
            branchEntity.setEmail(email);

            // Then
            assertThat(branchEntity.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should set and get location metadata")
        void shouldSetAndGetLocationMetadata() {
            // Given
            String locationMetadata = "{\"lat\":40.7128,\"lng\":-74.0060}";

            // When
            branchEntity.setLocationMetadata(locationMetadata);

            // Then
            assertThat(branchEntity.getLocationMetadata()).isEqualTo(locationMetadata);
        }

        @Test
        @DisplayName("Should set and get active flag")
        void shouldSetAndGetActiveFlag() {
            // Given
            boolean active = true;

            // When
            branchEntity.setActive(active);

            // Then
            assertThat(branchEntity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            branchEntity.setCreatedAt(createdAt);

            // Then
            assertThat(branchEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get updated at timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            // Given
            Instant updatedAt = Instant.now();

            // When
            branchEntity.setUpdatedAt(updatedAt);

            // Then
            assertThat(branchEntity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            branchEntity.setId(null);

            // Then
            assertThat(branchEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null code")
        void shouldHandleNullCode() {
            // When
            branchEntity.setCode(null);

            // Then
            assertThat(branchEntity.getCode()).isNull();
        }

        @Test
        @DisplayName("Should handle null name")
        void shouldHandleNullName() {
            // When
            branchEntity.setName(null);

            // Then
            assertThat(branchEntity.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle null address line 1")
        void shouldHandleNullAddressLine1() {
            // When
            branchEntity.setAddressLine1(null);

            // Then
            assertThat(branchEntity.getAddressLine1()).isNull();
        }

        @Test
        @DisplayName("Should handle null address line 2")
        void shouldHandleNullAddressLine2() {
            // When
            branchEntity.setAddressLine2(null);

            // Then
            assertThat(branchEntity.getAddressLine2()).isNull();
        }

        @Test
        @DisplayName("Should handle null city")
        void shouldHandleNullCity() {
            // When
            branchEntity.setCity(null);

            // Then
            assertThat(branchEntity.getCity()).isNull();
        }

        @Test
        @DisplayName("Should handle null state")
        void shouldHandleNullState() {
            // When
            branchEntity.setState(null);

            // Then
            assertThat(branchEntity.getState()).isNull();
        }

        @Test
        @DisplayName("Should handle null postal code")
        void shouldHandleNullPostalCode() {
            // When
            branchEntity.setPostalCode(null);

            // Then
            assertThat(branchEntity.getPostalCode()).isNull();
        }

        @Test
        @DisplayName("Should handle null country")
        void shouldHandleNullCountry() {
            // When
            branchEntity.setCountry(null);

            // Then
            assertThat(branchEntity.getCountry()).isNull();
        }

        @Test
        @DisplayName("Should handle null phone")
        void shouldHandleNullPhone() {
            // When
            branchEntity.setPhone(null);

            // Then
            assertThat(branchEntity.getPhone()).isNull();
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            // When
            branchEntity.setEmail(null);

            // Then
            assertThat(branchEntity.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should handle null location metadata")
        void shouldHandleNullLocationMetadata() {
            // When
            branchEntity.setLocationMetadata(null);

            // Then
            assertThat(branchEntity.getLocationMetadata()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            branchEntity.setCreatedAt(null);

            // Then
            assertThat(branchEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null updated at timestamp")
        void shouldHandleNullUpdatedAtTimestamp() {
            // When
            branchEntity.setUpdatedAt(null);

            // Then
            assertThat(branchEntity.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle inactive branch")
        void shouldHandleInactiveBranch() {
            // When
            branchEntity.setActive(false);

            // Then
            assertThat(branchEntity.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle empty location metadata")
        void shouldHandleEmptyLocationMetadata() {
            // When
            branchEntity.setLocationMetadata("{}");

            // Then
            assertThat(branchEntity.getLocationMetadata()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long branch name")
        void shouldHandleLongBranchName() {
            // When
            branchEntity.setName("a".repeat(200));

            // Then
            assertThat(branchEntity.getName()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle branch name with special characters")
        void shouldHandleBranchNameWithSpecialCharacters() {
            // When
            branchEntity.setName("Branch O'Brien-Smith");

            // Then
            assertThat(branchEntity.getName()).isEqualTo("Branch O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle branch name with unicode")
        void shouldHandleBranchNameWithUnicode() {
            // When
            branchEntity.setName("分行 测试");

            // Then
            assertThat(branchEntity.getName()).isEqualTo("分行 测试");
        }

        @Test
        @DisplayName("Should handle branch code with special characters")
        void shouldHandleBranchCodeWithSpecialCharacters() {
            // When
            branchEntity.setCode("BR-001_A");

            // Then
            assertThat(branchEntity.getCode()).isEqualTo("BR-001_A");
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
            String code = "BR001";
            String name = "Test Branch";
            String addressLine1 = "123 Main Street";
            String addressLine2 = "Suite 100";
            String city = "New York";
            String state = "NY";
            String postalCode = "10001";
            String country = "US";
            String phone = "+1-555-0100";
            String email = "branch@example.com";
            String locationMetadata = "{\"lat\":40.7128,\"lng\":-74.0060}";
            boolean active = true;
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            // When
            branchEntity.setId(id);
            branchEntity.setCode(code);
            branchEntity.setName(name);
            branchEntity.setAddressLine1(addressLine1);
            branchEntity.setAddressLine2(addressLine2);
            branchEntity.setCity(city);
            branchEntity.setState(state);
            branchEntity.setPostalCode(postalCode);
            branchEntity.setCountry(country);
            branchEntity.setPhone(phone);
            branchEntity.setEmail(email);
            branchEntity.setLocationMetadata(locationMetadata);
            branchEntity.setActive(active);
            branchEntity.setCreatedAt(createdAt);
            branchEntity.setUpdatedAt(updatedAt);

            // Then
            assertThat(branchEntity.getId()).isEqualTo(id);
            assertThat(branchEntity.getCode()).isEqualTo(code);
            assertThat(branchEntity.getName()).isEqualTo(name);
            assertThat(branchEntity.getAddressLine1()).isEqualTo(addressLine1);
            assertThat(branchEntity.getAddressLine2()).isEqualTo(addressLine2);
            assertThat(branchEntity.getCity()).isEqualTo(city);
            assertThat(branchEntity.getState()).isEqualTo(state);
            assertThat(branchEntity.getPostalCode()).isEqualTo(postalCode);
            assertThat(branchEntity.getCountry()).isEqualTo(country);
            assertThat(branchEntity.getPhone()).isEqualTo(phone);
            assertThat(branchEntity.getEmail()).isEqualTo(email);
            assertThat(branchEntity.getLocationMetadata()).isEqualTo(locationMetadata);
            assertThat(branchEntity.isActive()).isTrue();
            assertThat(branchEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(branchEntity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            String code = "BR001";
            String name = "Test Branch";

            // When
            branchEntity.setId(id);
            branchEntity.setCode(code);
            branchEntity.setName(name);

            // Then
            assertThat(branchEntity.getId()).isEqualTo(id);
            assertThat(branchEntity.getCode()).isEqualTo(code);
            assertThat(branchEntity.getName()).isEqualTo(name);
            assertThat(branchEntity.getAddressLine1()).isNull();
            assertThat(branchEntity.getCity()).isNull();
            assertThat(branchEntity.isActive()).isTrue(); // Default value
        }
    }
}
