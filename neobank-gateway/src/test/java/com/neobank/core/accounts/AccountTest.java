package com.neobank.core.accounts;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateValidAccount() {
        UUID id = UUID.randomUUID();
        String ownerName = "John Doe";
        BigDecimal balance = new BigDecimal("1000.00");

        Account account = new Account(id, ownerName, balance);

        assertNotNull(account);
        assertEquals(id, account.id());
        assertEquals(ownerName, account.ownerName());
        assertEquals(balance, account.balance());
    }

    @Test
    void shouldRejectBlankOwnerName() {
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1000.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Account(id, "   ", balance));

        assertEquals("Account ownerName must not be blank", exception.getMessage());
    }

    @Test
    void shouldRejectNullOwnerName() {
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1000.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Account(id, null, balance));

        assertEquals("Account ownerName must not be blank", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeBalance() {
        UUID id = UUID.randomUUID();
        String ownerName = "John Doe";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Account(id, ownerName, new BigDecimal("-100.00")));

        assertEquals("Account balance must not be null", exception.getMessage());
    }
}
