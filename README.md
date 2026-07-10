# Big Application

Distributed Spring Boot microservices starter.

## Services

- [`user-service`](user-service/README.md) - user CRUD, JWT auth, persistence, and observability
- [`api-gateway`](api-gateway/README.md) - JWT validation, routing, rate limiting, and observability

## Service documentation

- [`user-service/README.md`](user-service/README.md)
- [`api-gateway/README.md`](api-gateway/README.md)

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
- `JWT_SECRET` for both services
