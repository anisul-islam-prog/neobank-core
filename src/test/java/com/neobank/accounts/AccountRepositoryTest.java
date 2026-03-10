package com.neobank.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Test
    void shouldSaveAndRetrieveAccountEntity() {
        // Given
        AccountEntity entity = new AccountEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerName("John Doe");
        entity.setBalance(new BigDecimal("1000.00"));

        // When
        AccountEntity saved = accountRepository.save(entity);
        Optional<AccountEntity> retrieved = accountRepository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals("John Doe", retrieved.get().getOwnerName());
        assertEquals(0, retrieved.get().getBalance().compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void shouldFindByOwnerNameContaining() {
        // Given
        AccountEntity entity1 = new AccountEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setOwnerName("John Smith");
        entity1.setBalance(new BigDecimal("1000.00"));

        AccountEntity entity2 = new AccountEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setOwnerName("Jane Smith");
        entity2.setBalance(new BigDecimal("2000.00"));

        AccountEntity entity3 = new AccountEntity();
        entity3.setId(UUID.randomUUID());
        entity3.setOwnerName("Bob Jones");
        entity3.setBalance(new BigDecimal("3000.00"));

        accountRepository.saveAll(List.of(entity1, entity2, entity3));

        // When
        Streamable<AccountEntity> results = accountRepository.findByOwnerNameContaining("Smith");

        // Then
        List<AccountEntity> resultList = results.toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.stream().allMatch(e -> e.getOwnerName().contains("Smith")));
    }

    @Test
    void shouldFindByBalanceGreaterThan() {
        // Given
        AccountEntity entity1 = new AccountEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setOwnerName("John Doe");
        entity1.setBalance(new BigDecimal("1000.00"));

        AccountEntity entity2 = new AccountEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setOwnerName("Jane Doe");
        entity2.setBalance(new BigDecimal("5000.00"));

        AccountEntity entity3 = new AccountEntity();
        entity3.setId(UUID.randomUUID());
        entity3.setOwnerName("Bob Smith");
        entity3.setBalance(new BigDecimal("2500.00"));

        accountRepository.saveAll(List.of(entity1, entity2, entity3));

        // When
        List<AccountEntity> results = accountRepository.findByBalanceGreaterThan(new BigDecimal("2000.00"));

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(e -> e.getBalance().compareTo(new BigDecimal("2000.00")) > 0));
    }

    @Test
    void shouldPersistTransactionHistory() {
        // Given
        AccountEntity entity = new AccountEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerName("John Doe");
        entity.setBalance(new BigDecimal("1000.00"));
        entity.setTransactionHistory(List.of("DEPOSIT: 1000.00", "WITHDRAW: 200.00"));

        // When
        AccountEntity saved = accountRepository.save(entity);
        accountRepository.flush();
        Optional<AccountEntity> retrieved = accountRepository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(2, retrieved.get().getTransactionHistory().size());
        assertTrue(retrieved.get().getTransactionHistory().contains("DEPOSIT: 1000.00"));
        assertTrue(retrieved.get().getTransactionHistory().contains("WITHDRAW: 200.00"));
    }

    @Test
    void shouldConvertDomainRecordToEntity() {
        // Given
        Account account = new Account(UUID.randomUUID(), "John Doe", new BigDecimal("1000.00"));

        // When
        AccountEntity entity = accountMapper.toEntity(account);

        // Then
        assertNotNull(entity);
        assertEquals(account.id(), entity.getId());
        assertEquals(account.ownerName(), entity.getOwnerName());
        assertEquals(0, account.balance().compareTo(entity.getBalance()));
        assertNotNull(entity.getTransactionHistory());
    }

    @Test
    void shouldConvertEntityToDomainRecord() {
        // Given
        AccountEntity entity = new AccountEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerName("Jane Doe");
        entity.setBalance(new BigDecimal("2500.00"));
        entity.setTransactionHistory(List.of("DEPOSIT: 2500.00"));

        // When
        Account account = accountMapper.toDomain(entity);

        // Then
        assertNotNull(account);
        assertEquals(entity.getId(), account.id());
        assertEquals(entity.getOwnerName(), account.ownerName());
        assertEquals(0, entity.getBalance().compareTo(account.balance()));
    }

    @Test
    void shouldRoundTripViaMapperAndRepository() {
        // Given
        Account original = new Account(UUID.randomUUID(), "Test User", new BigDecimal("5000.00"));

        // When - Convert to entity, save, retrieve, convert back
        AccountEntity entity = accountMapper.toEntity(original);
        entity.setTransactionHistory(List.of("INITIAL: 5000.00"));
        AccountEntity saved = accountRepository.save(entity);
        Optional<AccountEntity> retrieved = accountRepository.findById(saved.getId());
        Account convertedBack = retrieved.map(accountMapper::toDomain).orElseThrow();

        // Then
        assertEquals(original.id(), convertedBack.id());
        assertEquals(original.ownerName(), convertedBack.ownerName());
        assertEquals(0, original.balance().compareTo(convertedBack.balance()));
    }

    @Test
    void shouldPersistAndRetrieveTransactionHistoryAsJsonb() {
        // Given
        AccountEntity entity = new AccountEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerName("Test User");
        entity.setBalance(new BigDecimal("5000.00"));
        entity.setTransactionHistory(List.of(
                "DEPOSIT: 5000.00",
                "WITHDRAW: 1000.00",
                "TRANSFER: 500.00"
        ));

        // When
        AccountEntity saved = accountRepository.save(entity);
        Optional<AccountEntity> retrieved = accountRepository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        List<String> history = retrieved.get().getTransactionHistory();
        assertEquals(3, history.size());
        assertEquals("DEPOSIT: 5000.00", history.get(0));
        assertEquals("WITHDRAW: 1000.00", history.get(1));
        assertEquals("TRANSFER: 500.00", history.get(2));
    }
}
