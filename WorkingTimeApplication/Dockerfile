# File: `Dockerfile`
# Language: dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy pom first to leverage layer caching for dependencies
COPY pom.xml ./
# If you use a .mvn or mvnw, copy them too (optional)
COPY .mvn .mvn
COPY mvnw mvnw

# Download dependencies (offline) for caching
RUN mvn -B dependency:go-offline

# Copy sources and build. Use build-arg SKIP_TESTS (default true)
COPY src ./src
ARG SKIP_TESTS=true
RUN mvn -B -DskipTests=${SKIP_TESTS} package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
EXPOSE 8080

# Copy built jar from builder stage (wildcard allowed)
COPY --from=builder /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

