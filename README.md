# Bookstore API Lab

Teaching project for **REST APIs from a QA perspective**: JWT auth, role-based access (`ADMIN` vs read-only `USER`), paginated/filtered `GET /api/books`, stable JSON errors, `X-Request-Id`, OpenAPI/Swagger UI, and **REST Assured + TestNG** examples.

## Requirements

- **JDK 17+** (Spring Boot 3). Ensure Maven uses Java 17:

  ```text
  set JAVA_HOME=C:\Program Files\Java\jdk-17.0.7
  mvn -version
  ```

- **Maven 3.8+**

## Run locally (in-memory H2, no Supabase)

Default profile **`dev`** uses H2 with PostgreSQL compatibility mode (good for demos without cloud DB).

```text
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) (or whatever port you set) — log in as **`admin` / `password`** (full access) or **`user` / `password`** (browse only; no write buttons).

- Swagger UI: `http://localhost:<port>/swagger-ui.html`  
- OpenAPI JSON: `http://localhost:<port>/v3/api-docs`

### Port 8080 already in use

Use another port without editing files:

```text
set SERVER_PORT=8081
mvn spring-boot:run
```

Or stop whatever is holding **8080** (often a previous run of this app). In PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
taskkill /PID <OwningProcess> /F
```

## Run against Supabase (PostgreSQL)

1. Create a Supabase project and copy the **database password** and connection details.
2. Copy [`application-local.properties.example`](application-local.properties.example) to `application-local.properties` (gitignored) and fill in URL (`sslmode=require`), user, password, and a long random `app.jwt.secret` (32+ bytes).
3. Start with profile **`local`** and include your local properties, for example:

   ```text
   set SPRING_PROFILES_ACTIVE=local
   set SPRING_CONFIG_ADDITIONAL_LOCATION=file:./application-local.properties
   mvn spring-boot:run
   ```

   (Adjust paths for your OS; the important part is `local` profile + datasource + JWT secret.)

Flyway runs migrations `V1__schema.sql` and `V2__seed_books.sql` on startup; `UserInitializer` creates **`admin`** and **`user`** (password **`password`**) if the `users` table is empty.

## API quick reference

| Action | Request |
|--------|---------|
| Login | `POST /api/login` JSON `{"username":"admin","password":"password"}` → `accessToken` |
| List | `GET /api/books?page=0&size=20&sort=createdAt,desc&category=Fiction&q=harry` + header `Authorization: Bearer <token>` |
| Get one | `GET /api/books/{id}` |
| Create / replace / delete | `POST` / `PUT` / `DELETE` `/api/books` … — **`ADMIN` only** (403 for `USER`) |

**Semantics:** second `DELETE` on the same id returns **404**. Duplicate **ISBN** on create → **409**. Send **`X-Request-Id`** to have it echoed on responses and included in error JSON (`requestId`).

## Tests

```text
mvn test
```

Uses profile **`test`** (H2). Uses **TestNG** + **REST Assured** (`BookApiIT`).

Same checks run on **GitHub Actions** when you push or open a PR to `main` / `master` (workflow: [`.github/workflows/ci.yml`](.github/workflows/ci.yml)). For a classroom-friendly explanation of **CI/CD**, what Actions does here, and a **step-by-step demo** (branch → commit → PR → green checks → merge), see **[`docs/CI_CD_AND_GITHUB_ACTIONS.md`](docs/CI_CD_AND_GITHUB_ACTIONS.md)**. With the app running, **Course reference → CI/CD** summarizes the same ideas at `/reference/ci-cd`.

## Postman

Import OpenAPI from `http://localhost:8080/v3/api-docs` (while the app is running), then add a **Bearer Token** from `POST /api/login`.
