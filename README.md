# NeoBank Core

> Production-grade, microservice-ready digital banking platform built on Spring Boot 3.5 LTS with reactive API gateway, AI-powered lending, and full observability stack.

[![CI/CD](https://github.com/anisul-islam-prog/neobank-core/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/anisul-islam-prog/neobank-core/actions/workflows/ci-cd.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![Spring Boot 3.5.13](https://img.shields.io/badge/Spring%20Boot-3.5.13%20LTS-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2025.0.0](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-orange)](https://spring.io/projects/spring-cloud)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-ready-blue)](k8s/)
[![OpenAI](https://img.shields.io/badge/AI-OpenAI%20%2B%20Ollama-7b68ee)](#hybrid-ai-strategy)

---

## Quick Start (3 Steps)

```bash
# Step 1 — Start infrastructure (PostgreSQL)
docker compose --profile dev up -d

# Step 2 — Build & test all 9 modules
mvn clean install

# Step 3 — Launch all microservices in dependency order
./run-all.sh
```

All services healthy in ~60 seconds. See [Service Port Map](#service-port-map) for access points.

---

## Quick Start: The 60-Second Bank

Want to see a fully running bank with realistic data — in one command?

```bash
docker compose --profile demo up -d
```

This single command:

1. **Starts PostgreSQL** — pre-loaded with `seed-data.sql` on first run
2. **Starts Ollama** — local AI models for lending risk assessment (no API key needed)
3. **Starts the full LGT observability stack** — Loki (logs), Grafana (metrics), Tempo (traces)
4. **Auto-populates 50+ customers** with realistic names, accounts, and balances

Then launch the 9 Spring Boot services:

```bash
SPRING_PROFILES_ACTIVE=demo ./run-all.sh
```

Within ~60 seconds you'll have:

| What | Count |
|------|-------|
| Customers | 50+ |
| Transactions | 200+ |
| Loans (with AI risk assessments) | 15 |
| Payment Cards (virtual + physical) | 50+ |
| KYC Records | All customers |
| Fraud Analyses | 30+ |
| High-Value Transactions (Maker-Checker queue) | 10+ |

**Grafana dashboards:** http://localhost:3000 (`admin` / `admin123`)
**Swagger UI:** http://localhost:8080/swagger-ui.html

To tear everything down: `docker compose --profile demo down -v`

> **Troubleshooting — seed data not loading?**  
> PostgreSQL only runs init scripts on a **fresh** volume. If you previously started the demo profile and the seed data didn't appear, prune the old volume first:
> ```bash
> docker compose --profile demo down -v   # destroys the old postgres_data volume
> docker compose --profile demo up -d     # recreates it and re-runs seed-data.sql
> ```

---

## Running Environments

NeoBank supports four environment profiles, each optimized for a specific use case:

| Profile | Purpose | Infrastructure | Seed Data | AI Provider |
|---------|---------|---------------|-----------|-------------|
| **dev** | Local development | PostgreSQL only | ❌ | Ollama (local) |
| **test** | Integration testing | PostgreSQL + LGT Stack | ❌ | Ollama (local) |
| **demo** | Presentations & showcases | PostgreSQL + Ollama + LGT Stack | ✅ (50+ users, 200+ txns, 15 loans) | Ollama (local) |
| **prod** | Production (Kubernetes) | Managed DB + K8s secrets | ❌ | OpenAI (cloud) |

### Development (dev)

Lightweight setup for coding and unit testing:

```bash
# Start only PostgreSQL
docker compose --profile dev up -d

# Run services with dev profile
SPRING_PROFILES_ACTIVE=dev ./run-all.sh
```

- Hot-reload enabled (Spring Boot DevTools)
- `ddl-auto: update` — schema auto-creates on first run
- DEBUG logging for all modules
- Tracing disabled (reduced overhead)

### Integration Testing (test)

Full system with observability:

```bash
# Start PostgreSQL + Grafana, Prometheus, Tempo, Loki
docker compose --profile test up -d

# Run services with test profile
SPRING_PROFILES_ACTIVE=test ./run-all.sh
```

- Full LGT monitoring stack
- Grafana dashboards: http://localhost:3000 (`admin` / `admin123`)
- Prometheus: http://localhost:9090
- Distributed traces in Tempo: http://localhost:3200

### Demo / Showcase (demo)

Pre-loaded with realistic data for presentations — no Spring Boot rebuild needed:

```bash
# Start everything: PostgreSQL + Ollama + full observability + seed data
docker compose --profile demo up -d

# Run services with demo profile
SPRING_PROFILES_ACTIVE=demo ./run-all.sh
```

- **50+ customers** with realistic names and balances
- **200+ transactions** across categories (salary, rent, shopping, etc.)
- **15 loan applications** with AI risk assessments (8 approved)
- **50+ payment cards** (virtual + physical)
- **KYC records** for all customers
- **30 fraud analysis** records with risk scores
- **10 high-value transactions** in Maker-Checker queue

### Production (prod)

Security-hardened for Kubernetes deployment:

```bash
# Via CI/CD: push to main → build → deploy to K8s
# Or manually:
kubectl apply -k k8s/overlays/prod
```

- `ddl-auto: validate` — never auto-modify schema
- HikariCP connection pooling (strict limits)
- Liquibase manages migrations
- Structured WARN-level logging
- Full OpenTelemetry tracing (0.1% sampling)
- Secrets via Kubernetes Secret objects

---

## Service Port Map

| Service | Port | Purpose | Health Endpoint |
|---------|:---:|---------|:---:|
| **API Gateway** | 8080 | Reactive Spring Cloud Gateway — single entry point | `GET /actuator/health` |
| **Auth** | 8081 | JWT issuance, OAuth2, RBAC, user credentials | `GET /actuator/health` |
| **Onboarding** | 8082 | KYC workflow, customer registration & approval | `GET /actuator/health` |
| **Core Banking** | 8083 | Accounts, transfers, Maker-Checker protocol | `GET /actuator/health` |
| **Lending** | 8084 | AI-powered loan origination & risk assessment | `GET /actuator/health` |
| **Cards** | 8085 | Card lifecycle, spending controls, AES-256 encryption | `GET /actuator/health` |
| **Fraud** | 8086 | AI fraud detection, risk scoring, async analysis | `GET /actuator/health` |
| **Batch** | 8087 | EOD reconciliation, interest calculation, alerts | `GET /actuator/health` |
| **Analytics** | 8088 | CQRS read model, BI aggregation, dashboards | `GET /actuator/health` |

---

## Observability & Developer Links

| Dashboard | URL | Credentials |
|-----------|:---:|:---:|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | — |
| **Grafana (Metrics)** | http://localhost:3000 | `admin` / `admin123` |
| **Prometheus** | http://localhost:9090 | — |
| **Tempo (Traces)** | http://localhost:3200 | — |
| **Loki (Logs)** | http://localhost:3100 | — |
| **Actuator Health** | http://localhost:8080/actuator/health | — |
| **Circuit Breakers** | http://localhost:8080/actuator/circuitbreakers | — |
| **Prometheus Metrics** | http://localhost:8080/actuator/prometheus | — |

---

## Architecture at a Glance

```
  ┌──────────────────────────────────────────────────────────────┐
  │                    Clients (3 Next.js Apps)                   │
  │  Retail :3000   │   Staff Portal :3001   │   Admin :3002     │
  └──────────────────────────┬───────────────────────────────────┘
                             │ HTTPS + JWT
  ┌──────────────────────────▼───────────────────────────────────┐
  │              neobank-gateway  :8080 (Reactive)                │
  │  CORS → Rate Limit → JWT Auth → Route → Circuit Breaker      │
  └──┬────┬────┬────┬────┬────┬────┬────┬────────────────────────┘
     │    │    │    │    │    │    │    │
  ┌──▼─┐┌─▼──┐┌─▼───┐┌▼───┐┌▼──┐┌▼──┐┌▼───┐┌▼────────┐
  │Auth││Onbd││Core ││Lend││Card││Frd ││Batch││Analytics│
  │8081││8082││Bank ││8084││8085││8086││8087 ││8088     │
  └────┘└────┘└─────┘└────┘└────┘└────┘└─────┘└─────────┘
                             │
              ┌──────────────▼──────────────┐
              │   PostgreSQL 17 (9 schemas) │
              └─────────────────────────────┘
```

### Key Design Decisions

| Decision | Technology | Why |
|----------|-----------|-----|
| Reactive gateway | Spring Cloud Gateway + WebFlux | Non-blocking I/O for high-throughput routing |
| Servlet microservices | Spring Modulith modules | Simpler per-service model, enforced boundaries |
| Schema-per-module | PostgreSQL schemas | Data isolation without database sprawl |
| AI-powered lending | OpenAI (prod) + Ollama (dev) | Hybrid: cloud accuracy, local privacy |
| Circuit breakers | Resilience4j | Per-route fault tolerance with automatic recovery |
| Rate limiting | Bucket4j | Reactive token-bucket algorithm |
| Observability | LGT + OTel | Full-stack: logs (Loki), metrics (Prometheus), traces (Tempo) |

---

## Production Deployment

### Kubernetes (Zero-Downtime)

```bash
# Deploy all 9 services with rolling updates
./scripts/deploy-prod.sh

# Verify health, fallback routes, and pod readiness
./scripts/verify-deployment.sh
```

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for the full CI/CD pipeline, Kustomize overlays, and rollback procedures.

### CI/CD Pipeline

Push to `main` triggers:

1. **Build & Test** — Java 21, `mvn clean install` (zero-skip, fails immediately on any test failure)
2. **Docker Build** — 9 parallel builds with `load: true` (images stored in runner's local Docker daemon)
3. **Security Scan** — Trivy scans local images for CRITICAL/HIGH vulnerabilities *before* push (fails pipeline on detection)
4. **Docker Push** — 9 parallel pushes to GHCR, only after Trivy confirms images are safe
5. **Deploy** — `kubectl apply -k k8s/overlays/prod` (manual `workflow_dispatch` only)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Runtime** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 3.5.13 LTS |
| **Gateway** | Spring Cloud Gateway 2025.0.0 + WebFlux |
| **Modular Architecture** | Spring Modulith 1.3.3 |
| **Security** | Spring Security 6.x, JWT (JJWT 0.12.6) |
| **Database** | PostgreSQL 17, Liquibase 4.31.1, Hibernate 6.x |
| **Resilience** | Resilience4j 2.4.0, Bucket4j 8.6.0 |
| **AI** | Spring AI 1.0.0 (OpenAI gpt-4o-mini / Ollama llama3.2) |
| **Frontend** | Next.js 16, React 19, TypeScript 5.8, Node.js 24 |
| **Testing** | JUnit 5, Testcontainers 1.20.4, Mockito 5, AssertJ 3 |
| **Observability** | Micrometer, OpenTelemetry, Prometheus, Grafana, Tempo, Loki |
| **CI/CD** | GitHub Actions, Docker (GHCR), Trivy, Kubernetes Kustomize |

---

## Module Test Coverage

| Module | Tests | Status | Architecture |
|--------|------:|:------:|-------------|
| neobank-gateway | 64 | ✅ | Reactive WebFlux + Spring Cloud Gateway |
| neobank-auth | 156 | ✅ | Servlet-based Spring Modulith |
| neobank-onboarding | 280 | ✅ | Servlet-based Spring Modulith |
| neobank-core-banking | 395 | ✅ | Servlet-based Spring Modulith |
| neobank-lending | 326 | ✅ | Servlet-based Spring Modulith |
| neobank-cards | 302 | ✅ | Servlet-based Spring Modulith |
| neobank-batch | 98 | ✅ | Servlet-based Spring Modulith |
| neobank-analytics | 75 | ✅ | Servlet-based Spring Modulith |
| neobank-fraud | 241 | ✅ | Servlet-based Spring Modulith |

---

## Documentation

| Document | Description |
|----------|-------------|
| [📖 Operational Manual](docs/USAGE.md) | Customer, staff, and admin user guides with API examples |
| [🏗️ Architecture](docs/ARCHITECTURE.md) | Technical deep-dive, module boundaries, observability |
| [🚀 Deployment Guide](DEPLOYMENT_GUIDE.md) | CI/CD pipeline, K8s manifests, deployment scripts |
| [🤝 Contributing](CONTRIBUTING.md) | How to contribute, coding standards, commit guidelines |
| [📊 Chaos Engineering](CHAOS.md) | Failure scenarios and resilience testing |
| [🔧 Disaster Recovery](DISASTER_RECOVERY.md) | Backup, restore, and recovery procedures |
| [✨ Features](FEATURES.md) | Complete feature list |

---

## Demo Credentials

| Portal | Username | Password |
|--------|----------|----------|
| Retail (Customer) | `customer_john` | `demo123!` |
| Staff (Manager) | `manager_bob` | `demo123!` |
| Admin | `admin_alice` | `demo123!` |

---

## Security Highlights

- JWT authentication with audience claims and BCrypt password hashing
- AES-256-GCM card encryption
- Role-Based Access Control (8 roles)
- Maker-Checker protocol for high-value transfers (>$5,000)
- CORS restricted to 3 specific frontend domains
- CSRF protection with HttpOnly, Secure, SameSite=Strict cookies
- API rate limiting: 5–500 req/min based on user type
- Swagger UI restricted to authorized roles via access tokens

---

## License

NeoBank is open-source under the [MIT License](LICENSE).

---

**Built with ❤️ using Java 21, Spring Boot 3.5.13 LTS, and Spring Cloud Gateway**
