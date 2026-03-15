# NeoBank Microservice-Ready Migration Plan

## Executive Summary

This document outlines the migration strategy for transitioning NeoBank from a single-module monolith to a **Microservice-Ready Modular Monolith** architecture. This refactor enables future extraction of independent microservices while maintaining a single deployable unit.

---

## Current State Analysis

### Existing Structure
```
neobank-core/
├── src/main/java/com/neobank/
│   ├── accounts/      # Account management
│   ├── auth/          # Authentication + User management (mixed concerns)
│   ├── cards/         # Card lifecycle
│   ├── fraud/         # AI fraud detection
│   ├── loans/         # Loan origination
│   ├── notifications/ # Event listeners
│   └── transfers/     # Fund transfers
├── frontend/          # Single Next.js app (retail only)
└── pom.xml            # Single-module build
```

### Current Issues
| Issue | Impact | Severity |
|-------|--------|----------|
| Mixed concerns in auth module | Hard to extract, violates SRP | High |
| No schema separation | Cross-module DB coupling | High |
| Single frontend app | No role-specific UX | Medium |
| No gateway layer | Direct module exposure | High |
| Missing audience claims | Token misuse possible | High |
| No batch processing | Manual EOD operations | Medium |

---

## Target Architecture

### Module Decomposition

```
neobank-parent/ (multi-module Maven)
│
├── neobank-gateway/           # Single entry point, routing, Swagger protection
├── neobank-auth/              # Infrastructure: credentials, JWT, schema_auth
├── neobank-onboarding/        # Business: KYC, user status, schema_onboarding
├── neobank-core-banking/      # Accounts, transfers, branches (schema_core)
├── neobank-lending/           # Loans module (schema_loans)
├── neobank-cards/             # Cards module (schema_cards)
├── neobank-batch/             # EOD reconciliation, interest calculation
└── apps/                      # Frontend monorepo
    ├── retail-app/            # Customer dashboard
    ├── staff-portal/          # Tellers, managers (KYC, loans)
    └── admin-console/         # SysAdmin (audits, config)
```

### Database Schema Separation

| Module | Schema | Tables | Access |
|--------|--------|--------|--------|
| **auth** | `schema_auth` | users, user_roles, jwt_tokens | Internal only |
| **onboarding** | `schema_onboarding` | user_status, kyc_documents, approvals | Internal + Gateway |
| **core-banking** | `schema_core` | accounts, transfers, branches | Internal + Gateway |
| **lending** | `schema_loans` | loans, amortization, risk_profiles | Internal + Gateway |
| **cards** | `schema_cards` | cards, spending_limits, mcc_blocks | Internal + Gateway |
| **batch** | `schema_batch` | batch_jobs, reconciliation_logs | Internal only |

### Cross-Module Communication

| Pattern | Use Case | Implementation |
|---------|----------|----------------|
| **@NamedInterface** | Synchronous calls within process | Spring Modulith public APIs |
| **@ApplicationEventListener** | Asynchronous domain events | Spring Modulith events |
| **UUID References** | Cross-schema relationships | No FK constraints, application-level integrity |
| **Gateway Routing** | External requests | Path-based routing to modules |

---

## Migration Phases

### Phase 1: Foundation Setup (Week 1)

**Goal:** Create module structure without breaking existing functionality

#### Tasks
1. **Create Multi-Module Maven Structure**
   - Parent POM with module declarations
   - Individual POMs for each module
   - Shared dependency management

2. **Database Schema Configuration**
   - Add schema properties to `application.properties`
   - Create schema initialization scripts
   - Configure Hibernate per-module

3. **Gateway Module Skeleton**
   - Basic Spring Boot application
   - Route configuration placeholder
   - CORS and security baseline

4. **Frontend Monorepo Setup**
   - Create `apps/` directory structure
   - Initialize three Next.js apps with shared config
   - Set up build scripts

#### Deliverables
- ✅ Multi-module Maven build (compiles successfully)
- ✅ Database schemas created on startup
- ✅ Gateway routes to existing endpoints
- ✅ Three frontend apps build successfully

#### Risks & Mitigation
| Risk | Impact | Mitigation |
|------|--------|------------|
| Build complexity | High | Keep existing pom.xml as fallback |
| Schema migration errors | High | Use Flyway for versioned migrations |
| Frontend duplication | Medium | Create shared UI component library |

---

### Phase 2: Auth Module Extraction (Week 2)

**Goal:** Separate infrastructure (credentials) from business logic

#### Current State
```
com.neobank.auth/
├── UserEntity          # Mixed: credentials + status
├── AuthService         # Mixed: JWT + user approval
├── JwtService          # Infrastructure ✓
└── SecurityConfig      # Infrastructure ✓
```

