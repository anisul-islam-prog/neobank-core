package com.neobank.branches.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for branch persistence.
 */
@Repository
interface BranchRepository extends JpaRepository<BranchEntity, UUID> {

    /**
     * Find branch by code.
     *
     * @param code the branch code
     * @return optional branch
     */
    Optional<BranchEntity> findByCode(String code);

    /**
     * Check if branch code exists.
     *
     * @param code the branch code
     * @return true if exists
     */
    boolean existsByCode(String code);

    /**
     * Find branch by name.
     *
     * @param name the branch name
     * @return optional branch
     */
    Optional<BranchEntity> findByName(String name);
}
