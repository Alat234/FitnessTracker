# PROJECT_STATE.md — FitnessTracker (backend)

Current implementation state, based on actual files. Last updated: 2026-06-11 (branch `dev`, clean).

## Implemented (visible in code)

- **Auth** (`AuthController`, `AuthService`): register (`RegisterRequest` → `UserDTO`, role defaults to ROLE_USER), authentication (`AuthenticationRequest` → `UserDTO`), logout. JWT issued into httpOnly cookie (`JwtService`, `JwtAuthenticationFilter`); stateless security, CSRF off.
- **User** (`UserController`): `GET /api/user/me` returns current `UserDTO`.
- **Exercises** (`ExerciseController`, `ExerciseService`, `FileStorageService`): full CRUD, ADMIN-gated for writes, multipart image upload to `uploads/exercises/`, `BodyPart` enum.
- **Workouts** (`WorkOutController`, `WorkOutService`): save completed workout (`WorkOutDTO` with nested `ExerciseSetDTO`), history list, delete by id, ownership-checked.
- **Workout programs** (`WorkoutProgramController`, `WorkOutProgramService`): create/update/delete/list (`ProgramRequestDTO`), nested `WorkoutTemplate` → `SetTemplate` structure, ownership-checked.
- **Nutrition** (`NutritionController`, `NutritionService`): log CRUD by date (`NutritionLogDTO`), daily summary (`DailyNutritionSummaryDTO`), goals get/update (`NutritionGoalDTO`, defaults 2000 kcal / 150 P / 250 C / 70 F).
- **Trainer** (`TrainerController`, `TrainerService`): `GET /api/trainer/clients` (`ClientDTO`), `GET /api/trainer/clients/{id}/progress` (`ClientProgressDTO`) — read-only, gated to TRAINER/ADMIN/GYM_OWNER. Clients linked via `UserInfo.trainer`.
- **Error handling**: `BaseException` + `CustomExceptionController` (global handler), `ErrorResponse` model.
- **CORS**: `WebConfig` — origin `http://localhost:5173` only, credentials, `/api/**` and `/uploads/**`.

## Entities

`User` (UserDetails, role enum, authProvider), `UserInfo` (1:1 user; firstName, lastName, bio, phone, M:1 `trainer`→User), `Gym` (1:1 gymOwner; address/coords — **no controller**), `Exercise`, `WorkOut` + `ExerciseSet`, `WorkoutProgram` + `WorkoutTemplate` + `SetTemplate`, `NutritionLog` (indexed user_id+date), `NutritionGoal` (1:1 user).

## Not implemented (diploma scope gaps)

- **Body metrics tracking** — no entity, no controller.
- **Profile (UserInfo) read/update endpoints** — entity exists, no REST exposure.
- **Admin user management** — no endpoints. Frontend AdminUsersPage exists only as a placeholder; decide whether to implement minimal admin user management or hide the page for the demo.
- **Gym endpoints** — entity only.
- **Trainer client assignment (write side)** — only DB field, no endpoint.
- **Password change/reset, email verification** — none.

## Known inconsistencies

- **JWT expiration bug**: cookie maxAge is 365 days, but `JwtService` computes token expiration as `365 * 24 * 60 * 60` **milliseconds** (~8.75 hours). After ~8.75h the token is expired while the cookie persists → requests return 401 until re-login.
- `WorkOutService` / `WorkOutProgramService` throw raw `RuntimeException` (mixed UA/EN messages) instead of `BaseException`.
- No pagination on any list endpoint.
- Upload path `uploads/exercises/` hardcoded; secrets in `application.properties`.
- No OpenAPI/Swagger, no API versioning.
