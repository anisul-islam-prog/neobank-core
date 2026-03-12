# NeoBank Core

A next-generation banking platform built with cutting-edge Java technologies and modular architecture. Features AI-powered fraud detection, resilient event-driven design, and strict architectural boundaries.

## High-Level Architecture

```plantuml
@startuml
set separator none
title NeoBankCoreApplication

top to bottom direction

!include <C4/C4>
!include <C4/C4_Context>
!include <C4/C4_Component>

Container_Boundary("NeoBankCoreApplication.NeoBankCoreApplication_boundary", "NeoBankCoreApplication", $tags="") {
  Component(NeoBankCoreApplication.NeoBankCoreApplication.Accounts, "Accounts", $techn="Module", $descr="Account management", $tags="", $link="")
  Component(NeoBankCoreApplication.NeoBankCoreApplication.Transfers, "Transfers", $techn="Module", $descr="Fund transfers with circuit breaker", $tags="", $link="")
  Component(NeoBankCoreApplication.NeoBankCoreApplication.Fraud, "Fraud", $techn="Module", $descr="AI-powered fraud detection", $tags="", $link="")
  Component(NeoBankCoreApplication.NeoBankCoreApplication.Notifications, "Notifications", $techn="Module", $descr="Async notifications", $tags="", $link="")
}

Rel(NeoBankCoreApplication.NeoBankCoreApplication.Fraud, NeoBankCoreApplication.NeoBankCoreApplication.Transfers, "listens to", $techn="", $tags="", $link="")
Rel(NeoBankCoreApplication.NeoBankCoreApplication.Transfers, NeoBankCoreApplication.NeoBankCoreApplication.Accounts, "uses", $techn="", $tags="", $link="")
Rel(NeoBankCoreApplication.NeoBankCoreApplication.Notifications, NeoBankCoreApplication.NeoBankCoreApplication.Transfers, "listens to", $techn="", $tags="", $link="")

SHOW_LEGEND(true)
@enduml
```

### Architecture Overview

- **Accounts Module**: Account creation, retrieval, and balance management
- **Transfers Module**: Atomic fund transfers with Resilience4j circuit breaker protection
- **Notifications Module**: Asynchronous event listener for sending transfer notifications
- **Fraud Module**: AI-powered fraud analysis using Spring AI, listens to transfer events

All modules communicate through Spring Modulith's enforced boundaries, ensuring loose coupling and architectural integrity.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Virtual threads, records, pattern matching |
| **Spring Boot** | 4.0.0 | Application framework |
| **Spring Modulith** | 1.4.0 | Modular architecture enforcement |
| **Spring AI** | 2.0.0-M1 | Hybrid AI (OpenAI/Ollama) for fraud detection |
| **Resilience4j** | 2.3.0 | Circuit breaker pattern for fault tolerance |
| **Spring Data JPA** | - | Database access layer |
| **PostgreSQL** | 17 | Production database |
| **Testcontainers** | 2.0.3 | Integration testing with real PostgreSQL |
| **Micrometer** | - | Metrics and observability (Prometheus) |
| **OpenAPI/Swagger** | 2.8.9 | API documentation |
| **Ollama** | latest | Local AI model runner (llama3.2) |

## Key Features

### Java 25
Leverages the latest Java features for modern, efficient code:
- **Virtual Threads** (Project Loom) - High-throughput concurrency with lightweight threads
- **Records** - Immutable data carriers with concise syntax
- **Pattern Matching** - Enhanced type checking and data extraction

### Spring Modulith
Enforces strict architectural boundaries at runtime:
- Module isolation and dependency validation
- Automated architecture documentation generation
- Prevents architectural drift through verification tests
- **Persistent Event Registry** - Events stored in database until successfully processed

### Spring AI
AI-powered fraud detection:
- OpenAI integration for transaction risk analysis
- Automatic token usage tracking via Micrometer observations
- Configurable risk thresholds with priority alerts

