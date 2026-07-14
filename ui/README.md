# UI

React + Express + Tailwind CSS frontend for user management.

## Overview

`ui` provides:

- login with JWT stored in HTTP-only cookies
- user list
- create/edit/delete user screens
- Express backend-for-frontend proxy to `user-service`

## Configuration

`server/index.ts` expects:

- `USER_SERVICE_URL` - optional, defaults to `http://localhost:8081`
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

- login and refresh go through Express
- user CRUD requests are proxied to `user-service`
- JWTs are kept in HTTP-only cookies

## OpenAPI specification

`ui/openapi.yaml`

## End-to-end flow

1. `src/main.tsx` loads `App` and wraps it with `BrowserRouter`.
2. `App.tsx` mounts `AuthProvider` and the shared `Layout`.
3. `AuthProvider` calls `GET /api/session` through Express to restore auth state.
4. `LoginPage.tsx` submits credentials through `auth.login()`.
5. `AuthContext` calls `POST /api/auth/login`.
6. `server/index.ts` forwards login to `user-service`.
7. `user-service` validates credentials and returns JWTs.
8. Express stores JWTs in HTTP-only cookies and returns the auth response.
9. `UsersPage.tsx` calls `/api/users` to load the list.
10. Express adds the access token from cookies and proxies to `user-service`.
11. Create, update, and delete actions go through the same Express proxy path.
12. Logout clears cookies in Express and resets the React auth state.
