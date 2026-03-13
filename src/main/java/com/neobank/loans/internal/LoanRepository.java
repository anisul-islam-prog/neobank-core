package com.neobank.loans.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for loan persistence.
 */
@Repository
interface LoanRepository extends JpaRepository<LoanEntity, UUID> {

    /**
     * Find all loans for a given account.
     *
     * @param accountId the account identifier
     * @return list of loans
     */
    List<LoanEntity> findByAccountId(UUID accountId);
}
