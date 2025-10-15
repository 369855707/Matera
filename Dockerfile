# ========================================
# Stage 1: Build Stage
# ========================================
# Using eclipse-temurin with Maven installed
FROM eclipse-temurin:21 AS builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /build

COPY settings.xml /root/.m2/settings.xml

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies offline (will use mounted cache)
RUN mvn dependency:go-offline -B -s /root/.m2/settings.xml

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests -B -s /root/.m2/settings.xml

# ========================================
# Stage 2: Runtime Stage
# ========================================
# Using openjdk variant for better China registry compatibility
FROM eclipse-temurin:21-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Create directories
RUN mkdir -p /data/h2 /data/logs && chown -R spring:spring /data /app

# Switch to non-root user
USER spring:spring

# JVM memory config
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]