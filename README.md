# NeoBank

> Next-generation digital banking platform with reactive API gateway and modular microservices architecture.

[![Java CI](https://github.com/anisul-islam-prog/neobank-core/actions/workflows/ci.yml/badge.svg)](https://github.com/anisul-islam-prog/neobank-core/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net)
[![Spring Boot 3.5.13](https://img.shields.io/badge/Spring%20Boot-3.5.13%20LTS-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2025.0.0](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-orange.svg)](https://spring.io/projects/spring-cloud)
[![Tests](https://img.shields.io/badge/Tests-2,396%20passing-brightgreen.svg)](#)

---

## 🚀 Quick Start

### Prerequisites
- Java 21+ (Virtual Threads enabled)
- Docker (for PostgreSQL and Testcontainers)
- Maven 3.9+

### Start Infrastructure
```bash
# Start PostgreSQL and other services
docker-compose --profile dev up -d

# Start monitoring stack (optional)
docker compose -f docker-compose-monitoring.yml up -d
```

### Run Backend (Spring Boot 3.5.13 LTS)

**All 9 modules are fully functional and tested with 2,396+ tests passing.**

```bash
# Build all modules
mvn clean install -DskipTests

# Run all available modules
./run-all.sh

# Run specific module
./run-all.sh --module neobank-gateway
./run-all.sh --module neobank-core-banking

# Stop all running modules
./run-all.sh --stop
```

**Available Services:**
| Service | Port | Status |
|---------|------|--------|
| API Gateway | 8080 | ✅ Working |
| Auth | 8081 | ✅ Working |
| Onboarding | 8082 | ✅ Working |
| Core Banking | 8083 | ✅ Working |
| Lending | 8084 | ✅ Working |
| Cards | 8085 | ✅ Working |
| Fraud | 8086 | ✅ Working |
| Batch | 8087 | ✅ Working |
| Analytics | 8088 | ✅ Working |

**Access Points:**
- API Gateway: http://localhost:8080
- Auth API: http://localhost:8081
- Onboarding API: http://localhost:8082
- Core Banking API: http://localhost:8083
- Lending API: http://localhost:8084
- Cards API: http://localhost:8085
- Fraud API: http://localhost:8086
- Batch API: http://localhost:8087
- Analytics API: http://localhost:8088
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health
- Grafana Dashboards: http://localhost:3003 (admin/admin123)
- Prometheus: http://localhost:9090

### Run Frontend Applications

The project includes three Next.js frontend applications:

**Retail App (Default)**
```bash
cd apps/retail-app

# With pnpm (recommended)
pnpm install && pnpm dev

# With npm (fallback)
npm install && npm run dev
```

**Staff Portal**
```bash
cd apps/staff-portal
pnpm install && pnpm dev
# or: npm install && npm run dev
```

**Admin Console**
```bash
cd apps/admin-console
pnpm install && pnpm dev
# or: npm install && npm run dev
```

**Frontend Access:**
- Retail App: http://localhost:3000
- Staff Portal: http://localhost:3001
- Admin Console: http://localhost:3002

---

## ✅ Backend Test Coverage

All 9 backend modules have comprehensive test suites with **2,396+ tests passing**:

| Module | Tests | Status | Architecture |
|--------|-------|--------|--------------|
| neobank-gateway | 64 | ✅ Complete | Reactive WebFlux + Spring Cloud Gateway |
| neobank-auth | 156 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-onboarding | 280 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-core-banking | 395 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-lending | 326 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-cards | 302 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-batch | 98 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-analytics | 75 | ✅ Complete | Servlet-based Spring Modulith |
| neobank-fraud | 241 | ✅ Complete | Servlet-based Spring Modulith |

### Run All Tests

```bash
# Run verification script (recommended)
./verify-backend.sh

# Run with Maven (all modules)
mvn clean test

# Run tests for specific module
mvn test -pl neobank-gateway
mvn test -pl neobank-auth
mvn test -pl neobank-lending
```

### Run Tests with Maven

```bash
# Run all tests from root
mvn clean test

# Run tests for specific module
mvn test -pl neobank-gateway
mvn test -pl neobank-auth
mvn test -pl neobank-analytics

# Run specific test class
mvn test -pl neobank-gateway -Dtest=SecurityConfigTest
mvn test -pl neobank-analytics -Dtest=TransferEventHandlerTest

# Run with coverage
mvn clean test -DargLine="-Dnet.bytebuddy.experimental=true --sun-misc-unsafe-memory-access=allow"
```

### Test Structure

```
[module]/src/test/
├── java/com/neobank/[module]/
│   ├── internal/
│   │   ├── [Service]Test.java          # Unit tests (@ExtendWith(MockitoExtension))
│   │   ├── [Entity]Test.java           # Entity state tests
│   │   └── [Repository]IntegrationTest.java # @SpringBootTest + Testcontainers
│   ├── web/
│   │   ├── [Controller]WebMvcTest.java # @WebMvcTest with security
│   │   ├── CardsWebMvcTestConfig.java  # Security + ObjectMapper for slice tests
│   │   └── CardsWebMvcTestBootConfig.java # @SpringBootConfiguration excluding JPA
│   └── config/
│       ├── SecurityConfigTest.java     # Security configuration tests
│       └── RateLimitingFilterTest.java # Filter tests (WebFlux or Servlet)
└── resources/
    └── application-test.yml            # Testcontainers + tracing disabled
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

NeoBank uses a **hybrid architecture**: a reactive Spring Cloud Gateway proxy routing to servlet-based Spring Modulith microservices.

```
neobank-parent/
├── neobank-gateway/        # Reactive API Gateway (Spring Cloud Gateway + WebFlux)
├── neobank-auth/           # Authentication (schema_auth)
├── neobank-onboarding/     # KYC & User Status (schema_onboarding)
├── neobank-core-banking/   # Accounts, Transfers (schema_core)
├── neobank-lending/        # Loans (schema_loans)
├── neobank-cards/          # Cards (schema_cards)
├── neobank-batch/          # EOD Reconciliation (schema_batch)
└── neobank-analytics/      # BI & CQRS (schema_analytics)
```

### Gateway Routing

The gateway acts as a reactive proxy, routing requests to downstream services:

| Route Pattern | Downstream Service | Default URI |
|---------------|-------------------|-------------|
| `/api/auth/**` | neobank-auth | http://localhost:8081 |
| `/api/onboarding/**` | neobank-onboarding | http://localhost:8082 |
| `/api/accounts/**`, `/api/transfers/**` | neobank-core-banking | http://localhost:8083 |
| `/api/loans/**` | neobank-lending | http://localhost:8084 |
| `/api/cards/**` | neobank-cards | http://localhost:8085 |

### Key Features
- **Reactive Gateway**: Spring Cloud Gateway with WebFlux for non-blocking I/O
- **Schema Isolation**: Each module has its own database schema
- **Event-Driven**: Spring Modulith events for cross-module communication
- **Maker-Checker**: Dual authorization for high-value operations
- **CQRS**: Read-optimized BI tables for analytics
- **Circuit Breakers**: Resilience4j for fault tolerance with fallback endpoints
- **Rate Limiting**: Reactive Bucket4j for API protection
- **Trace Propagation**: Micrometer tracing headers on all responses

---

## 🔒 Security

- **Gateway**: Reactive SecurityWebFilterChain with JWT resource server
- **Modules**: Servlet-based SecurityFilterChain with JWT validation
- JWT authentication with audience claims (retail/staff/admin)
- BCrypt password hashing
- AES-256-GCM card encryption
- Role-Based Access Control (8 roles)
- Maker-Checker protocol for sensitive operations
- CORS policies (3 specific frontend domains only)
- CSRF protection with secure cookies (CookieServerCsrfTokenRepository)
- HttpOnly, Secure, SameSite=Strict cookies
- API rate limiting (5-500 req/min based on user type)

---

## 🛡️ Resilience & Fault Tolerance

### Circuit Breakers
- Automatic failure detection and recovery per service route
- Configurable thresholds (50% failure rate, 30s recovery)
- Fallback endpoints for graceful degradation (`/fallback/*`)

### Rate Limiting
| User Type | Limit | Window |
|-----------|-------|--------|
| Retail Users | 100 requests | per minute |
| Staff Users | 500 requests | per minute |
| Public Registration | 5 requests | per minute |
| Unauthenticated (IP) | 60 requests | per minute |

### Retry Configuration
- 2 retries per route on 503 Service Unavailable
- Exponential backoff between retries
- Applied to GET, POST, PUT, DELETE, PATCH methods

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

### Gateway Testing

The gateway uses WebFlux testing patterns:

```bash
# Run gateway tests
mvn test -pl neobank-gateway

# Run specific test classes
mvn test -pl neobank-gateway -Dtest=SecurityConfigTest
mvn test -pl neobank-gateway -Dtest=RateLimitingFilterTest
mvn test -pl neobank-gateway -Dtest=TracePropagationFilterTest
mvn test -pl neobank-gateway -Dtest=CookieSecurityConfigTest
```

### Continuous Integration

Our CI pipeline runs on every push and PR with parallel jobs:
- ✅ **Backend**: Java 21 build + all module tests (JUnit 5 + Testcontainers)
- ✅ **Frontend**: Node.js 24 build for all 3 apps (Next.js 16 + TypeScript 5.8)
- ✅ **Test Summary**: Auto-generated per-module results in GitHub Actions summary

---

## 📋 Demo Credentials

| Portal | Username | Password |
|--------|----------|----------|
| Retail | customer_john | demo123! |
| Staff (Manager) | manager_bob | demo123! |
| Admin | admin_alice | demo123! |

---

## 🛠️ Tech Stack

**Gateway:** Java 21 · Spring Boot 3.5.13 LTS · Spring Cloud Gateway 2025.0.0 · WebFlux · Reactive Security
**Backend Modules:** Java 21 · Spring Boot 3.5.13 LTS · Spring Modulith 1.3.3 · Spring Security 6.x
**Data:** PostgreSQL 17 · Liquibase 4.31.1 · Hibernate 6.x · JPA
**Resilience:** Resilience4j 2.4.0 · Bucket4j 8.6.0
**Frontend:** Next.js 16 · React 19 · TypeScript 5.8 · Node.js 24 · pnpm 10 · Tailwind CSS · Recharts
**Testing:** JUnit 5.10+ · Testcontainers 1.20.4 · Mockito 5.17.0 · AssertJ 3.27.3
**Observability:** Micrometer 1.13+ · Micrometer Tracing 1.3+ · OpenTelemetry · OTel Collector · Prometheus · Grafana · Tempo · Loki

---

## 📊 Observability & Monitoring

NeoBank includes a full LGT (Loki, Grafana, Tempo) monitoring stack with OpenTelemetry Collector and production health check scripts.

### Start Monitoring

```bash
# Start the monitoring stack
docker compose -f docker-compose-monitoring.yml up -d

# Verify all services are healthy
docker compose -f docker-compose-monitoring.yml ps
```

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| Grafana | http://localhost:3003 | admin / admin123 | Unified dashboards |
| Prometheus | http://localhost:9090 | — | Metrics scraping and storage |
| Tempo | http://localhost:3200 | — | Distributed trace storage |
| Loki | http://localhost:3100 | — | Log aggregation |
| OTel Collector | :4317 (gRPC) / :4318 (HTTP) | — | Receives OTLP traces from backend & frontend |

### Frontend Setup (pnpm + Next.js 16)

The frontend requires **Node.js ≥ 24.14.1** and **pnpm ≥ 10**:

```bash
# Install pnpm globally (if not already installed)
npm install -g pnpm

# Install dependencies (all 3 frontend apps)
cd apps/retail-app && pnpm install
cd apps/staff-portal && pnpm install
cd apps/admin-console && pnpm install

# Run development server
pnpm dev

# Build for production
pnpm build
pnpm start
```

Frontend traces are sent to the OTel Collector via `src/instrumentation.ts` (OpenTelemetry NodeSDK). The `next.config.ts` proxies `/api/:path*` requests to the Reactive Gateway at `http://localhost:8080`.

### Backend Tracing Configuration

All backend modules export traces via OTLP. Configuration is in `application-observability.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (reduce in production)
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
```

Access actuator metrics at:
- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Prometheus: `http://localhost:8080/actuator/prometheus`
- Metrics: `http://localhost:8080/actuator/metrics`

### Production Health Monitoring

```bash
# One-time health check of all services
./scripts/check-system-health.sh

# Continuous monitoring every 30 seconds
./scripts/check-system-health.sh --watch 30

# JSON-only output (for scripting/CI)
./scripts/check-system-health.sh --json

# Help
./scripts/check-system-health.sh --help
```

The health check script monitors:
- **Core Services**: Frontend (:3000), Reactive Gateway (:8080)
- **Downstream Services**: Auth (:8081), Onboarding (:8082), Core Banking (:8083), Lending (:8084), Cards (:8085), Fraud (:8086), Batch (:8087), Analytics (:8088)
- **Observability Stack**: Prometheus (:9090), Grafana (:3003), Tempo (:3200), Loki (:3100), OTel Collector (:13133)

If any service is DOWN, the script outputs a color-coded alert and prints a mock JSON payload simulating a Slack webhook notification. Set `SLACK_WEBHOOK_URL` to enable real alerts.

---

## 📄 License

NeoBank is open-source under the [MIT License](LICENSE).

---

**Built with ❤️ using Java 21, Spring Boot 3.5.13 LTS, and Spring Cloud Gateway**
