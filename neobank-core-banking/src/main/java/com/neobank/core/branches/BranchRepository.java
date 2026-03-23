package com.neobank.core.branches;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for BranchEntity.
 */
public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {

    Optional<BranchEntity> findByCode(String code);

    boolean existsByCode(String code);
}