#### Target State
```
com.neobank.auth/ (Infrastructure Only)
├── api/
│   ├── AuthApi.java           # Named interface
│   └── JwtTokenProvider.java  # Public API
├── internal/
│   ├── UserCredentialsEntity  # schema_auth.users
│   ├── JwtService.java
│   ├── PasswordEncoder.java
│   └── SecurityFilterChain.java
└── web/
    └── TokenController.java   # /api/auth/token/*
```

```
com.neobank.onboarding/ (Business Logic)
├── api/
│   ├── OnboardingApi.java
│   └── UserStatusChecker.java
├── internal/
│   ├── UserProfileEntity      # schema_onboarding.profiles
│   ├── KycDocumentEntity      # schema_onboarding.kyc_documents
│   ├── ApprovalWorkflow.java
│   └── UserStatusService.java
└── web/
    ├── RegistrationController.java  # /api/onboarding/register
    └── ApprovalController.java      # /api/onboarding/approve/*
```

#### The Handshake Flow
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Public    │────▶│  Onboarding  │────▶│    Auth     │
│  Register   │     │  (PENDING)   │     │ (Credentials)│
└─────────────┘     └──────────────┘     └─────────────┘
                           │                    │
                           │ UserCreatedEvent   │
                           ▼                    ▼
                    ┌──────────────┐     ┌─────────────┐
                    │    Admin     │     │   JWT Token │
                    │   Approval   │     │   Issued    │
                    └──────────────┘     └─────────────┘
                           │
                           │ ApprovedEvent
                           ▼
                    ┌──────────────┐
                    │   ACTIVE     │
                    │  (Can Login) │
                    └──────────────┘
```

#### Deliverables
- ✅ Auth module handles only credentials + JWT
- ✅ Onboarding module handles KYC + status
- ✅ Handshake flow implemented
- ✅ All existing tests pass

#### Risks & Mitigation
| Risk | Impact | Mitigation |
|------|--------|------------|
| Circular dependencies | High | Use events, not direct calls |
| Transaction boundaries | High | Use @TransactionalEventListener |
| Login flow breaks | High | Keep fallback to monolith flow |

---

### Phase 3: Core Banking Module (Week 3)

**Goal:** Consolidate accounts, transfers, branches with schema separation

#### Module Structure
```
com.neobank.core-banking/
├── accounts/
│   ├── AccountEntity.java      # schema_core.accounts
│   ├── AccountRepository.java
│   └── AccountService.java
├── transfers/
│   ├── TransferEntity.java     # schema_core.transfers
│   ├── TransferService.java
│   └── CircuitBreakerConfig.java
├── branches/
│   ├── BranchEntity.java       # schema_core.branches
│   └── BranchService.java
└── CoreBankingConfig.java      # Schema configuration
```

#### Schema Migration
```sql
-- Create schema_core
CREATE SCHEMA IF NOT EXISTS schema_core;

