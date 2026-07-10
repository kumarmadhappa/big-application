# Big Application

Starter repository for distributed Spring Boot microservices.

## Parent project

- `pom.xml` — Maven aggregator for all microservices

## Current service

- `user-service/` — CRUD for users with JWT authentication, PostgreSQL, JPA, and Flyway.

## User Service Overview

`user-service` provides:

- Authentication endpoints (`/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`)
- Protected user CRUD endpoints (`/api/users/**`)
- JWT-based stateless security
- Schema migration with Flyway
- File and console logging via Logback

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL database

## Configuration

`user-service/src/main/resources/application.yml` expects these environment variables:

- `POSTGRES_PASSWORD`
- `JWT_SECRET`

Optional:

- `LOG_PATH` (defaults to `logs`)

## Build and Run

From repository root:

```bash
mvn clean install -pl user-service -am
mvn -pl user-service spring-boot:run
```

Service runs on: `http://localhost:8081`

## API Usage

1. Register user (public):
   - `POST /api/auth/register`
2. Login (public):
   - `POST /api/auth/login`
3. Use access token for protected endpoints:
   - Header: `Authorization: Bearer <access_token>`
4. Refresh token:
   - `POST /api/auth/refresh`

## Database Migrations

Flyway migration scripts are in:

- `user-service/src/main/resources/db/migration`

Initial schema is created by:

- `V1__create_users_table.sql`

## Logging

Logback config:

- `user-service/src/main/resources/logback-spring.xml`

Generated files (by default):

- `logs/user-service.log`
- `logs/user-service-error.log`
- archived rolling logs under `logs/archive/`

## Tests

Run tests:

```bash
mvn -pl user-service -am test
```

Important test classes:

- `UserServiceApplicationTests` (context smoke test)
- `AuthControllerIntegrationTest` (register/login flows)
- `UserControllerIntegrationTest` (JWT protection and authorized access)