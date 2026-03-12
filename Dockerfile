# Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn .mvn
COPY pom.xml pom.xml

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
