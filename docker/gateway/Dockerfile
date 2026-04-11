# NeoBank Gateway - Multi-Stage Dockerfile
# Spring Cloud Gateway (Reactive) - Port 8080

# ── Build Stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy parent pom for dependency resolution
COPY pom.xml pom.xml

# Download all module dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B || true

# Copy all module source trees (required for multi-module build)
COPY neobank-gateway neobank-gateway
COPY neobank-auth neobank-auth
COPY neobank-onboarding neobank-onboarding
COPY neobank-core-banking neobank-core-banking
COPY neobank-lending neobank-lending
COPY neobank-cards neobank-cards
COPY neobank-fraud neobank-fraud
COPY neobank-batch neobank-batch
COPY neobank-analytics neobank-analytics

# Build only the gateway module (with its dependencies)
RUN mvn -pl neobank-gateway -am clean package -DskipTests -B

# ── Runtime Stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the executable JAR from builder
COPY --from=builder /app/neobank-gateway/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose gateway port
EXPOSE 8080

# Health check via actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseVirtualThreads"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
