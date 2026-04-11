# NeoBank 2026 — System Documentation

> **Platform:** Spring Boot 3.5.13 LTS | **Java:** 21 (Virtual Threads) | **Architecture:** Hybrid Modular Monolith
> 
> **Last Updated:** April 2026 | **Version:** 2026.1

---

## Table of Contents

1. [System Architecture Overview](#system-architecture-overview)
2. [Service Port Map](#service-port-map)
3. [Gateway-to-Service Communication Flow](#gateway-to-service-communication-flow)
4. [Security Architecture](#security-architecture)
5. [Observability Guide](#observability-guide)
6. [Database Schema Map](#database-schema-map)
7. [API & Integration Catalog](#api--integration-catalog)
8. [Inter-Service Event Map](#inter-service-event-map)

---

## System Architecture Overview

### Hybrid Modular Monolith

NeoBank uses a **Hybrid Modular Monolith** architecture — a middle ground between a traditional monolith and a distributed microservices mesh.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                           │
│               Spring Cloud Gateway (WebFlux / Reactive)             │
│  ┌─────────────┬──────────────┬───────────────┬──────────────────┐  │
│  │ Resilience4j│  Rate Limiter│  JWT Validator │  Trace Propagator│  │
│  └─────────────┴──────────────┴───────────────┴──────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP Routes (per /api/** path)
                               ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│   Auth   │  │Onboarding│  │CoreBank. │  │ Lending  │  │  Cards   │
│  :8081   │  │  :8082   │  │  :8083   │  │  :8084   │  │  :8085   │
│ (Servlet)│  │ (Servlet)│  │ (Servlet)│  │+SpringAI │  │ (Servlet)│
└──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘
                                                      
┌──────────┐  ┌──────────┐  ┌──────────┐
│  Fraud   │  │  Batch   │  │Analytics │
│  :8086   │  │  :8087   │  │  :8088   │
│+SpringAI │  │ (Spring  │  │  (CQRS   │
│          │  │  Batch)  │  │  Reader) │
└──────────┘  └──────────┘  └──────────┘
       │              │              │
       └──────────────┴──────────────┘
                      │
         ┌────────────┴────────────┐
         │    PostgreSQL (17)      │
         │  8 Isolated Schemas     │
         │  schema_core, schema_   │
         │  auth, schema_onboard.. │
         └─────────────────────────┘
```

### Why Hybrid?

| Aspect | Monolith | Microservices | **NeoBank Hybrid** |
|--------|----------|---------------|---------------------|
| Deployment | Single artifact | N independent artifacts | 9 independent services |
| Database | Shared | Per-service | Per-schema (shared DB instance) |
| Team ownership | None | Per-team | Per-module |
| Complexity | Low | High | **Medium** |
| Failure isolation | None | Full | **Circuit breakers per route** |
| Transaction management | ACID | Sagas | **ACMD per-schema** |

Each service is independently deployable with its own port, schema, and module boundary, but shares a single PostgreSQL instance with isolated schemas. The Gateway routes traffic and provides failure isolation via Resilience4j circuit breakers.

### Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Runtime** | Java / Temurin | 21 (Virtual Threads) |
| **Framework** | Spring Boot | 3.5.13 LTS |
| **Gateway** | Spring Cloud Gateway | 2025.0.0 (Northfields) |
| **Security** | Spring Security | 6.4+ |
| **Resilience** | Resilience4j | 2.4.0 |
| **AI/ML** | Spring AI + OpenAI | 1.0.0-M4 |
| **Database** | PostgreSQL | 17 |
| **ORM** | Hibernate / Spring Data JPA | 6.6 / 3.5 |
| **Tracing** | OpenTelemetry + Micrometer | 1.48 / 1.6 |
| **Metrics** | Prometheus + Grafana | Latest |
| **Build** | Maven | 3.9+ |
| **Container** | Docker / Docker Compose | 24+ |

---

## Service Port Map

| Service | Port | Protocol | Framework | Database Schema | Key Dependencies |
|---------|------|----------|-----------|-----------------|------------------|
| **API Gateway** | 8080 | WebFlux (Reactive) | Spring Cloud Gateway | N/A (proxy only) | Resilience4j, JWT, Bucket4j |
| **Auth** | 8081 | Servlet (WebMvc) | Spring Security + Modulith | `schema_auth` | JWT (jjwt), BCrypt |
| **Onboarding** | 8082 | Servlet (WebMvc) | Spring Modulith | `schema_onboarding` | Auth, Core Banking |
| **Core Banking** | 8083 | Servlet (WebMvc) | Spring Modulith | `schema_core` | Resilience4j |
| **Lending** | 8084 | Servlet (WebMvc) | Spring Modulith | `schema_loans` | **Spring AI OpenAI**, Auth, Core Banking |
| **Cards** | 8085 | Servlet (WebMvc) | Spring Modulith | `schema_cards` | Auth, Core Banking |
| **Fraud** | 8086 | Servlet (WebMvc) | Spring Modulith | `schema_fraud` | **Spring AI OpenAI**, Core Banking |
| **Batch** | 8087 | Servlet (WebMvc) | Spring Batch | `schema_core` | Core Banking |
| **Analytics** | 8088 | Servlet (WebMvc) | Spring Modulith (CQRS) | `schema_analytics` | Core Banking, Cards |

---

## Gateway-to-Service Communication Flow

### Request Lifecycle

```
Client (Browser/Mobile)
    │
    ▼ 1. HTTP Request + JWT Token
┌─────────────────────────────────┐
│   Gateway (8080)                │
│                                 │
│   1. Rate Limit Check (Bucket4j)│
│   2. JWT Validation             │
│   3. Trace ID Generation        │
│   4. Route Lookup               │
│   5. Circuit Breaker Check      │
└──────────┬──────────────────────┘
           │ 2. Forwarded Request + X-Trace-Id
           ▼
┌─────────────────────────────────┐
│   Target Service (8081-8088)    │
│                                 │
│   1. JWT Re-validation          │
│   2. Role-based Authorization   │
│   3. Business Logic             │
│   4. Database Operation         │
│   5. Event Publication          │
└──────────┬──────────────────────┘
           │ 3. Response + Service Headers
           ▼
┌─────────────────────────────────┐
│   Gateway (8080)                │
│                                 │
│   1. Add X-Service-Name header  │
│   2. Collect trace data         │
│   3. Return to client           │
└──────────┬──────────────────────┘
           │ 4. Final Response
           ▼
        Client
```

### Route Table

| Gateway Path | Target Service | Port | Circuit Breaker | Retry |
|-------------|---------------|------|-----------------|-------|
| `/api/auth/**` | Auth | 8081 | — | 2 attempts |
| `/api/onboarding/**` | Onboarding | 8082 | `onboardingCircuitBreaker` | 2 attempts |
| `/api/accounts/**` | Core Banking | 8083 | `coreBankingCircuitBreaker` | 2 attempts |
| `/api/transfers/**` | Core Banking | 8083 | `coreBankingCircuitBreaker` | 2 attempts |
| `/api/loans/**` | Lending | 8084 | `lendingCircuitBreaker` | 2 attempts |
| `/api/cards/**` | Cards | 8085 | `cardsCircuitBreaker` | 2 attempts |
| `/api/fraud/**` | Fraud | 8086 | `fraudCircuitBreaker` | 2 attempts |
| `/api/batch/**` | Batch | 8087 | — | 2 attempts |
| `/api/analytics/**` | Analytics | 8088 | `analyticsCircuitBreaker` | 2 attempts |

### Resilience4j Configuration

Each circuit breaker has specific thresholds tuned to the service's criticality:

| Circuit Breaker | Failure Threshold | Open State Duration | Half-Open Calls | Sliding Window |
|----------------|-------------------|--------------------|-----------------|----------------|
| `coreBankingCircuitBreaker` | 50% | 30s | 3 | 10 calls |
| `lendingCircuitBreaker` | 50% | 30s | 3 | 10 calls |
| `cardsCircuitBreaker` | 50% | 30s | 3 | 10 calls |
| `onboardingCircuitBreaker` | 60% | 30s | 3 | 10 calls |
| `fraudCircuitBreaker` | 40% | 45s | 3 | 10 calls |
| `analyticsCircuitBreaker` | 40% | 60s | 2 | 5 calls |

---

## Security Architecture

### JWT Authentication Flow

```
┌─────────┐         ┌─────────┐         ┌──────────────┐
│ Client  │  ──1──> │Gateway  │  ──2──> │ Auth (8081)  │
│         │         │(8080)   │         │              │
│         │  <──5── │         │  <──3── │              │
│         │         │         │         │              │
│         │  ──6──> │         │  ──7──> │ Target Svc   │
└─────────┘         └─────────┘         └──────────────┘

1. POST /api/auth/login {username, password}
2. Gateway forwards to Auth /api/auth/login
3. Auth validates credentials, generates JWT
4. Auth returns JWT to Gateway
5. Gateway returns JWT to client
6. Subsequent requests include: Authorization: Bearer <JWT>
7. Gateway validates JWT signature, extracts claims,
   forwards to target service with JWT intact
```

### JWT Token Structure

```json
{
  "sub": "user-uuid-here",
  "username": "johndoe",
  "roles": ["ROLE_CUSTOMER_RETAIL"],
  "status": "ACTIVE",
  "iat": 1712592000,
  "exp": 1712595600,
  "iss": "http://localhost:8081"
}
```

| Claim | Type | Description |
|-------|------|-------------|
| `sub` | String | User UUID (primary identifier) |
| `username` | String | Login username |
| `roles` | Array<String> | RBAC roles (prefixed with `ROLE_`) |
| `status` | String | Account status: `PENDING`, `ACTIVE`, `REJECTED`, `SUSPENDED` |
| `iat` | Long | Issued-at timestamp (epoch seconds) |
| `exp` | Long | Expiration timestamp (1 hour from issue) |
| `iss` | String | Token issuer URI (Auth service URL) |

### Role Hierarchy

```
SYSTEM_ADMIN (highest)
    ├── AUDITOR
    ├── MANAGER
    │   ├── RELATIONSHIP_OFFICER
    │   │   └── TELLER
    │   └── CUSTOMER_BUSINESS
    └── CUSTOMER_RETAIL (lowest)
```

| Role | Access Scope | Example Endpoints |
|------|-------------|-------------------|
| `SYSTEM_ADMIN` | Full system access | All endpoints |
| `AUDITOR` | Audit logs, compliance reports | `/api/audit/**` |
| `MANAGER` | Loan approvals, staff oversight | `/api/loans/approve/**`, `/api/auth/users/*/approve` |
| `RELATIONSHIP_OFFICER` | Customer portfolio, loan recommendations | `/api/accounts/search/**` |
| `TELLER` | Customer lookup, basic transactions | `/api/accounts/search/**` |
| `CUSTOMER_BUSINESS` | Business accounts, commercial operations | `/api/accounts/**`, `/api/transfers/**` |
| `CUSTOMER_RETAIL` | Personal banking | `/api/accounts/**`, `/api/transfers/**` |

### Gateway Security Filter Chain

```
Incoming Request
    │
    ▼
DisableEncodeUrlFilter
    ▼
WebAsyncManagerIntegrationFilter
    ▼
SecurityContextHolderFilter
    ▼
HeaderWriterFilter (CSP, X-Frame-Options, etc.)
    ▼
CorsFilter (allowed origins: localhost:3000-3002)
    ▼
RateLimitingFilter (Bucket4j — per-IP and per-endpoint)
    ▼
JwtAuthenticationFilter (validates JWT, extracts claims)
    ▼
LogoutFilter
    ▼
RequestCacheAwareFilter
    ▼
SecurityContextHolderAwareRequestFilter
    ▼
AnonymousAuthenticationFilter
    ▼
SessionManagementFilter (STATELESS — no sessions)
    ▼
ExceptionTranslationFilter
    ▼
AuthorizationFilter (role-based path matching)
    ▼
Target Service
```

### Cookie Security

All cookies issued by the system follow these rules:

| Property | Value | Reason |
|----------|-------|--------|
| `HttpOnly` | `true` (JWT, session) | Prevents XSS theft |
| `Secure` | `true` | HTTPS-only transmission |
| `SameSite` | `Strict` | CSRF prevention |
| `Domain` | `neobank.com` | Scoped to neoBank domain |
| `Max-Age` | 3600s (1 hour) | Matches JWT expiration |

---

## Observability Guide

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    All 9 Services                           │
│  Each service sends traces + metrics via OTLP HTTP         │
│  Endpoint: http://otel-collector:4318/v1/traces            │
└────────────────────────┬────────────────────────────────────┘
                         │ OTLP (HTTP 4318)
                         ▼
              ┌──────────────────────┐
              │  OTel Collector      │
              │  (otel-contrib)      │
              │                      │
              │  Receivers: otlp     │
              │  Processors: batch   │
              │  Exporters:          │
              │   → Jaeger (traces) │
              │   → Prometheus      │
              │     (metrics)       │
              └──┬──────────────┬───┘
                 │              │
                 ▼              ▼
         ┌──────────────┐ ┌──────────┐
         │   Jaeger     │ │Prometheus│
         │   :16686     │ │  :9090   │
         └──────────────┘ └────┬─────┘
                               │
                               ▼
                        ┌──────────┐
                        │ Grafana  │
                        │  :3003   │
                        └──────────┘
```

### Tracing with Jaeger

Every request generates a unique `X-Trace-Id` at the Gateway that propagates through all downstream services.

**Access Jaeger:** http://localhost:16686

#### How to Trace a Request

1. Make a request through the Gateway:
   ```bash
   curl -v http://localhost:8080/api/accounts/{id} \
     -H "Authorization: Bearer $TOKEN"
   ```

2. Note the `X-Trace-Id` from the response header.

3. Open Jaeger at http://localhost:16686

4. Search by:
   - **Service:** `neobank-gateway`
   - **Tag:** `x-trace-id=<the-trace-id>`

5. View the complete trace span:
   ```
   Gateway (8080) → Core Banking (8083) → Database
   ├── span: gateway.route (2ms)
   ├── span: jwt.validate (1ms)
   ├── span: circuit.breaker.check (0ms)
   ├── span: forward.to.core.banking (15ms)
   │   ├── span: account.findById (5ms)
   │   ├── span: db.query (3ms)
   │   └── span: serialize.response (2ms)
   └── span: gateway.response (1ms)
   ```

### Metrics with Prometheus

**Access Prometheus:** http://localhost:9090

#### Key Metrics Available

| Metric Name | Type | Description |
|------------|------|-------------|
| `http_server_requests_seconds_count` | Counter | Total HTTP requests by service, method, status |
| `http_server_requests_seconds_sum` | Histogram | Total request duration |
| `resilience4j_circuitbreaker_state` | Gauge | Current CB state (0=closed, 1=open, 2=half-open) |
| `resilience4j_circuitbreaker_calls_total` | Counter | CB call outcomes (success, failure, rejected) |
| `jvm_memory_used_bytes` | Gauge | JVM memory usage |
| `jvm_gc_pause_seconds` | Histogram | GC pause duration |
| `disk_free_bytes` | Gauge | Available disk space |
| `db_connections_active` | Gauge | Active database connections |

#### Useful Prometheus Queries

```promql
# Error rate per service (5xx responses / total)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service)
/
sum(rate(http_server_requests_seconds_count[5m])) by (service)

# P99 latency per service
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket[5m])) by (service, le)
)

# Circuit breaker state (1 = OPEN = bad)
resilience4j_circuitbreaker_state

# JVM memory pressure
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

### Dashboards with Grafana

**Access Grafana:** http://localhost:3003 (admin / admin123)

### Key Alerts to Monitor

| Alert Name | Condition | Severity | Action |
|-----------|-----------|----------|--------|
| **Gateway 5xx Spike** | > 5% of requests return 5xx over 5 min | **Critical** | Check downstream service health, review circuit breaker states |
| **Circuit Breaker Open** | Any circuit breaker transitions to OPEN state | **Warning** | Investigate target service, check logs for root cause |
| **High P99 Latency** | P99 response time exceeds 2 seconds over 10 min | **Warning** | Review database query performance, check connection pool saturation |
| **Database Connection Exhaustion** | Active connections > 80% of max pool size | **Critical** | Increase pool size, investigate connection leaks |
| **Disk Space Low** | Free disk space < 10% on any service host | **Warning** | Clean logs, review disk usage, expand storage |
| **AI Service Degradation** | OpenAI error rate > 10% over 5 min | **Warning** | Check fallback activation, verify OpenAI status |

---

## Database Schema Map

### PostgreSQL 17 — 8 Isolated Schemas

All services share a single PostgreSQL instance but use isolated schemas for data separation:

```
neobank (database)
├── schema_core          ─── Core Banking (accounts, transfers, branches, approvals)
├── schema_auth          ─── Authentication (users, credentials, sessions)
├── schema_onboarding    ─── Onboarding (KYC records, approval workflows)
├── schema_loans         ─── Lending (loan applications, amortization, credit scores)
├── schema_cards         ─── Cards (card details, issuance records)
├── schema_fraud         ─── Fraud (blacklist, fraud events, analysis configs)
├── schema_analytics     ─── Analytics (CQRS read models, BI tables)
└── schema_public        ─── Shared (Liquibase changelogs, if any)
```

### Schema Ownership

| Schema | Owner Service | Tables | Description |
|--------|--------------|--------|-------------|
| `schema_core` | Core Banking (8083) | `accounts`, `transactions`, `branches`, `authorization_approvals` | Account balances, transfers, branch data, approval workflows |
| `schema_auth` | Auth (8081) | `users`, `credentials` | User profiles, password hashes, roles, status |
| `schema_onboarding` | Onboarding (8082) | `user_profiles`, `kyc_records`, `approval_events` | Customer onboarding state, KYC data, staff approval history |
| `schema_loans` | Lending (8084) | `loan_applications`, `amortization_schedules`, `credit_scores`, `loan_accounts` | Loan lifecycle data, payment schedules, credit evaluations |
| `schema_cards` | Cards (8085) | `card_details`, `card_issuance_records` | Virtual/physical card data, issuance status |
| `schema_fraud` | Fraud (8086) | `blacklist_entries`, `fraud_events`, `fraud_analysis_configs` | Blocked entities, fraud incidents, AI analysis configurations |
| `schema_analytics` | Analytics (8088) | `transaction_read_models`, `user_metrics`, `bi_dashboards` | Denormalized read-optimized data for BI and dashboards |

### Cross-Schema References

```
schema_auth.users
    │
    ├──> schema_core.accounts         (owner_id FK)
    ├──> schema_onboarding.user_profiles  (user_id FK)
    ├──> schema_loans.loan_applications   (applicant_id FK)
    └──> schema_cards.card_details        (holder_id FK)

schema_core.accounts
    │
    ├──> schema_core.transactions         (source/target account FK)
    ├──> schema_loans.loan_accounts       (linked_account FK)
    └──> schema_analytics.transaction_read_models  (event-sourced copy)
```

### Redis Caching Strategy (Auth Sessions)

| Cache Key Pattern | TTL | Content | Purpose |
|------------------|-----|---------|---------|
| `auth:session:{sessionId}` | 30 min | Session context, user ID, last activity | Active session tracking |
| `auth:jwt:blacklist:{jti}` | Until expiry | Revoked token IDs | Token invalidation on logout |
| `auth:ratelimit:ip:{ip}:{endpoint}` | 1 min | Request counter | Per-IP rate limiting |
| `auth:cache:user:{userId}` | 5 min | User profile snapshot | Reduce DB lookups for repeated auth checks |

**Cache Invalidation:**
- On user role change → invalidate `auth:cache:user:{userId}`
- On logout → add JTI to `auth:jwt:blacklist:{jti}`, delete session
- On password change → invalidate all user sessions and cache entries

---

## API & Integration Catalog

### Swagger Aggregation via Gateway

All 9 services expose OpenAPI 3.0 documentation via `springdoc-openapi`. The Gateway aggregates these into a unified Swagger UI.

**Access:** http://localhost:8080/swagger-ui.html

#### Service Documentation Paths

| Service | Swagger Path | OpenAPI JSON |
|---------|-------------|--------------|
| Gateway | `/swagger-ui.html` | `/v3/api-docs` |
| Auth | — (proxied via Gateway) | — |
| Core Banking | — (proxied via Gateway) | — |
| All services | Aggregated at Gateway | Collected via Gateway route inspection |

> **Note:** In the current architecture, Swagger UI is served by the Gateway itself. Individual service documentation can be accessed by visiting each service's `/swagger-ui.html` directly (e.g., http://localhost:8081/swagger-ui.html for Auth).

### API Endpoints Summary

#### Public Endpoints (No Authentication)

| Method | Path | Service | Description |
|--------|------|---------|-------------|
| `POST` | `/api/auth/register` | Auth (8081) | Register new user account |
| `POST` | `/api/auth/login` | Auth (8081) | Authenticate and receive JWT |
| `GET` | `/actuator/health` | Gateway (8080) | System health check |
| `GET` | `/actuator/info` | Gateway (8080) | Service metadata |

#### Authenticated Endpoints (JWT Required)

| Method | Path | Service | Required Role | Description |
|--------|------|---------|---------------|-------------|
| `GET` | `/api/accounts/{id}` | Core Banking (8083) | `authenticated` | Get account details |
| `POST` | `/api/transfers` | Core Banking (8083) | `authenticated` | Initiate transfer (may require approval >$5K) |
| `GET` | `/api/cards` | Cards (8085) | `authenticated` | List user's cards |
| `POST` | `/api/loans/apply` | Lending (8084) | `authenticated` | Submit loan application |
| `GET` | `/api/onboarding/status` | Onboarding (8082) | `authenticated` | Check onboarding progress |

#### Admin/Staff Endpoints

| Method | Path | Service | Required Role | Description |
|--------|------|---------|---------------|-------------|
| `GET` | `/api/auth/users/pending` | Auth (8081) | `MANAGER` | List users awaiting approval |
| `POST` | `/api/auth/users/{id}/approve` | Auth (8081) | `MANAGER` | Approve user account |
| `GET` | `/api/transfers/pending` | Core Banking (8083) | `MANAGER` | List transfers awaiting approval |
| `POST` | `/api/transfers/{id}/approve` | Core Banking (8083) | `MANAGER` | Approve large transfer |
| `GET` | `/api/audit/events` | Auth (8081) | `AUDITOR` | View audit log |

---

## Inter-Service Event Map

NeoBank uses **Spring Modulith events** for asynchronous inter-service communication. Events are published within a transaction and delivered reliably via Spring's event system.

### Event Flow Diagram

```
┌─────────────────┐
│   Onboarding    │
│   User Created  │
└────────┬────────┘
         │ UserAccountRequestedEvent
         ▼
┌─────────────────┐     ┌──────────────────┐
│     Auth        │────>│  Core Banking    │
│  (creates user) │     │ (creates account)│
└────────┬────────┘     └────────┬─────────┘
         │ UserApprovedEvent     │ AccountCreatedEvent
         ▼                       ▼
┌─────────────────┐     ┌──────────────────┐
│   Onboarding    │     │    Analytics     │
│ (KYC complete)  │     │  (BI update)     │
└─────────────────┘     └──────────────────┘


┌─────────────────┐
│  Core Banking   │
│  Transfer Made  │
└────────┬────────┘
         │ TransferCompletedEvent
         ▼
┌─────────────────┐     ┌──────────────────┐
│    Fraud        │     │   Analytics      │
│ (AI analysis)   │     │  (CQRS sync)     │
└────────┬────────┘     └──────────────────┘
         │ FraudAlertEvent (if flagged)
         ▼
┌─────────────────┐
│  Admin Console  │
│  (manual review)│
└─────────────────┘
```

### Event Catalog

| Event Name | Publisher | Consumers | Description | Payload |
|-----------|-----------|-----------|-------------|---------|
| `UserAccountRequestedEvent` | Onboarding | Auth | New user registration initiated | `{userId, username, email, role}` |
| `UserCreatedEvent` | Auth | Onboarding, Core Banking | User account created in auth system | `{userId, username, roles[], status}` |
| `UserApprovedEvent` | Auth | Onboarding, Core Banking | Staff approved a user account | `{userId, approvedBy, timestamp}` |
| `AccountCreatedEvent` | Core Banking | Analytics | New bank account opened | `{accountId, ownerId, type, balance}` |
| `TransferCompletedEvent` | Core Banking | Fraud, Analytics | Money transfer completed | `{transferId, from, to, amount, timestamp}` |
| `TransferPendingApproval` | Core Banking | — | Transfer >$5K requires manager approval | `{transferId, amount, initiator}` |
| `TransferApprovedEvent` | Core Banking | Analytics | Manager approved a transfer | `{transferId, approvedBy, timestamp}` |
| `FraudAlertEvent` | Fraud | Admin Console, Batch | Suspicious activity detected | `{eventId, transactionId, riskScore, reason}` |
| `CardIssuedEvent` | Cards | Analytics | New card issued to customer | `{cardId, userId, type, status}` |
| `LoanApplicationSubmitted` | Lending | Analytics | New loan application received | `{loanId, userId, amount, purpose}` |
| `CreditScoreUpdated` | Lending | Analytics | Credit score changed (AI or manual) | `{userId, oldScore, newScore, reason}` |
| `ReconciliationAlert` | Batch | Admin Console | Account balance mismatch detected | `{accountId, expected, actual, difference}` |

### Event Delivery Guarantees

| Property | Behavior |
|----------|----------|
| **Delivery** | At-least-once (events republished on restart) |
| **Ordering** | Per-aggregate ordered (within same transaction) |
| **Idempotency** | Consumers must handle duplicate events |
| **Retention** | Events stored in `schema_core` event store table |
| **Monitoring** | Event publication visible in actuator `/modulith` endpoint |

---

## Runbooks & Operations

### Service Startup Order

When starting the full stack manually:

```
1. PostgreSQL (infrastructure)
2. Auth (8081) — wait for HEALTHY
3. Core Banking (8083) — wait for HEALTHY
4. Onboarding (8082) — depends on Auth
5. Lending (8084) — depends on Auth + Core
6. Cards (8085) — depends on Auth + Core
7. Fraud (8086) — depends on Core
8. Batch (8087) — depends on Core
9. Analytics (8088) — depends on Core + Cards
10. Gateway (8080) — starts last, routes to all
```

### Quick Health Check Script

```bash
#!/bin/bash
# Run: ./health-check.sh
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088; do
  status=$(curl -sf http://localhost:$port/actuator/health | jq -r '.status' 2>/dev/null)
  printf "%-5s %-20s %s\n" ":$port" "${status:-DOWN}" "$(date +%H:%M:%S)"
done
```

### Log Locations

| Context | Log Location |
|---------|-------------|
| **Local (run-all.sh)** | `logs/<service>.log` |
| **Docker** | `docker logs neobank-<service>` |
| **Centralized (future)** | Grafana Loki / ELK via OTel logs pipeline |

---

*This document is maintained by the NeoBank engineering team. Last reviewed: April 2026.*
*For incident response, see [DISASTER_RECOVERY.md](./DISASTER_RECOVERY.md).*
