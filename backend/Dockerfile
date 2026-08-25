# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies first (changes less often)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build a fat jar
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar

# Non-root user
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
