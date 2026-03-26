# NeoBank

> Next-generation digital banking platform with modular microservices architecture.

[![Java CI](https://github.com/anisul-islam-prog/neobank-core/actions/workflows/ci.yml/badge.svg)](https://github.com/anisul-islam-prog/neobank-core/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 25](https://img.shields.io/badge/Java-25-blue.svg)](https://adoptium.net)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Tests: 2,422](https://img.shields.io/badge/Tests-2,422-brightgreen.svg)](MIGRATION_PLAN.md)

---

## 🚀 Quick Start

```bash
# Start infrastructure
docker-compose --profile dev up -d

# Run backend
mvn spring-boot:run

# Run frontend (retail)
cd apps/retail-app && npm install && npm run dev
```

**Access:**
- Retail App: http://localhost:3000
- Staff Portal: http://localhost:3001
- Admin Console: http://localhost:3002
- API: http://localhost:8080

---

## ✅ Backend Test Coverage

All 9 backend modules have comprehensive test suites with **2,422 total tests**:

| Module | Tests | Status |
|--------|-------|--------|
| neobank-gateway | 158 | ✅ Complete |
| neobank-auth | 286 | ✅ Complete |
| neobank-onboarding | 280 | ✅ Complete |
| neobank-core-banking | 428 | ✅ Complete |
| neobank-lending | 410 | ✅ Complete |
| neobank-cards | 372 | ✅ Complete |
| neobank-batch | 118 | ✅ Complete |
| neobank-analytics | 112 | ✅ Complete |
| neobank-fraud | 258 | ✅ Complete |

### Run All Tests

```bash
# Run verification script (recommended)
./verify-backend.sh

# Run with verbose output
./verify-backend.sh --verbose

# Run specific module
./verify-backend.sh --module neobank-auth
```

### Run Tests with Maven

```bash
# Run all tests from root
mvn clean test

# Run tests for specific module
mvn test -pl neobank-auth
mvn test -pl neobank-onboarding
mvn test -pl neobank-core-banking
mvn test -pl neobank-lending
mvn test -pl neobank-cards
mvn test -pl neobank-batch
mvn test -pl neobank-analytics
mvn test -pl neobank-fraud
mvn test -pl neobank-gateway

# Run specific test class
mvn test -pl neobank-auth -Dtest=AuthServiceTest
mvn test -pl neobank-auth -Dtest=JwtServiceTest
mvn test -pl neobank-onboarding -Dtest=OnboardingServiceTest

# Run with coverage
mvn clean test -DargLine="-Dnet.bytebuddy.experimental=true"
```

### Test Structure

Each module follows standardized test patterns:

```
[module]/src/test/
├── java/com/neobank/[module]/
│   ├── internal/
│   │   ├── [Service]Test.java          # Unit tests (@ExtendWith(MockitoExtension))
│   │   ├── [Entity]Test.java           # Entity state tests
│   │   └── [Repository]IntegrationTest.java # @DataJpaTest + Testcontainers
│   ├── web/
│   │   └── [Controller]WebMvcTest.java # @WebMvcTest with security
│   └── [Record/Enum]Test.java          # Record/enum tests
└── resources/
    └── application-test.yml            # Testcontainers config
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [📖 Features](FEATURES.md) | Complete list of NeoBank features |
| [📖 Operational Manual](docs/USAGE.md) | Complete user guide for customers, staff, and admins |
| [🏗️ Architecture](docs/ARCHITECTURE.md) | Technical deep-dive and system design |
| [🔄 Migration Plan](MIGRATION_PLAN.md) | Microservice migration roadmap with test coverage |
| [🤝 Contributing](CONTRIBUTING.md) | How to contribute to NeoBank |
| [📊 Chaos Engineering](CHAOS.md) | Failure scenarios and resilience testing |

---

## 🏛️ Platform Overview

### For Customers
- 🏦 Instant savings account on registration
- 💸 Real-time fund transfers
- 💳 Virtual and physical card management
- 📱 Intuitive dashboard at http://localhost:3000

### For Staff (Tellers, Managers, ROs)
- ✅ KYC user approval workflow
- 🔒 Maker-Checker protocol for high-value transfers (>$5,000)
- 📊 Credit score management and adjustment
- 💰 Loan application processing
- Access at http://localhost:3001

### For Administrators
- 📈 BI Dashboard with transaction analytics
- 🎯 Risk distribution and credit score heatmaps
- 📋 KYC funnel visualization
- System monitoring and health checks
- Access at http://localhost:3002

---

## 🏗️ Architecture

NeoBank uses a **Modular Monolith** architecture with Spring Modulith:

```
neobank-parent/
├── neobank-gateway/        # API Gateway, Rate Limiting, Security
├── neobank-auth/           # Authentication (schema_auth)
├── neobank-onboarding/     # KYC & User Status (schema_onboarding)
├── neobank-core-banking/   # Accounts, Transfers (schema_core)
├── neobank-lending/        # Loans (schema_loans)
├── neobank-cards/          # Cards (schema_cards)
├── neobank-batch/          # EOD Reconciliation (schema_batch)
└── neobank-analytics/      # BI & CQRS (schema_analytics)
```

### Key Features
- **Schema Isolation**: Each module has its own database schema
- **Event-Driven**: Spring Modulith events for cross-module communication
- **Maker-Checker**: Dual authorization for high-value operations
- **CQRS**: Read-optimized BI tables for analytics
- **Circuit Breakers**: Resilience4j for fault tolerance
- **Rate Limiting**: Bucket4j for API protection

---

## 🔒 Security

- JWT authentication with audience claims (retail/staff/admin)
- BCrypt password hashing
- AES-256-GCM card encryption
- Role-Based Access Control (8 roles)
- Maker-Checker protocol for sensitive operations
- CORS policies (3 specific frontend domains only)
- CSRF protection with secure cookies
- HttpOnly, Secure, SameSite=Strict cookies
- API rate limiting (5-500 req/min based on user type)

---

## 🛡️ Resilience & Fault Tolerance (Phase 7)

### Circuit Breakers
- Automatic failure detection and recovery
- Configurable thresholds per module
- Fallback mechanisms for graceful degradation

### Rate Limiting
| User Type | Limit | Window |
|-----------|-------|--------|
| Retail Users | 100 requests | per minute |
| Staff Users | 500 requests | per minute |
| Public Registration | 5 requests | per minute |
| Unauthenticated (IP) | 60 requests | per minute |

### Bulkhead Pattern
- Critical path isolation (transfers, auth)
- Non-critical path isolation (BI, analytics)
- Prevents resource starvation

### Database Migrations
- Liquibase for version-controlled schema changes
- Automatic migration on startup
- Rollback support for each changeset

---

## 🧪 Testing

```bash
# Run all tests
./test-all.sh

# Run specific suites
./test-all.sh --skip-backend   # Skip backend tests
./test-all.sh --skip-frontend  # Skip frontend tests
./test-all.sh --skip-e2e       # Skip E2E tests
```

### Continuous Integration

Our CI pipeline runs on every push and PR:
- ✅ Java 25 build
- ✅ Unit and integration tests
- ✅ Architecture documentation generation
- ✅ Modulith boundary verification
- ✅ Testcontainers for database tests

---

## 📋 Demo Credentials

| Portal | Username | Password |
|--------|----------|----------|
| Retail | customer_john | demo123! |
| Staff (Manager) | manager_bob | demo123! |
| Admin | admin_alice | demo123! |

---

## 🛠️ Tech Stack

**Backend:** Java 25 · Spring Boot 4 · Spring Modulith · PostgreSQL · Liquibase · Resilience4j · Bucket4j
**Frontend:** Next.js 14 · React · TypeScript · Tailwind CSS · Recharts
**AI:** Spring AI · Ollama (local) · OpenAI (cloud)
**Testing:** JUnit 5 · Testcontainers · Vitest · Playwright
**Observability:** Micrometer · Prometheus · Grafana · OpenTelemetry

---

## 📄 License

NeoBank is open-source under the [MIT License](LICENSE).

---

**Built with ❤️ using Java 25 and Spring Boot 4**
