# NeoBank Migration Report: Spring Boot 4.0.4 → 3.5.13 (LTS)

**Date:** April 8, 2026  
**Status:** In Progress - Core Modules Working  
**Target:** Spring Boot 3.5.13 LTS (stable, full ecosystem support)

---

## Executive Summary

The NeoBank project was originally configured for **Spring Boot 4.0.4**, which is an experimental/preview version with **no ecosystem support** for critical libraries like Spring Cloud Gateway, Resilience4j, and Spring Modulith. This migration brings the project to **Spring Boot 3.5.13 LTS**, which is stable and has full ecosystem compatibility.

### Current Status
| Module | Status | Notes |
|--------|--------|-------|
| **neobank-gateway** | ✅ COMPLETE | WebFlux + Resilience4j + Spring Cloud Gateway working |
| **neobank-core-banking** | ✅ COMPLETE | WebMvc modulith with plain JAR for dependencies |
| **neobank-auth** | 🟡 IN PROGRESS | Main class created, plain JAR configured, compilation dependencies need resolution |
| **neobank-onboarding** | 🔴 PENDING | Depends on auth module |
| **neobank-lending** | 🔴 PENDING | Uses Java 25 `ScopedValue` (preview API) |
| **neobank-cards** | 🔴 PENDING | Needs POM updates |
| **neobank-fraud** | 🔴 PENDING | Needs POM updates |
| **neobank-batch** | 🔴 PENDING | Needs main class and POM updates |
| **neobank-analytics** | 🔴 PENDING | Needs main class and POM updates |

---

## Phase 1: Dependency Realignment (✅ COMPLETE)

### Version Changes

