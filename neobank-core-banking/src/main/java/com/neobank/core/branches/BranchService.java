package com.neobank.core.branches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for branch management.
 * Handles branch initialization and lookup operations.
 */
@Service
@Transactional
public class BranchService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BranchService.class);

    private static final String HEAD_OFFICE_CODE = "HO-001";
    private static final String HEAD_OFFICE_NAME = "Head Office";

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    /**
     * Initialize default branches on application startup.
     * Creates the Head Office branch if it doesn't exist.
     */
    @Override
    public void run(String... args) {
        initializeHeadOffice();
    }

    /**
     * Get branch by ID.
     */
    public Optional<BranchEntity> getBranchById(UUID id) {
        return branchRepository.findById(id);
    }

    /**
     * Get branch by code.
     */
    public Optional<BranchEntity> getBranchByCode(String code) {
        return branchRepository.findByCode(code);
    }

    /**
     * Get the Head Office branch.
     * Used for default assignment during user registration.
     */
    public BranchEntity getHeadOffice() {
        return branchRepository.findByCode(HEAD_OFFICE_CODE)
                .orElseThrow(() -> new IllegalStateException("Head Office branch not found"));
    }

    /**
     * Get or create branch by code.
     */
    public BranchEntity getOrCreateBranch(String code, String name) {
        return branchRepository.findByCode(code)
                .orElseGet(() -> createBranch(code, name));
    }

    /**
     * Create a new branch.
     */
    public BranchEntity createBranch(String code, String name) {
        if (branchRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Branch code already exists: " + code);
        }

        BranchEntity branch = new BranchEntity();
        branch.setId(UUID.randomUUID());
        branch.setCode(code);
        branch.setName(name);
        branch.setActive(true);
        branch.setCreatedAt(Instant.now());

        branchRepository.save(branch);
        log.info("Branch created: {} ({})", name, code);

        return branch;
    }

    /**
     * Initialize the Head Office branch if it doesn't exist.
     */
    private void initializeHeadOffice() {
        if (!branchRepository.existsByCode(HEAD_OFFICE_CODE)) {
            BranchEntity headOffice = new BranchEntity();
            headOffice.setId(UUID.randomUUID());
            headOffice.setCode(HEAD_OFFICE_CODE);
            headOffice.setName(HEAD_OFFICE_NAME);
            headOffice.setAddressLine1("123 Banking Street");
            headOffice.setCity("Financial District");
            headOffice.setState("NY");
            headOffice.setPostalCode("10001");
            headOffice.setCountry("US");
            headOffice.setPhone("+1-555-0100");
            headOffice.setEmail("headoffice@neobank.com");
            headOffice.setActive(true);
            headOffice.setCreatedAt(Instant.now());

            branchRepository.save(headOffice);
            log.info("Head Office branch initialized: {} ({})", HEAD_OFFICE_NAME, HEAD_OFFICE_CODE);
        }
    }
}
