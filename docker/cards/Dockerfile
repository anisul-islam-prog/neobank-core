# NeoBank Cards Service - Multi-Stage Dockerfile
# Card Management - Port 8085

# ── Build Stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml pom.xml
RUN mvn dependency:go-offline -B || true

COPY neobank-gateway neobank-gateway
COPY neobank-auth neobank-auth
COPY neobank-onboarding neobank-onboarding
COPY neobank-core-banking neobank-core-banking
COPY neobank-lending neobank-lending
COPY neobank-cards neobank-cards
COPY neobank-fraud neobank-fraud
COPY neobank-batch neobank-batch
COPY neobank-analytics neobank-analytics

RUN mvn -pl neobank-cards -am clean package -DskipTests -B

# ── Runtime Stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/neobank-cards/target/*-exec.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseVirtualThreads"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
