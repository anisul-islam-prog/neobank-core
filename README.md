# Modern Neo-Bank Core

A next-generation banking platform built with cutting-edge Java technologies and modular architecture.

## Key Technologies

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

### PostgreSQL with Testcontainers
Production-grade database testing:
- Real PostgreSQL instances in Docker containers for integration tests
- No local database setup required
- Consistent, reproducible test environments

## Getting Started

### Prerequisites

- Java 25
- Maven
- Docker (for Testcontainers)

### Build and Test

```bash
./mvnw clean test
```

### Run the Application

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
└── transfers/
    ├── TransferRequest.java (Record)
    ├── TransactionResult.java (Record)
    └── TransferService.java
```

## Module Boundaries

- **accounts**: Account creation, retrieval, and management
- **transfers**: Fund transfers with atomic transactions

Spring Modulith enforces that modules only communicate through their public APIs.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./mvnw clean test`
5. Submit a pull request
