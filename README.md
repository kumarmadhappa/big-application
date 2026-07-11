# Big Application

Distributed Spring Boot microservices starter.

## Services

- [`user-service`](user-service/README.md) - user CRUD, JWT auth, persistence, and observability
- [`api-gateway`](api-gateway/README.md) - JWT validation, routing, rate limiting, and observability
- [`banking-system`](banking-system/README.md) - banking admin/account holder flows and transactions

## Service documentation

- [`user-service/README.md`](user-service/README.md)
- [`api-gateway/README.md`](api-gateway/README.md)
- [`banking-system/README.md`](banking-system/README.md)

## Quick start

```bash
mvn clean install
```

Run each service from the repository root:

```bash
mvn -pl user-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

## Shared requirements

- Java 17+
- Maven 3.9+
- PostgreSQL for `user-service`
- PostgreSQL for `banking-system`
- `JWT_SECRET` for all services
