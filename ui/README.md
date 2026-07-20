# UI

React + Express + Tailwind CSS frontend for user management and banking workflows.

## Overview

`ui` provides:

- a landing page with service selection
- login with JWT stored in HTTP-only cookies
- user list
- banking admin and holder dashboards
- banking admin account search by name, account number, or account ID
- create/edit/delete user screens
- Express backend-for-frontend proxy to the API gateway

## Configuration

`server/index.ts` expects:

- `API_GATEWAY_URL` - optional, defaults to `http://localhost:8080`
- `PORT` - optional, defaults to `4000`
- `ACCESS_COOKIE_NAME` - optional
- `REFRESH_COOKIE_NAME` - optional

## Run

From the repository root:

```bash
cd ui
npm install
npm run dev
```

The Vite client runs on `http://localhost:5173` and proxies API calls to the Express server on `http://localhost:4000`.

## Build

```bash
npm run build
npm run start
```

## Backend integration

- login and refresh go through Express and then the API gateway
- user CRUD requests are proxied to the API gateway
- banking login, account search, account management, and transaction requests are proxied to the API gateway
- JWTs are kept in HTTP-only cookies

## OpenAPI specification

`ui/openapi.yaml`

## End-to-end flow

1. `src/main.tsx` loads `App` and wraps it with `BrowserRouter`.
2. `App.tsx` mounts `AuthProvider` and the shared `Layout`.
3. `HomePage.tsx` lets the user choose User Service or Banking System.
4. `AuthProvider` calls `GET /api/session` through Express to restore auth state.
5. `LoginPage.tsx` submits user-service credentials through `auth.login()`.
6. `BankingLoginPage.tsx` submits banking credentials through `auth.bankingLogin()`.
7. `server/index.ts` forwards the login to the API gateway.
8. The gateway routes the request to the selected backend service.
9. The backend validates credentials and returns JWTs.
10. Express stores JWTs in HTTP-only cookies and returns the auth response.
11. `UsersPage.tsx` calls `/api/users` to load the list.
12. `BankingLandingPage.tsx` redirects admins to `/banking/admin` and holders to `/banking/holder`.
13. `BankingAdminPage.tsx` requires admins to search by holder name, account number, or account ID before account rows are loaded.
14. `BankingHolderPage.tsx` manages only the signed-in holder's accounts.
15. Express adds the access token from cookies and proxies to the gateway.
16. The gateway forwards the call to `user-service` or `banking-system`.
17. Logout clears cookies in Express and returns to the Home page.
