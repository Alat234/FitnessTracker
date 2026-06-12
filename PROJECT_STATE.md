# PROJECT_STATE.md — FitnessTracker (backend)

Current implementation state, based on actual files. Last updated: 2026-06-12, branch `dev`, backend phases 1–3 completed.

## Implemented (visible in code)

- **Auth** (`AuthController`, `AuthService`): register (`RegisterRequest` → `UserDTO`, role defaults to ROLE_USER), authentication (`AuthenticationRequest` → `UserDTO`), logout. JWT issued into httpOnly cookie (`JwtService`, `JwtAuthenticationFilter`); stateless security, CSRF off.
- **User** (`UserController`): `GET /api/user/me` returns current `UserDTO`.
- **Exercises** (`ExerciseController`, `ExerciseService`, `FileStorageService`): full CRUD, ADMIN-gated for writes, multipart image upload to `uploads/exercises/`, `BodyPart` enum.
- **Workouts** (`WorkOutController`, `WorkOutService`): save completed workout (`WorkOutDTO` with nested `ExerciseSetDTO`), history list, delete by id, ownership-checked.
- **Workout programs** (`WorkoutProgramController`, `WorkOutProgramService`): create/update/delete/list (`ProgramRequestDTO`), nested `WorkoutTemplate` → `SetTemplate` structure, ownership-checked.
- **Nutrition** (`NutritionController`, `NutritionService`): log CRUD by date (`NutritionLogDTO`), daily summary (`DailyNutritionSummaryDTO`), goals get/update (`NutritionGoalDTO`, defaults 2000 kcal / 150 P / 250 C / 70 F).
- **Trainer** (`TrainerController`, `TrainerService`): `GET /api/trainer/clients` (`ClientDTO`), `GET /api/trainer/clients/{id}/progress` (`ClientProgressDTO`) — read-only, gated to TRAINER/ADMIN/GYM_OWNER. Clients now come from **ACCEPTED TRAINER `UserConnection` rows** (viewer = trainer); progress access checked the same way (403 without an accepted TRAINER connection). `UserInfo.trainer` and `UserRepository.findAllClientsByTrainer` are `@Deprecated` legacy — not read by new logic; the `trainer_id` column remains in DB (one-time backfill SQL needed if pre-existing rows must appear in the panel).
- **Progress sharing / connections** (`ConnectionController`, `ConnectionService`, `ShareController`, `ShareService`, `ConnectionMapper`): invite-based sharing. Owner invites a viewer by email (`POST /api/connections/invites`, type `TRAINER` or `FRIEND`; TRAINER invites only to users with ROLE_TRAINER). Lifecycle: PENDING → ACCEPTED/DECLINED (viewer-only) / REVOKED (owner-only); re-invite after DECLINED/REVOKED reuses the row (reset to PENDING, default flags re-applied). Per-row permission flags: workouts, nutrition, bodyMetrics, progressSummary (TRAINER defaults: all true; FRIEND defaults: workouts + progressSummary only). Lists: `GET /api/connections/outgoing` (owner side, all statuses), `GET /api/connections/incoming` (viewer side, PENDING+ACCEPTED). Viewer reads shared data via `GET /api/share/{ownerId}/progress` (`SharedProgressDTO`) — sections gated by OR-merged flags across the pair's ACCEPTED rows; non-permitted sections null; `bodyMetrics` always null (reserved until body metrics exist).
- **Error handling**: `BaseException` + `CustomExceptionController` (global handler), `ErrorResponse` model.
- **CORS**: `WebConfig` — origin `http://localhost:5173` only, credentials, `/api/**` and `/uploads/**`.

## Entities

`User` (UserDetails, role enum, authProvider), `UserInfo` (1:1 user; firstName, lastName, bio, phone, M:1 `trainer`→User — **legacy/deprecated**), `Gym` (1:1 gymOwner; address/coords — **no controller**), `Exercise`, `WorkOut` + `ExerciseSet`, `WorkoutProgram` + `WorkoutTemplate` + `SetTemplate`, `NutritionLog` (indexed user_id+date), `NutritionGoal` (1:1 user), `UserConnection` (M:1 `owner`→User, M:1 `viewer`→User, enums `ConnectionType` TRAINER|FRIEND and `ConnectionStatus` PENDING|ACCEPTED|DECLINED|REVOKED, 4 permission booleans, createdAt/respondedAt, unique (owner_id, viewer_id, connection_type)).

## Tests

- `ConnectionServiceTest` (19), `ShareServiceTest` (7), `TrainerServiceTest` (8) — Mockito service unit tests, no DB needed.
- Pre-existing `FitnessTrackerApplicationTests` (contextLoads) boots the full context — requires running MariaDB.
- `gradlew test`: 35/35 passing.

## Not implemented (diploma scope gaps)

- **Body metrics tracking** — no entity, no controller; also needs wiring into `SharedProgressDTO.bodyMetrics` (currently reserved null).
- **Profile (UserInfo) read/update endpoints** — entity exists, no REST exposure.
- **Admin user management** — no endpoints. Frontend AdminUsersPage exists only as a placeholder; decide whether to implement minimal admin user management or hide the page for the demo.
- **Gym endpoints** — entity only.
- **Connection permission editing** — flags stored per row, but no `PATCH /api/connections/{id}/permissions` endpoint (optional polish).
- **Password change/reset, email verification** — none.

## Known inconsistencies

- **JWT expiration bug**: cookie maxAge is 365 days, but `JwtService` computes token expiration as `365 * 24 * 60 * 60` **milliseconds** (~8.75 hours). After ~8.75h the token is expired while the cookie persists → requests return 401 until re-login.
- `WorkOutService` / `WorkOutProgramService` throw raw `RuntimeException` (mixed UA/EN messages) instead of `BaseException`.
- Legacy `trainer_id` column still exists on `user_info` (ddl-auto can't drop it). Pre-existing trainer-client rows are invisible to the new connection-based panel until a one-time backfill: `INSERT INTO user_connection (owner_id, viewer_id, connection_type, status, can_view_workouts, can_view_nutrition, can_view_body_metrics, can_view_progress_summary, created_at) SELECT ui.user_id, ui.trainer_id, 'TRAINER', 'ACCEPTED', 1, 1, 1, 1, NOW() FROM user_info ui WHERE ui.trainer_id IS NOT NULL;`
- No pagination on any list endpoint.
- Upload path `uploads/exercises/` hardcoded; secrets in `application.properties`.
- No OpenAPI/Swagger, no API versioning.