### Resilience4j Circuit Breakers
Fault tolerance for production resilience:
- Automatic circuit breaking when failure rates exceed thresholds
- Graceful degradation with fallback responses
- Self-healing after configurable recovery periods

### PostgreSQL with Testcontainers
Production-grade database testing:
- Real PostgreSQL instances in Docker containers for integration tests
- No local database setup required
- Consistent, reproducible test environments

## Resilience Features

### Event Registry (Spring Modulith)
Domain events are persisted to the `event_publication` table, ensuring:
- **Durability**: Events survive application restarts
- **Reliability**: Failed event listeners are automatically retried
- **Consistency**: Events are only published after transaction commit

Configuration:
```properties
spring.modulith.events.republish-outstanding-events-on-restart=true
spring.modulith.events.replication.period=60
```

### Circuit Breaker (Resilience4j)
The transfer API is protected by a circuit breaker that:
- **Opens** when failure rate exceeds 50% over 10 calls
- **Returns 503** "Service Temporarily Unavailable" when open
- **Half-opens** after 30 seconds to test recovery
- **Closes** when 3 consecutive calls succeed

Configuration:
```properties
resilience4j.circuitbreaker.instances.transfer.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.transfer.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.transfer.minimum-number-of-calls=5
```

### AI Fraud Detection
The Fraud module analyzes every transfer asynchronously:
- Receives `TransferCompletedEvent` after transaction commit
- Sends transaction details to AI for risk scoring (0-100)
- Logs `[FRAUD ALERT]` for scores > 80
- Token usage tracked via Micrometer (`gen_ai.client.token.usage`)

## Hybrid AI Strategy (Local vs. Cloud)

NeoBank supports multiple AI providers through Spring AI's abstraction layer. Switch between cloud-based (OpenAI) and local (Ollama) models using Spring profiles.

### Supported Providers

| Provider | Model | Use Case | Cost | Latency |
|----------|-------|----------|------|---------|
| **OpenAI** | gpt-4o-mini | Production, highest accuracy | Pay-per-token | ~500ms |
| **Ollama** | llama3.2 | Local development, offline | Free | ~100ms |

---

## Switching AI Providers: Complete Guide

### Quick Start

```bash
# Development (Local/Ollama) - Default
./mvnw spring-boot:run

# Production (Cloud/OpenAI)
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run -Dspring-boot.run.profiles=openai
```

---

### Method 1: Maven Command Line (Recommended for Development)

```bash
# Run with Ollama (Local AI)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Run with OpenAI (Cloud AI)
export OPENAI_API_KEY=your-api-key-here
./mvnw spring-boot:run -Dspring-boot.run.profiles=openai
```

---

### Method 2: Java JAR Command (Production Deployment)

```bash
# Run with Ollama (Local AI)
java -jar target/neobank-core-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=local

# Run with OpenAI (Cloud AI)
java -jar target/neobank-core-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=openai \
    --spring.ai.openai.api-key=your-api-key-here
```

---

### Method 3: Environment Variable

```bash
# Set profile via environment
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run

# Or for OpenAI
export SPRING_PROFILES_ACTIVE=openai
export OPENAI_API_KEY=your-api-key-here
./mvnw spring-boot:run
```

---

### Method 4: Permanent Configuration Change

Edit `src/main/resources/application.properties`:

```properties
# Change this line to switch default provider
spring.profiles.active=local    # or 'openai' for cloud-first
```

---

### Method 5: Docker Compose Profiles

```bash
# Local Development (includes Ollama container)
docker-compose --profile local up -d

# Production with OpenAI
export OPENAI_API_KEY=your-api-key-here
docker-compose --profile openai up -d
```

**What each profile starts:**

| Profile | Containers | Ports | Best For |
|---------|-----------|-------|----------|
| `local` | PostgreSQL, Ollama, NeoBank | 5432, 11434, 8080 | Development, testing |
| `openai` | PostgreSQL, NeoBank | 5432, 8081 | Production, CI/CD |

---