-- Move tables (example for accounts)
CREATE TABLE schema_core.accounts (
    id UUID PRIMARY KEY,
    owner_name VARCHAR(255) NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    branch_id UUID,  -- Reference only, no FK
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Similar for transfers, branches
```

#### No Cross-Schema Joins
```java
// ❌ BEFORE (Foreign Key)
@Entity
@Table(name = "accounts")
class AccountEntity {
    @ManyToOne
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_account_branch"))
    private BranchEntity branch;
}

// ✅ AFTER (UUID Reference)
@Entity
@Table(name = "accounts", schema = "schema_core")
class AccountEntity {
    @Column(name = "branch_id")
    private UUID branchId;  // Application-level integrity
}
```

#### Deliverables
- ✅ Core banking module with schema_core
- ✅ No FK constraints to other schemas
- ✅ All transfer tests pass
- ✅ Branch assignment works via UUID

---

### Phase 4: Lending & Cards Modules (Week 4)

**Goal:** Isolate loans and cards with dedicated schemas

#### Module Dependencies
```
neobank-lending/
├── Dependencies: neobank-core-banking (for accounts)
├── Schema: schema_loans
└── Tables: loans, amortization_entries, risk_profiles

neobank-cards/
├── Dependencies: neobank-core-banking (for accounts)
├── Schema: schema_cards
└── Tables: cards, spending_limits, mcc_blocklist
```

#### Communication Pattern
```java
// Loans module calls Core Banking via Named Interface
@Service
class LoanDisbursementService {
    
    private final AccountApi accountApi;  // From core-banking
    
    public DisbursementResult disburse(UUID loanId) {
        // Get loan details from schema_loans
        LoanEntity loan = loanRepository.findById(loanId);
        
        // Credit account via public API (cross-module)
        accountApi.creditAccount(loan.getAccountId(), loan.getPrincipal());
        
        // Update loan status in schema_loans
        loan.setStatus(LoanStatus.DISBURSED);
        loanRepository.save(loan);
    }
}
```

#### Deliverables
- ✅ Lending module with schema_loans
- ✅ Cards module with schema_cards
- ✅ Cross-module account operations work
- ✅ Loan disbursement flow intact

---

### Phase 5: Batch Module (Week 5)

**Goal:** Implement EOD processing and reconciliation

#### Module Structure
```
com.neobank.batch/
├── jobs/
│   ├── EndOfDayJob.java        # Main EOD orchestrator
│   ├── InterestCalculationJob.java
│   ├── ReconciliationJob.java
│   └── OverdraftNotificationJob.java
├── config/
│   └── BatchSchedulerConfig.java
└── listeners/
    └── BatchJobListener.java
```

#### EOD Flow
```
┌─────────────────────────────────────────────────────────┐
│                    End-of-Day Job                        │
├─────────────────────────────────────────────────────────┤
│  1. Lock all accounts (prevent concurrent modifications)│
│  2. Calculate interest on savings accounts              │
│  3. Calculate interest on loans                         │
│  4. Reconcile transfers (match pending transactions)    │
│  5. Generate overdraft notifications                    │
│  6. Update batch_job_logs table                         │
│  7. Unlock accounts                                      │
└─────────────────────────────────────────────────────────┘
```

#### Deliverables
- ✅ Batch module created
- ✅ EOD job runs at scheduled time
- ✅ Interest calculation accurate
- ✅ Reconciliation reports generated

---

### Phase 6: Multi-Persona Frontend (Week 6)

**Goal:** Create three role-specific frontend applications

#### App Structure
```
apps/
├── retail-app/              # Port 3000
│   ├── app/
│   │   ├── dashboard/       # Account overview
│   │   ├── transfers/       # Make transfers
│   │   ├── cards/           # View cards
│   │   └── loans/           # Apply for loans
│   └── middleware.ts        # JWT validation (aud: retail)
│
├── staff-portal/            # Port 3001
│   ├── app/
│   │   ├── customers/       # Search users
│   │   ├── kyc/             # Approve pending users
│   │   ├── loans/           # Process applications
│   │   └── approvals/       # Loan approvals
│   └── middleware.ts        # JWT validation (aud: staff)
│
└── admin-console/           # Port 3002
    ├── app/
    │   ├── branches/        # Branch management
    │   ├── users/           # Staff onboarding
    │   ├── docs/            # Swagger token management
    │   └── audits/          # System audits
    └── middleware.ts        # JWT validation (aud: admin)
```

#### Shared Configuration
```typescript
// apps/shared/config.ts
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const ROLE_ROUTES = {
  retail: '/dashboard',
  staff: '/customers',
  admin: '/branches',
};

export const JWT_AUDIENCE = {
  retail: 'retail',
  staff: 'staff',
  admin: 'admin',
};
```

#### Deliverables
- ✅ Three frontend apps build successfully
- ✅ Role-based routing works
- ✅ JWT audience validation in middleware
- ✅ Shared component library (optional)

---

### Phase 7: Gateway & Security (Week 7)

**Goal:** Implement gateway routing and JWT audience claims

#### Gateway Configuration
```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> routing() {
        return RouterFunctions
            .route(RequestPredicates.path("/api/auth/**"), authHandler())
            .andRoute(RequestPredicates.path("/api/onboarding/**"), onboardingHandler())
            .andRoute(RequestPredicates.path("/api/accounts/**"), accountsHandler())
            .andRoute(RequestPredicates.path("/api/transfers/**"), transfersHandler())
            .andRoute(RequestPredicates.path("/api/loans/**"), loansHandler())
            .andRoute(RequestPredicates.path("/api/cards/**"), cardsHandler())
            .andRoute(RequestPredicates.path("/api/admin/**"), adminHandler())
            .andRoute(RequestPredicates.path("/swagger-ui/**"), swaggerHandler())
            .andRoute(RequestPredicates.path("/v3/api-docs/**"), docsHandler());
    }
}
```

#### JWT Audience Claims
```java
@Service
public class JwtService {

