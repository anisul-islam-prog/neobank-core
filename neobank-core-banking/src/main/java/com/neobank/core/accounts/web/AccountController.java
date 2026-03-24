package com.neobank.core.accounts.web;

import com.neobank.core.accounts.AccountService;
import com.neobank.core.accounts.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for account operations.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Create a new account.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody AccountRequest request) {
        Account account = accountService.createNewAccount(request.owner(), request.initialBalance());
        return ResponseEntity.ok(Map.of(
            "id", account.id().toString(),
            "owner", account.ownerName(),
            "balance", account.balance(),
            "currency", "USD"
        ));
    }

    /**
     * Get account by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable UUID id) {
        Optional<Account> accountOpt = accountService.getAccount(id);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            return ResponseEntity.ok(Map.of(
                "id", account.id().toString(),
                "owner", account.ownerName(),
                "balance", account.balance(),
                "currency", "USD"
            ));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Account request DTO.
     */
    public record AccountRequest(
        String owner,
        BigDecimal initialBalance
    ) {}
}