### Verification

After starting, verify the active profile:

```bash
# Check application info endpoint
curl http://localhost:8080/actuator/info | jq .

# Check logs for provider initialization
docker logs neobank-core | grep -i "ollama\|openai"
```

Expected log output for **local** profile:
```
Using Ollama Chat API at http://localhost:11434
Model: llama3.2
```

Expected log output for **openai** profile:
```
Using OpenAI Chat API
Model: gpt-4o-mini
```

---

### Fraud Detection Test

Trigger a transfer to test fraud analysis:

```bash
# Create accounts first
curl -X POST http://localhost:8080/api/accounts \
    -H "Content-Type: application/json" \
    -d '{"ownerName": "Alice", "balance": 1000}'

curl -X POST http://localhost:8080/api/accounts \
    -H "Content-Type: application/json" \
    -d '{"ownerName": "Bob", "balance": 500}'

# Then transfer and watch fraud analysis in logs
curl -X POST http://localhost:8080/api/transfers \
    -H "Content-Type: application/json" \
    -d '{"fromId": "<alice-id>", "toId": "<bob-id>", "amount": 100}'

# Check fraud logs
docker logs neobank-core | grep -i "fraud\|risk"
```

---

### Troubleshooting

**Ollama not responding:**
```bash
# Pull model manually
docker exec -it neobank-ollama ollama pull llama3.2

# Verify Ollama is running
curl http://localhost:11434/api/tags
```

**OpenAI API errors:**
```bash
# Verify API key is set
echo $OPENAI_API_KEY

# Test OpenAI connectivity
curl https://api.openai.com/v1/models \
    -H "Authorization: Bearer $OPENAI_API_KEY"
```

**Check active profile:**
```bash
# View boot log for profile info
docker logs neobank-core | grep "Active profiles"
```

---

### Cost Considerations

| Aspect | Local (Ollama) | Cloud (OpenAI) |
|--------|----------------|----------------|
| **Setup Cost** | None (uses local GPU/CPU) | API key required |
| **Per-Request Cost** | $0 | ~$0.0001-0.001 per transfer |
| **Hardware** | 8GB RAM minimum | None |
| **Accuracy** | Good for standard patterns | Higher for edge cases |
| **Privacy** | All data stays local | Data sent to OpenAI |

**Recommendation:** Use **local** for development and testing. Switch to **openai** for production where higher accuracy justifies the cost.

## Getting Started

### Prerequisites

- Java 25
- Maven
- Docker (for Testcontainers and docker-compose)
- OpenAI API key (for cloud mode) OR Ollama installed (for local mode)

### Environment Variables

```bash
# Required for OpenAI/cloud mode
export OPENAI_API_KEY=your-api-key-here

# PostgreSQL configuration (optional, has defaults)
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
export POSTGRES_URL=jdbc:postgresql://localhost:5432/neobank
```

### Build and Test

```bash
./mvnw clean test
```

---

### Fraud Detection Tests: AI Provider Configuration

Tests can run with either **Ollama (local)** or **OpenAI (cloud)** for fraud detection validation.

#### Default: Test with Ollama (Docker)

```bash
# Start Ollama in Docker (required for local testing)
docker-compose --profile test up -d ollama

# Wait for Ollama to be ready (pulls llama3.2 on first run)
docker logs neobank-ollama -f

# Run tests with Ollama (default)
FRAUD_TEST_USE_OPENAI=false ./mvnw clean test
```

#### Alternative: Test with OpenAI API

```bash
# Run tests with OpenAI (requires valid API key)
export OPENAI_API_KEY=sk-...
FRAUD_TEST_USE_OPENAI=true ./mvnw clean test
```

#### Test Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `FRAUD_TEST_USE_OPENAI` | `false` | Set to `true` to use OpenAI for tests |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama endpoint for local tests |
| `OPENAI_API_KEY` | `test-key` | Required when `FRAUD_TEST_USE_OPENAI=true` |

#### Test Behavior

