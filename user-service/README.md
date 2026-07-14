# User Service

User CRUD service with JWT authentication, PostgreSQL, Flyway, and observability.

## Overview

`user-service` provides:

- registration, login, and refresh-token endpoints
- protected user CRUD endpoints
- stateless JWT security
- database migrations with Flyway
- logs, metrics, health checks, and Prometheus export

## Configuration

`src/main/resources/application.yml` expects:

- `POSTGRES_PASSWORD`
- `JWT_SECRET`

Optional:

- `LOG_PATH` - defaults to `logs`

## Build and run

From the repository root:

```bash
mvn clean install -pl user-service -am
mvn -pl user-service spring-boot:run
```

The service runs on `http://localhost:8081`.

## API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/users/**`
  - `/api/users/**` requires a Bearer token.

## OpenAPI specification

`user-service/openapi.yaml`

## Database

Flyway migrations live in:

- `src/main/resources/db/migration`

## Observability

Actuator endpoints:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/**`
- `GET /actuator/prometheus`

`/actuator/health` is public. Health details are shown only when authorized.

## Logging

Log files:

- `logs/user-service.log`
- `logs/user-service-error.log`

## Tests

```bash
mvn -pl user-service -am test
```

Key tests:

- `UserServiceApplicationTests`
- `AuthControllerIntegrationTest`
- `UserControllerIntegrationTest`
- `ActuatorEndpointsIntegrationTest`
