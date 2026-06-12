# CLAUDE.md — FitnessTracker (backend)

Spring Boot backend of the **Fitness and Progress Tracking Web Platform** — the practical part of a diploma project. Diploma text is done; the goal now is a working, demonstrable app (frontend + backend together, screenshots).

Frontend repo: `C:\Users\Влад\WebstormProjects\fitnes_tracker_ui_v1` (React 19 + Vite, runs on `http://localhost:5173`). See `API_CONTRACT.md`, `PROJECT_STATE.md`, `TODO_BACKEND.md` in this root.

## Stack

- Spring Boot 3.5.7, Java 21, Gradle. MariaDB via `docker-compose.yml` (db `fitness-tracker`, port 3306). JPA/Hibernate with `ddl-auto=update`.
- Security: Spring Security + JJWT. JWT in **httpOnly cookie** (not Bearer header), stateless sessions, 365-day expiry. CORS allows only `http://localhost:5173` with credentials (`config/WebConfig.java`).
- Lombok, spring-boot-starter-validation, freemarker.

## Run

1. `docker compose up -d` (MariaDB)
2. `gradlew bootRun` → API on `http://localhost:8080`

Active development branch: **`dev`** (not `main`).

## Layout & conventions

- Base package `com.mycompany.fitnesstracker`: `Controllers/`, `Services/`, `Repositories/`, `Mappers/`, `Models/` (entities + DTOs + `Enums/`), `config/` (SecurityConfig, WebConfig, JwtService, JwtAuthenticationFilter).
- **DTO + Mapper pattern**: controllers never return entities; map via `Mappers/*Mapper`.
- **Errors**: throw `BaseException`, handled by `CustomExceptionController`. Note: WorkOut/WorkOutProgram services still use raw `RuntimeException` — new code should use `BaseException`.
- **File uploads**: `FileStorageService`, saves to `uploads/exercises/` (relative path), served under `/uploads/**`.
- Roles enum: `ROLE_USER` (default on register), `ROLE_TRAINER`, `ROLE_ADMIN`, `ROLE_GYM_OWNER`. Public paths: `/api/auth/**` only; everything else authenticated.
- Secrets (DB credentials, JWT key) live in `application.properties` — do not copy them into docs or commits; externalizing them is a TODO.

## Contract discipline

`API_CONTRACT.md` here and in the frontend repo describe the same contract. When adding/changing endpoints, update both files.
