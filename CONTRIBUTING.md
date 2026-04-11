# Contributing to NeoBank Core

Thank you for your interest in contributing to NeoBank Core! This document outlines our principles, expectations, and guidelines for becoming a valued contributor.

---

## 🏛️ Contributor's Manifesto

We, the contributors, pledge to uphold the following principles:

### 1. Code Quality First
- Write clean, readable, and maintainable code
- Follow established Java conventions and project style
- Prefer clarity over cleverness
- Document the "why," not just the "what"

### 2. Architecture Matters
- Respect module boundaries enforced by Spring Modulith
- Design for loose coupling and high cohesion
- Think in terms of domain-driven design
- Keep business logic pure and side-effects isolated

### 3. Testing is Non-Negotiable
- Write tests before or alongside features
- Ensure tests are deterministic and fast
- Use Testcontainers for integration tests
- Maintain high test coverage without chasing metrics
- **Zero-Skip Policy:** The CI pipeline runs `mvn clean install` with **no skipped tests**. Any PR that introduces `@Disabled`, `@Ignore`, or skipped integration tests will be **automatically rejected** by CI and must be fixed before review.

### 3.1. CI Test Enforcement

The GitHub Actions pipeline (`.github/workflows/ci-cd.yml`) enforces:

| Rule | Enforcement | Consequence |
|------|------------|-------------|
| No skipped tests | `mvn clean install` with no `-DskipTests` | Pipeline fails immediately |
| No `@Disabled` tests | Code review + CI | PR automatically rejected |
| Integration tests must pass | Testcontainers + PostgreSQL 17 | Pipeline fails immediately |
| No test regressions | All 2,396+ tests must pass | Pipeline fails immediately |
| Docker build requires passing tests | Docker stage depends on build-test | Images are not built |
| Deployment blocked on failure | Deploy stage needs docker-build | Code is not deployed |

**In short:** If your tests don't pass, your code doesn't ship.

### 4. Performance with Responsibility
- Use virtual threads wisely
- Profile before optimizing
- Consider resource usage in all decisions
- Design for horizontal scalability

### 5. Security by Design
- Never commit secrets or API keys
- Validate all inputs
- Follow principle of least privilege
- Keep dependencies updated

### 6. Documentation is a Feature
- Update README for user-facing changes
- Comment complex business logic
- Keep architecture diagrams current
- Write meaningful commit messages

### 7. Community First
- Be respectful and inclusive
- Help newcomers learn
- Review code constructively
- Share knowledge freely

---

## 📋 How to Contribute

### Reporting Issues

1. **Search first** - Check if the issue already exists
2. **Use templates** - Fill out the issue template completely
3. **Provide context** - Include:
   - Java version
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs or stack traces

### Submitting Pull Requests

1. **Fork the repository**
2. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```

3. **Make your changes** following our coding standards
4. **Write tests** for new functionality
5. **Run all tests** (zero-skip policy):
   ```bash
   mvn clean install
   ```
6. **Generate architecture docs** (if applicable):
   ```bash
   mvn test -Dtest=ArchitectureDocumentationTest
   ```
7. **Commit with clear messages** (see Commit Guidelines)
8. **Push and open a PR** against `main`

---

## 💻 Development Setup

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21+ | Runtime environment (Virtual Threads) |
| Maven | 3.9+ | Build tool |
| Docker | 24+ | Testcontainers and PostgreSQL |
| Git | Latest | Version control |
| kubectl | Latest | Kubernetes deployment (production) |

### Local Development

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/neobank-core.git
cd neobank-core

# Build the project (zero-skip — tests always run)
mvn clean install

# Run tests (requires Docker for Testcontainers)
mvn test

# Run all 9 services locally
./run-all.sh
```

### IDE Setup

- **IntelliJ IDEA** (recommended): Enable auto-import and format on save
- **Eclipse**: Use the Eclipse Formatter plugin
- **VS Code**: Install Extension Pack for Java

---

## 📝 Coding Standards

### Java Conventions

```java
// Class names: PascalCase
public class TransferService { }

// Method names: camelCase
public void processTransfer() { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRIES = 3;

// Variables: camelCase
private int retryCount;

// Use records for immutable data carriers
public record TransferResult(UUID id, Status status) { }

// Use sealed interfaces for restricted hierarchies
public sealed interface TransactionResult
    permits Success, Failure { }
```

### Package Structure

```
com.neobank
├── module-name/
│   ├── api/           # Public interfaces
│   ├── internal/      # Package-private implementations
│   └── web/           # REST controllers
```

### Module Boundaries

- Modules communicate only through public APIs
- Use `@ApplicationModule` to declare dependencies
- Never import from `internal` packages of other modules

### Async Processing

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Async("fraudAnalysisExecutor")
void onEvent(DomainEvent event) {
    // Non-blocking event handling
}
```

### Error Handling

```java
// Use Result types instead of exceptions for expected failures
public TransactionResult transfer(TransferRequest request) {
    return switch (validation) {
        case INVALID -> new TransactionResult.Failure("Invalid request");
        case VALID -> new TransactionResult.Success("Transfer completed");
    };
}
```

---

## 🧪 Testing Guidelines

### Unit Tests
- Test one thing per test method
- Use descriptive names: `shouldDoSomethingWhenCondition()`
- No external dependencies (use mocks)

### Integration Tests
- Use `@Testcontainers` for real PostgreSQL
- Test full request/response cycles
- Verify database state changes

### Test Naming
```java
@Test
void shouldRejectTransfer_WhenInsufficientBalance() { }

@Test
void shouldPublishEvent_AfterSuccessfulTransfer() { }
```

---

## 📬 Commit Guidelines

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code style (formatting) |
| `refactor` | Code refactoring |
| `test` | Adding tests |
| `chore` | Build/config changes |

### Examples
```
feat(fraud): add Ollama support for local AI testing

- Add application-fraud-test.properties
- Add FRAUD_TEST_USE_OPENAI environment flag
- Update docker-compose with test profile

Fixes: #42

---

fix(transfers): prevent race condition in concurrent transfers

- Add pessimistic locking to account fetching
- Use @Transactional for atomic operations

Fixes: #38

---

docs(readme): add Mermaid architecture diagram

- Replace PlantUML with Mermaid for GitHub compatibility
- Add CI badge and prerequisites section
```

---

## 🔍 Code Review Process

1. **Automated Checks**: CI must pass (tests, build)
2. **Review Assignment**: Maintainers will review within 48 hours
3. **Feedback**: Address all comments and suggestions
4. **Approval**: Requires at least one maintainer approval
5. **Merge**: Squash and merge into `main`

### Review Checklist

- [ ] Code follows project conventions
- [ ] Tests cover new functionality
- [ ] Architecture boundaries respected
- [ ] Documentation updated
- [ ] No sensitive data committed
- [ ] Performance implications considered

---

## 🚀 Release Process

Releases follow semantic versioning (`MAJOR.MINOR.PATCH`):

1. **Version bump** in `pom.xml`
2. **Update CHANGELOG.md** with release notes
3. **Tag release** on GitHub
4. **Publish** to Maven Central (future)

---

## 📜 Legal

By contributing to NeoBank Core, you agree that your contributions will be licensed under the project's MIT License.

---

## 🙏 Acknowledgments

This manifesto was inspired by:
- The Java Community
- Spring Framework contributors
- Open Source best practices

**Thank you for making NeoBank Core better!** 💙
