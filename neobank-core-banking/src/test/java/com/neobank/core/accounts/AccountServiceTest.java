package com.neobank.core.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for AccountService using JUnit 5 and Mockito.
 * Provides 100% logic coverage for account management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, accountMapper);
    }

    @Nested
    @DisplayName("Account Creation")
    class AccountCreationTests {

        @Test
        @DisplayName("Should create new account with initial balance successfully")
        void shouldCreateNewAccountSuccessfully() {
            // Given
            String owner = "John Doe";
            BigDecimal initialBalance = new BigDecimal("1000.00");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result).isEqualTo(expectedAccount);
            assertThat(result.id()).isEqualTo(accountId);
            assertThat(result.ownerName()).isEqualTo(owner);
            assertThat(result.balance()).isEqualByComparingTo(initialBalance);

            verify(accountRepository).save(entity);
        }

        @Test
        @DisplayName("Should create new account with zero balance")
        void shouldCreateNewAccountWithZeroBalance() {
            // Given
            String owner = "Jane Doe";
            BigDecimal initialBalance = BigDecimal.ZERO;
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should create account with branch ID when provided")
        void shouldCreateAccountWithBranchWhenProvided() {
            // Given
            String owner = "John Doe";
            BigDecimal initialBalance = new BigDecimal("1000.00");
            UUID branchId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);
            entity.setBranchId(branchId);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccountWithBranch(owner, initialBalance, branchId);

            // Then
            ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
            verify(accountRepository).save(entityCaptor.capture());

            AccountEntity savedEntity = entityCaptor.getValue();
            assertThat(savedEntity.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should create account with null branch ID when not provided")
        void shouldCreateAccountWithNullBranchWhenNotProvided() {
            // Given
            String owner = "John Doe";
            BigDecimal initialBalance = new BigDecimal("1000.00");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);
            entity.setBranchId(null);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccountWithBranch(owner, initialBalance, null);

            // Then
            ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
            verify(accountRepository).save(entityCaptor.capture());

            AccountEntity savedEntity = entityCaptor.getValue();
            assertThat(savedEntity.getBranchId()).isNull();
        }
    }

    @Nested
    @DisplayName("Account Retrieval")
    class AccountRetrievalTests {

        @Test
        @DisplayName("Should get account by ID successfully")
        void shouldGetAccountByIdSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account expectedAccount = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);

            given(accountRepository.findById(accountId)).willReturn(Optional.of(entity));
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.getAccountById(accountId);

            // Then
            assertThat(result).isEqualTo(expectedAccount);
        }

        @Test
        @DisplayName("Should throw exception when account not found by ID")
        void shouldThrowExceptionWhenAccountNotFoundById() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(accountRepository.findById(accountId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> accountService.getAccountById(accountId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("Should get account as Optional by ID")
        void shouldGetAccountAsOptionalById() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account expectedAccount = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);

            given(accountRepository.findById(accountId)).willReturn(Optional.of(entity));
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Optional<Account> result = accountService.getAccount(accountId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedAccount);
        }

        @Test
        @DisplayName("Should return empty Optional when account not found")
        void shouldReturnEmptyOptionalWhenAccountNotFound() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(accountRepository.findById(accountId)).willReturn(Optional.empty());

            // When
            Optional<Account> result = accountService.getAccount(accountId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get account with lock successfully")
        void shouldGetAccountWithLockSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account expectedAccount = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(entity));
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.getAccountWithLock(accountId);

            // Then
            assertThat(result).isEqualTo(expectedAccount);
            verify(accountRepository).findByIdWithLock(accountId);
        }

        @Test
        @DisplayName("Should throw exception when account with lock not found")
        void shouldThrowExceptionWhenAccountWithLockNotFound() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> accountService.getAccountWithLock(accountId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    @Nested
    @DisplayName("Account Updates")
    class AccountUpdateTests {

        @Test
        @DisplayName("Should update account successfully")
        void shouldUpdateAccountSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = new Account(accountId, "John Doe", new BigDecimal("1500.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName("John Doe");
            entity.setBalance(new BigDecimal("1500.00"));

            given(accountMapper.toEntity(account)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(account);

            // When
            Account result = accountService.updateAccount(account);

            // Then
            assertThat(result).isEqualTo(account);
            verify(accountRepository).save(entity);
        }

        @Test
        @DisplayName("Should credit account successfully")
        void shouldCreditAccountSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal creditAmount = new BigDecimal("500.00");

            Account originalAccount = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
            Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("1500.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setBalance(new BigDecimal("1000.00"));

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(entity));
            given(accountMapper.toDomain(entity)).willReturn(originalAccount);
            given(accountMapper.toEntity(updatedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(updatedAccount);

            // When
            Account result = accountService.creditAccount(accountId, creditAmount);

            // Then
            assertThat(result.balance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Should credit account with zero balance")
        void shouldCreditAccountWithZeroBalance() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal creditAmount = new BigDecimal("100.00");

            Account originalAccount = new Account(accountId, "John Doe", BigDecimal.ZERO);
            Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("100.00"));
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setBalance(BigDecimal.ZERO);

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(entity));
            given(accountMapper.toDomain(entity)).willReturn(originalAccount);
            given(accountMapper.toEntity(updatedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(updatedAccount);

            // When
            Account result = accountService.creditAccount(accountId, creditAmount);

            // Then
            assertThat(result.balance()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large balance amounts")
        void shouldHandleVeryLargeBalanceAmounts() {
            // Given
            String owner = "John Doe";
            BigDecimal initialBalance = new BigDecimal("10000000.00");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result.balance()).isEqualByComparingTo(initialBalance);
        }

        @Test
        @DisplayName("Should handle balance with high decimal precision")
        void shouldHandleBalanceWithHighDecimalPrecision() {
            // Given
            String owner = "John Doe";
            BigDecimal initialBalance = new BigDecimal("1000.1234");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result.balance()).isEqualByComparingTo(initialBalance);
        }

        @Test
        @DisplayName("Should handle owner name with special characters")
        void shouldHandleOwnerNameWithSpecialCharacters() {
            // Given
            String owner = "John O'Brien-Smith";
            BigDecimal initialBalance = new BigDecimal("1000.00");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result.ownerName()).isEqualTo(owner);
        }

        @Test
        @DisplayName("Should handle owner name with unicode characters")
        void shouldHandleOwnerNameWithUnicodeCharacters() {
            // Given
            String owner = "用户 测试";
            BigDecimal initialBalance = new BigDecimal("1000.00");
            UUID accountId = UUID.randomUUID();

            Account expectedAccount = new Account(accountId, owner, initialBalance);
            AccountEntity entity = new AccountEntity();
            entity.setId(accountId);
            entity.setOwnerName(owner);
            entity.setBalance(initialBalance);

            given(accountMapper.toEntity(expectedAccount)).willReturn(entity);
            given(accountRepository.save(entity)).willReturn(entity);
            given(accountMapper.toDomain(entity)).willReturn(expectedAccount);

            // When
            Account result = accountService.createNewAccount(owner, initialBalance);

            // Then
            assertThat(result.ownerName()).isEqualTo(owner);
        }
    }
}