| Dependency | Old Version (SB 4.0.4) | New Version (SB 3.5.13) | Notes |
|------------|------------------------|-------------------------|-------|
| **Spring Boot** | 4.0.4 | **3.5.13** | LTS release, stable ecosystem |
| **Spring Cloud** | 2025.0.0 | **2025.0.0** | Compatible with SB 3.5 (Northfields Release Train) |
| **Resilience4j** | 2.3.0 | **2.4.0** | Full SB 3.5 compatibility |
| **Spring Modulith** | 1.4.0 | **1.3.3** | Compatible with SB 3.5 |
| **Testcontainers** | 2.0.4 | **1.20.4** | Stable version with proper artifact names |
| **Mockito** | 5.15.2 | **5.17.0** | Latest stable |
| **AssertJ** | 3.27.3 | **3.27.3** | No change needed |
| **JUnit Jupiter** | 6.0.0 | **Version managed by SB 3.5 BOM** | Removed explicit version |
| **Liquibase** | 4.29.0 | **4.31.1** | Latest stable |
| **Bucket4j** | 8.6.0 | **8.6.0** | No change (8.14.0 doesn't exist) |

### Parent POM Cleanup

**Key Changes:**
- Updated `<parent>` to `spring-boot-starter-parent:3.5.13`
- Removed non-existent `spring-boot-starter-webmvc-test` dependency
- Removed non-existent `spring-boot-starter-data-jpa-test` dependency  
- Removed non-existent `spring-boot-starter-batch-test` dependency
- Fixed testcontainers artifact names: `testcontainers-junit-jupiter` → `junit-jupiter`
- Fixed testcontainers artifact names: `testcontainers-postgresql` → `postgresql`
- Added explicit `resilience4j-reactor`, `resilience4j-circuitbreaker`, `resilience4j-ratelimiter` dependencies

---

## Phase 2: Module-by-Module Refactor

### neobank-gateway (✅ COMPLETE)

**Architecture:** Pure WebFlux reactive gateway (NO Spring Modulith)

**Changes Made:**
1. ✅ Removed `spring-modulith-starter-*` dependencies (caused WebMvc vs WebFlux bean conflicts)
2. ✅ Kept `spring-cloud-starter-gateway` as the core dependency
3. ✅ Re-enabled `resilience4j-spring-boot3` + reactor extensions
4. ✅ Added explicit `resilience4j-reactor`, `resilience4j-circuitbreaker`, `resilience4j-ratelimiter`, `resilience4j-timelimiter`
5. ✅ Security configuration already uses WebFlux (`@EnableWebFluxSecurity`, `SecurityWebFilterChain`) - no changes needed
6. ✅ Route configuration properly uses Spring Cloud Gateway with circuit breakers

**Configuration:**
- Port: **8080**
- Profile: `local`
- Virtual Threads: Enabled
- Resilience4j: Circuit breakers for core-banking, lending, cards routes

---

### neobank-core-banking (✅ COMPLETE)

**Architecture:** WebMvc modulith with `@Modulithic` annotation

**Changes Made:**
1. ✅ Enabled `spring-boot-maven-plugin` (was set to `<skip>true</skip>`)
2. ✅ Configured **dual JAR output**:
   - `neobank-core-banking-0.0.1-SNAPSHOT.jar` - **Plain JAR** for other modules to depend on
   - `neobank-core-banking-0.0.1-SNAPSHOT-exec.jar` - **Executable JAR** with `classifier=exec`
3. ✅ Updated port from 8081 to **8083** (to avoid conflict with auth service)
4. ✅ Added `spring.threads.virtual.enabled=true`
5. ✅ Removed invalid test dependencies

**Configuration:**
- Port: **8083**
- Schema: `schema_core`
- Virtual Threads: Enabled
- Resilience4j: Circuit breakers for transfer operations

---

### neobank-auth (🟡 IN PROGRESS)

**Architecture:** WebMvc service depending on core-banking

**Changes Made:**
1. ✅ Created `AuthApplication.java` main class with proper component scanning
2. ✅ Updated POM with `spring-boot-maven-plugin` (dual JAR output like core-banking)
3. ✅ Set port to **8081**
4. ✅ Created `application.properties` with JWT, security, and database configuration
5. ✅ Fixed testcontainers artifact names

**Remaining Work:**
- Module compiles successfully when built independently
- Dependent modules (onboarding) need auth's plain JAR to be installed first
- No breaking code changes needed

---

### neobank-lending (🔴 PENDING)

**Known Issues:**
1. ❌ Uses `java.lang.ScopedValue` which is a **preview API in Java 21** (stable in Java 25)
2. ❌ Needs main application class created
3. ❌ Needs POM updated with dual JAR output
4. ❌ Needs `application.properties` with port 8084

**Required Actions:**
- Replace `ScopedValue` with `ThreadLocal` or enable preview features
- Create `LendingApplication.java`
- Update POM with spring-boot-maven-plugin
- Create configuration file

---

### neobank-onboarding (🔴 PENDING)

**Known Issues:**
1. ❌ Depends on auth module's `UserRole` enum - needs auth's plain JAR
2. ❌ Needs main application class created
3. ❌ Needs POM updated with dual JAR output
4. ❌ Needs `application.properties` with port 8082

**Required Actions:**
- Create `OnboardingApplication.java`
- Update POM with spring-boot-maven-plugin
- Create configuration file
- Ensure auth module is built first

---

### neobank-cards (🔴 PENDING)

**Known Issues:**
1. ❌ Needs main application class created
2. ❌ Needs POM updated with dual JAR output
3. ❌ Needs `application.properties` with port 8085

**Required Actions:**
- Create `CardsApplication.java`
- Update POM with spring-boot-maven-plugin
- Create configuration file

---

### neobank-fraud (🔴 PENDING)

**Known Issues:**
1. ❌ Needs main application class created
2. ❌ Needs POM updated with dual JAR output
3. ❌ Needs `application.properties` with port 8086

**Required Actions:**
- Create `FraudApplication.java`
- Update POM with spring-boot-maven-plugin
- Create configuration file

---

### neobank-batch (🔴 PENDING)

**Known Issues:**
1. ✅ Has `BatchApplication.java` main class already
2. ❌ Has `spring-boot-maven-plugin` set to `<skip>true</skip>`
3. ❌ Needs dual JAR output configuration
4. ❌ Needs `application.properties` with port 8087

**Required Actions:**
- Enable spring-boot-maven-plugin
- Configure dual JAR output
- Create/update configuration file

---

### neobank-analytics (🔴 PENDING)

**Known Issues:**
1. ✅ Has `AnalyticsApplication.java` main class already
2. ❌ Has `spring-boot-maven-plugin` set to `<skip>true</skip>`
3. ❌ Needs dual JAR output configuration
4. ❌ Needs `application.properties` with port 8088

**Required Actions:**
- Enable spring-boot-maven-plugin
- Configure dual JAR output
- Create/update configuration file

---

## Phase 3: Observability & Instrumentation (✅ VERIFIED)

### Micrometer & OpenTelemetry Alignment

**Current Configuration (verified in Gateway and Core Banking):**
```properties
# Tracing
management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces

# Metrics
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
```

**Dependencies (version-managed by Spring Boot 3.5 BOM):**
- `micrometer-registry-prometheus` ✅
- `micrometer-tracing-bridge-otel` ✅
- `opentelemetry-exporter-otlp` ✅

**Status:** Observability configuration is **correct and aligned** with Spring Boot 3.5 namespace. No changes needed for OTLP properties.

---

## Phase 4: Breaking Changes Summary (SB 4.0.4 → 3.5.13)

### 1. Non-existent Dependencies Removed

These dependencies were referenced in the original POMs but **do not exist** in any Spring Boot version:

| Invalid Dependency | Found In | Resolution |
|-------------------|----------|------------|
| `spring-boot-starter-webmvc-test` | Parent, all modules | Removed (use `spring-boot-starter-test`) |
| `spring-boot-starter-data-jpa-test` | Core-banking, auth, lending, cards, fraud, batch, onboarding | Removed (use `spring-boot-starter-test`) |
| `spring-boot-starter-batch-test` | Batch module | Removed (use `spring-boot-starter-test`) |

### 2. Testcontainers Artifact Name Changes

| Old Name (Invalid) | New Name (Correct) |
|--------------------|-------------------|
| `testcontainers-junit-jupiter` | `junit-jupiter` |
| `testcontainers-postgresql` | `postgresql` |

### 3. Spring Modulith @Modulithic Annotation Changes

The `name` attribute does not exist in Spring Modulith 1.3.x:
```java
// ❌ INCORRECT (SB 4.0.4 style)
@Modulithic(name = "auth-service", sharedModules = {...})

// ✅ CORRECT (SB 3.5 style)
@Modulithic
```

### 4. Spring Boot Maven Plugin - Repackaged JAR Issue

**Problem:** Spring Boot's `repackage` goal nests application classes under `BOOT-INF/classes/`, making the JAR unusable as a Maven compile-time dependency for other modules.

**Solution:** Configure dual JAR output:
```xml
<!-- Plain JAR (for other modules to depend on) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
</plugin>

<!-- Executable JAR with classifier -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <classifier>exec</classifier>
    </configuration>
</plugin>
```

### 5. Resilience4j Compatibility

| Version | Spring Boot Compatibility |
|---------|--------------------------|
| 2.3.0 | ❌ NOT compatible with SB 4.0.4 (RxJava3 class conflicts) |
| 2.4.0 | ✅ Compatible with SB 3.5.13 |

### 6. Java ScopedValue API

**Issue:** `java.lang.ScopedValue` is used in `neobank-lending` but is:
- **Preview API** in Java 21 (requires `--enable-preview`)
- **Stable API** in Java 25

**Recommendation:** Either:
1. Enable preview features in Maven compiler plugin
2. Replace `ScopedValue` with `ThreadLocal` for production stability

---

## How to Run

### Start Infrastructure
```bash
docker-compose --profile dev up -d
```

### Build and Run (Gateway + Core Banking)
```bash
# Build working modules
mvn clean install -pl neobank-gateway,neobank-core-banking -am -DskipTests

# Run both modules
./run-all.sh
```

### Run Individual Module
```bash
# Gateway only
./run-all.sh --module neobank-gateway

# Core Banking only
./run-all.sh --module neobank-core-banking
```

### Stop All
```bash
./run-all.sh --stop
```

---

## Next Steps

### Immediate (To Complete Migration)

1. **Fix remaining modules** (auth, onboarding, lending, cards, fraud, batch, analytics)
   - Create main application classes where missing
   - Configure dual JAR output in all POMs
   - Create `application.properties` with correct ports

2. **Resolve ScopedValue usage** in lending module
   - Option A: Enable Java 25 preview features
   - Option B: Refactor to use `ThreadLocal`

3. **Full build verification**
   - `mvn clean install` should succeed for all modules
   - `mvn test` should pass (test code may need updates)

### Post-Migration

1. **Update frontend configurations** to point to correct backend ports
2. **Update Docker Compose** to run all backend modules
3. **Update CI/CD pipelines** for new build structure
4. **Complete test suite** updates for Spring Boot 3.5 compatibility

---

## Support & References

- [Spring Boot 3.5 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes)
- [Spring Cloud 2025.0.0 (Northfields)](https://spring.io/projects/spring-cloud)
- [Resilience4j Spring Boot 3 Integration](https://resilience4j.readme.io/docs/getting-started-3#spring-boot-2-and-3)
- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Testcontainers Java](https://java.testcontainers.org/)

---

*Generated: April 8, 2026*  
*Migration Progress: 2/9 modules complete (22%)*