- **Ollama mode** (`FRAUD_TEST_USE_OPENAI=false`):
  - Connects to Ollama at `http://localhost:11434`
  - Uses `llama3.2` model
  - No API costs
  - Requires Docker with Ollama container running

- **OpenAI mode** (`FRAUD_TEST_USE_OPENAI=true`):
  - Connects to OpenAI API
  - Uses `gpt-4o-mini` model
  - Incurs API costs (~$0.0001-0.001 per test)
  - Requires valid `OPENAI_API_KEY`

#### Troubleshooting Test Failures

**Connection refused to Ollama:**
```bash
# Ensure Ollama container is running
docker-compose --profile test up -d ollama

# Verify Ollama is accessible
curl http://localhost:11434/api/tags

# Check Ollama has the model
docker exec neobank-ollama ollama run llama3.2 'hello'
```

**OpenAI API errors in tests:**
```bash
# Verify API key is valid
export OPENAI_API_KEY=sk-...
curl https://api.openai.com/v1/models -H "Authorization: Bearer $OPENAI_API_KEY"

# Re-run tests
FRAUD_TEST_USE_OPENAI=true ./mvnw clean test
```

---

### Run with Docker Compose

```bash
docker-compose up -d
```

### Run the Application (Local)

```bash
./mvnw spring-boot:run
```

### API Documentation

OpenAPI documentation is auto-generated via springdoc-openapi:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Architecture Documentation

C4/PlantUML diagrams are generated via Spring Modulith's `Documenter`:

```bash
./mvnw test -Dtest=ArchitectureDocumentationTest
```

Generated docs are output to `target/modulith-docs/`.

## Project Structure

```
com.neobank
├── NeoBankCoreApplication.java
├── accounts/
│   ├── Account.java (Record)
│   ├── AccountEntity.java
│   ├── AccountRepository.java
│   ├── AccountService.java
│   └── api/AccountApi.java
├── transfers/
│   ├── TransferRequest.java (Record)
│   ├── TransactionResult.java (Record)
│   ├── TransferCompletedEvent.java (Record)
│   ├── api/TransferApi.java
│   ├── internal/ (package-private implementation)
│   └── web/TransferController.java
├── notifications/
│   └── NotificationService.java
└── fraud/
    ├── FraudListener.java
    └── FraudAnalysisConfig.java
```

## Module Boundaries

Spring Modulith enforces that modules only communicate through their public APIs:

- **accounts**: Account creation, retrieval, and management
- **transfers**: Fund transfers with atomic transactions and event publishing
- **notifications**: Asynchronous event listeners for side effects
- **fraud**: AI-powered fraud analysis (listens to transfers)

## Observability

### Metrics
Micrometer with Prometheus registry tracks:
- Transfer rate (transfers per second)
- Circuit breaker state transitions
- Event publication success/failure rates
- AI token usage (`gen_ai.client.token.usage`)

Access metrics at: `http://localhost:8080/actuator/prometheus`

### Tracing
- Micrometer tracing enabled for all AI operations
- Token counts tracked per transaction
- Configurable observation include/exclude settings

## Docker Deployment

### Build Docker Image

```bash
docker build -t neobank-core:latest .
```

### Run with Docker Compose

```bash
# Local development (Ollama + NeoBank on port 8080)
docker-compose --profile local up -d

# Production (OpenAI + NeoBank on port 8081)
export OPENAI_API_KEY=your-api-key
docker-compose --profile openai up -d
```

### Services

| Profile | Services | Ports |
|---------|----------|-------|
| **local** | PostgreSQL, Ollama, NeoBank Core | 5432, 11434, 8080 |
| **openai** | PostgreSQL, NeoBank Core | 5432, 8081 |

### Health Checks

- **PostgreSQL**: `pg_isready -U postgres`
- **Ollama**: Verifies model pull and availability
- **NeoBank Core**: `GET /actuator/health`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./mvnw clean test`
5. Submit a pull request

## License

MIT License
