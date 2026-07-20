# Banking System

Banking service with admin-managed account creation and account-holder transactions.

## Overview

`banking-system` provides:

- bank admin login
- admin account creation for person/company holders
- admin account search by holder name, account number, or account ID
- consumer/commercial account segments
- savings/credit account types
- holder login and balance operations (deposit/withdraw)
- JWT security, Flyway migrations, and observability endpoints

## Configuration

`src/main/resources/application.yml` expects:

- `JWT_SECRET`
- `BANKING_DB_PASSWORD` (or set full `BANKING_DB_URL` and `BANKING_DB_USERNAME`)

Optional admin bootstrap values:

- `BANK_ADMIN_USERNAME`
- `BANK_ADMIN_EMAIL`
- `BANK_ADMIN_PASSWORD`

Sample data seeding runs only when the active profile is `dev`, `local`, or `test`.

Default port: `8082`.

## API

Auth:

- `POST /api/banking/auth/login`
- `POST /api/banking/auth/refresh`

Admin:

- `POST /api/banking/admin/accounts` (admin only)
- `GET /api/banking/admin/accounts` (admin only)
- `GET /api/banking/admin/accounts/search?name={name}&accountNumber={accountNumber}&accountId={accountId}` (admin only)
- `PUT /api/banking/admin/accounts/{accountId}` (admin only)
- `DELETE /api/banking/admin/accounts/{accountId}` (admin only)
- `POST /api/banking/admin/accounts/{accountId}/transactions/deposit` (admin only)
- `POST /api/banking/admin/accounts/{accountId}/transactions/withdraw` (admin only)

The admin UI uses the search endpoint instead of loading every account when the page opens. Search parameters are optional, but at least one of `name`, `accountNumber`, or `accountId` should be supplied for useful results. Empty search criteria return an empty list.

Account holder:

- `GET /api/banking/accounts/mine`
- `POST /api/banking/accounts/{accountId}/transactions/deposit`
- `POST /api/banking/accounts/{accountId}/transactions/withdraw`

## OpenAPI specification

`banking-system/openapi.yaml`

## Account rules

- Savings account: cannot go below `0.00`
- Credit account: can go negative up to configured credit limit

## Observability

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/**`
- `GET /actuator/prometheus`

## Logging

Log files:

- `logs/banking-system.log`
- `logs/banking-system-error.log`

Set `LOG_PATH` to change the log directory.

## Build and run

```bash
mvn clean install -pl banking-system -am
mvn -pl banking-system spring-boot:run
```

## Tests

```bash
mvn -pl banking-system -am test
```

Key tests:

- `BankingSystemApplicationTests`
- `AdminControllerIntegrationTest`
- `TransactionControllerIntegrationTest`
- `ActuatorEndpointsIntegrationTest`
