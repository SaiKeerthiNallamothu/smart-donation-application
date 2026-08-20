# Smart Donation and Resource Management System — Backend

REST API for the Smart Donation platform: connecting donors, verified NGOs,
orphanages, shelters, volunteers, corporate CSR organizations and administrators
to reduce wastage of food and other useful resources.

## Tech stack

| Component   | Choice                                        |
|-------------|-----------------------------------------------|
| Language    | Java 21 (LTS)                                 |
| Framework   | Spring Boot 3.5                               |
| Build       | Maven                                         |
| Database    | MySQL 8.4 (Flyway-managed schema)             |
| Security    | Spring Security + JWT (stateless)             |
| API docs    | springdoc-openapi (Swagger UI)                |

## Project layout

```
src/main/java/com/smartdonation/
├── config/      # App properties, OpenAPI, JPA auditing
├── common/      # API response contract, exceptions, pagination
├── security/    # JWT service, auth filter, security config
└── <feature>/   # upcoming feature modules (user, donor, ngo, donation, ...)
src/main/resources/db/migration/   # Flyway SQL migrations
```

## Quickstart

```bash
# 1. Start MySQL (first time only)
docker compose up -d mysql

# 2. Run the application (defaults: localhost:3306, smart_donation/smart_donation)
mvn spring-boot:run
```

The database schema is created automatically by Flyway on startup.

- API docs (Swagger UI): http://localhost:8080/swagger-ui.html
- Health check:        http://localhost:8080/actuator/health

## Tests

```bash
mvn test
```

Tests run against an in-memory H2 database (MySQL compatibility mode) with the
`test` profile — no external services required.

## Configuration

All settings are overridable via environment variables:

| Variable               | Default                                   |
|------------------------|-------------------------------------------|
| `DB_HOST` / `DB_PORT`  | `localhost` / `3306`                      |
| `DB_NAME`              | `smart_donation`                          |
| `DB_USERNAME`          | `smart_donation`                          |
| `DB_PASSWORD`          | `smart_donation`                          |
| `SERVER_PORT`          | `8080`                                    |
| `JWT_SECRET`           | dev-only secret (change in production!)   |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173`                   |
