# Multi-stage build for common-api Spring Boot application

# Stage 1: Build the application
FROM maven:3.8-openjdk-8-slim AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM openjdk:8-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar common-api.jar

# Expose the application port
EXPOSE 8181

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8181/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "common-api.jar"]