    public String generateToken(UUID userId, String username, String role, String audience) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .claim("aud", audience)  // Audience claim
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey)
                .compact();
    }

    public void validateAudience(String token, String expectedAudience) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        String aud = claims.get("aud", String.class);
        if (!expectedAudience.equals(aud)) {
            throw new JwtAudienceException(
                "Invalid audience: expected " + expectedAudience + ", got " + aud);
        }
    }
}
```

#### Security Filter Chain
```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                
                // Retail endpoints (require retail audience)
                .requestMatchers("/api/accounts/**").hasAudience("retail")
                .requestMatchers("/api/transfers/**").hasAudience("retail")
                
                // Staff endpoints (require staff audience)
                .requestMatchers("/api/onboarding/approve/**").hasAudience("staff")
                .requestMatchers("/api/loans/approve/**").hasAudience("staff")
                
                // Admin endpoints (require admin audience)
                .requestMatchers("/api/admin/**").hasAudience("admin")
                .requestMatchers("/swagger-ui/**").hasAudience("admin")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAudienceFilter, JwtAuthenticationFilter.class);
        
        return http.build();
    }
}
```

#### Swagger Protection
```java
@Component
public class SwaggerAccessFilter implements Filter {

    private final DocTokenService tokenService;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Check for X-DOC-ACCESS-TOKEN header or access_token param
        String token = httpRequest.getHeader("X-DOC-ACCESS-TOKEN");
        if (token == null) {
            token = httpRequest.getParameter("access_token");
        }
        
        if (token == null || !tokenService.isValid(token)) {
            ((HttpServletResponse) response).sendError(403, "Valid access token required");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

#### Deliverables
- ✅ Gateway routes all requests
- ✅ JWT audience validation works
- ✅ Swagger UI protected by token
- ✅ Retail token cannot access staff endpoints

---

### Phase 8: Documentation & Testing (Week 8)

**Goal:** Update documentation and ensure full test coverage

#### ARCHITECTURE.md Updates
- Module dependency diagram
- Schema separation details
- Data flow between frontends and backend
- Security model (audience claims, zero-trust)

#### Testing Strategy
| Test Type | Coverage | Tools |
|-----------|----------|-------|
| Unit Tests | All services | JUnit 5, Mockito |
| Integration Tests | Module boundaries | Testcontainers, Spring Modulith |
| E2E Tests | Full user flows | Playwright |
| Security Tests | Audience validation | OWASP ZAP |

#### Deliverables
- ✅ ARCHITECTURE.md updated with distributed layout
- ✅ All modules have >80% test coverage
- ✅ E2E tests for all three frontends
- ✅ Security audit passed

---

## Rollback Strategy

### Phase-Level Rollback
If any phase fails:
1. **Revert Git commit** for that phase
2. **Restore previous pom.xml** (single-module)
3. **Drop new schemas** from database
4. **Deploy monolith** as fallback

### Data Migration Rollback
```sql
-- Backup before migration
CREATE TABLE backup_users AS SELECT * FROM users;
CREATE TABLE backup_accounts AS SELECT * FROM accounts;

-- Rollback script (if needed)
DROP SCHEMA schema_auth CASCADE;
DROP SCHEMA schema_onboarding CASCADE;
-- ... drop all new schemas

-- Restore from backup
INSERT INTO users SELECT * FROM backup_users;
```

---

## Success Criteria

| Criteria | Measurement | Target |
|----------|-------------|--------|
| **Build Success** | Maven compiles all modules | ✅ 100% |
| **Test Pass Rate** | All tests pass | ✅ >95% |
| **Schema Isolation** | No cross-schema FK constraints | ✅ 100% |
| **Audience Validation** | Retail token rejected from staff endpoints | ✅ 100% |
| **Frontend Separation** | Three apps build independently | ✅ 100% |
| **Gateway Routing** | All requests routed correctly | ✅ 100% |
| **Zero-Trust Onboarding** | PENDING users cannot login | ✅ 100% |

---

## Timeline Summary

| Phase | Duration | Dependencies | Risk Level |
|-------|----------|--------------|------------|
| **Phase 1: Foundation** | Week 1 | None | Low |
| **Phase 2: Auth Extraction** | Week 2 | Phase 1 | Medium |
| **Phase 3: Core Banking** | Week 3 | Phase 2 | Medium |
| **Phase 4: Lending & Cards** | Week 4 | Phase 3 | Low |
| **Phase 5: Batch Module** | Week 5 | Phase 4 | Low |
| **Phase 6: Frontend Suite** | Week 6 | Phase 1 | Medium |
| **Phase 7: Gateway & Security** | Week 7 | Phase 2, 6 | High |
| **Phase 8: Documentation** | Week 8 | All phases | Low |

**Total Duration:** 8 weeks

---

## Next Steps

1. **Review this plan** with the team
2. **Set up feature branch** for migration work
3. **Begin Phase 1** (Foundation Setup)
4. **Weekly checkpoints** to validate progress

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-16  
**Author:** NeoBank Development Team
