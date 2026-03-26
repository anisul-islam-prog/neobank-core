package com.neobank.core.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AccountMapper using JUnit 5.
 * Tests entity-to-domain and domain-to-entity mapping.
 */
@DisplayName("AccountMapper Unit Tests")
class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
    }

    @Nested
    @DisplayName("To Domain Mapping")
    class ToDomainMappingTests {

        @Test
        @DisplayName("Should map AccountEntity to Account")
        void shouldMapAccountEntityToAccount() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            AccountEntity entity = new AccountEntity();
            entity.setId(id);
            entity.setOwnerName(ownerName);
            entity.setBalance(balance);

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account).isNotNull();
            assertThat(account.id()).isEqualTo(id);
            assertThat(account.ownerName()).isEqualTo(ownerName);
            assertThat(account.balance()).isEqualByComparingTo(balance);
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            // When
            Account account = accountMapper.toDomain(null);

            // Then
            assertThat(account).isNull();
        }

        @Test
        @DisplayName("Should map entity with zero balance")
        void shouldMapEntityWithZeroBalance() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("John Doe");
            entity.setBalance(BigDecimal.ZERO);

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should map entity with negative balance")
        void shouldMapEntityWithNegativeBalance() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("John Doe");
            entity.setBalance(new BigDecimal("-100.00"));

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should map entity with balance having 4 decimal places")
        void shouldMapEntityWithBalanceHaving4DecimalPlaces() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("John Doe");
            entity.setBalance(new BigDecimal("123.4567"));

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }
    }

    @Nested
    @DisplayName("To Entity Mapping")
    class ToEntityMappingTests {

        @Test
        @DisplayName("Should map Account to AccountEntity")
        void shouldMapAccountToAccountEntity() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");

            Account account = new Account(id, ownerName, balance);

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getOwnerName()).isEqualTo(ownerName);
            assertThat(entity.getBalance()).isEqualByComparingTo(balance);
        }

        @Test
        @DisplayName("Should return null when account is null")
        void shouldReturnNullWhenAccountIsNull() {
            // When
            AccountEntity entity = accountMapper.toEntity(null);

            // Then
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should map account with zero balance")
        void shouldMapAccountWithZeroBalance() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John Doe", BigDecimal.ZERO);

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should map account with negative balance")
        void shouldMapAccountWithNegativeBalance() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John Doe", new BigDecimal("-100.00"));

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getBalance()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }

        @Test
        @DisplayName("Should map account with balance having 4 decimal places")
        void shouldMapAccountWithBalanceHaving4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John Doe", new BigDecimal("123.4567"));

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getBalance()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }
    }

    @Nested
    @DisplayName("To Entity With Branch Mapping")
    class ToEntityWithBranchMappingTests {

        @Test
        @DisplayName("Should map Account to AccountEntity with branch ID")
        void shouldMapAccountToAccountEntityWithBranchId() {
            // Given
            UUID id = UUID.randomUUID();
            String ownerName = "John Doe";
            BigDecimal balance = new BigDecimal("1000.00");
            UUID branchId = UUID.randomUUID();

            Account account = new Account(id, ownerName, balance);

            // When
            AccountEntity entity = accountMapper.toEntityWithBranch(account, branchId);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getOwnerName()).isEqualTo(ownerName);
            assertThat(entity.getBalance()).isEqualByComparingTo(balance);
            assertThat(entity.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should return null when account is null")
        void shouldReturnNullWhenAccountIsNullForBranchMapping() {
            // Given
            UUID branchId = UUID.randomUUID();

            // When
            AccountEntity entity = accountMapper.toEntityWithBranch(null, branchId);

            // Then
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should map account with null branch ID")
        void shouldMapAccountWithNullBranchId() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John Doe", new BigDecimal("1000.00"));

            // When
            AccountEntity entity = accountMapper.toEntityWithBranch(account, null);

            // Then
            assertThat(entity.getBranchId()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle account with special characters in owner name")
        void shouldHandleAccountWithSpecialCharactersInOwnerName() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John O'Brien-Smith", new BigDecimal("1000.00"));

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getOwnerName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle account with unicode in owner name")
        void shouldHandleAccountWithUnicodeInOwnerName() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "用户 测试", new BigDecimal("1000.00"));

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getOwnerName()).isEqualTo("用户 测试");
        }

        @Test
        @DisplayName("Should handle entity with special characters in owner name")
        void shouldHandleEntityWithSpecialCharactersInOwnerName() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("John O'Brien-Smith");
            entity.setBalance(new BigDecimal("1000.00"));

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.ownerName()).isEqualTo("John O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle entity with unicode in owner name")
        void shouldHandleEntityWithUnicodeInOwnerName() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("用户 测试");
            entity.setBalance(new BigDecimal("1000.00"));

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.ownerName()).isEqualTo("用户 测试");
        }

        @Test
        @DisplayName("Should handle account with very large balance")
        void shouldHandleAccountWithVeryLargeBalance() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "John Doe", new BigDecimal("10000000.00"));

            // When
            AccountEntity entity = accountMapper.toEntity(account);

            // Then
            assertThat(entity.getBalance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle entity with very large balance")
        void shouldHandleEntityWithVeryLargeBalance() {
            // Given
            AccountEntity entity = new AccountEntity();
            entity.setId(UUID.randomUUID());
            entity.setOwnerName("John Doe");
            entity.setBalance(new BigDecimal("10000000.00"));

            // When
            Account account = accountMapper.toDomain(entity);

            // Then
            assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }
    }
}
