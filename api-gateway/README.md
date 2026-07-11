# API Gateway

Spring Cloud Gateway entrypoint for JWT validation, routing, rate limiting, and observability.

## Overview

`api-gateway` provides:

- JWT validation before requests reach backend services
- route forwarding to `user-service`
- in-memory rate limiting
- actuator and metrics endpoints

## Configuration

`src/main/resources/application.yml` expects:

- `JWT_SECRET`
- `USER_SERVICE_URI` - optional, defaults to `http://localhost:8081`
- `BANKING_SERVICE_URI` - optional, defaults to `http://localhost:8082`

The gateway runs on `http://localhost:8080`.

## Routes

- `/api/auth/**` -> `user-service`
- `/api/users/**` -> `user-service`
- `/api/banking/**` -> `banking-system`

## Filters

- `JwtValidationGlobalFilter`
  - validates Bearer tokens
  - adds forwarded headers
  - skips `/api/auth/**` and actuator paths
- `InMemoryRateLimitingGlobalFilter`
  - limits requests per client IP
  - skips `/api/auth/**` and actuator paths

Rate limiting runs before JWT validation.

## Observability

Actuator endpoints:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/**`
- `GET /actuator/prometheus`

## Logging

Log files:

- `logs/api-gateway.log`
- `logs/api-gateway-error.log`

Set `LOG_PATH` to change the log directory.

## Build and run

From the repository root:

```bash
mvn clean install -pl api-gateway -am
mvn -pl api-gateway spring-boot:run
```

Or run the jar:

```bash
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

## Tests

```bash
mvn -pl api-gateway -am test
```

Key tests:

- `ApiGatewayApplicationTests`
- `ApiGatewayActuatorIntegrationTest`
- `JwtValidationGlobalFilterTest`
- `InMemoryRateLimitingGlobalFilterTest`
