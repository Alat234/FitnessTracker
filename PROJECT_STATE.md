# PROJECT_STATE.md — FitnessTracker (backend)

Last updated: **2026-06-18**, branch `dev`. **Feature Phases 6A–6F + 7B complete and verified.**

**Phase 7B (Trainer-client scheduled workouts):** `TrainingAppointment` entity
(`training_appointments`) + `AppointmentStatus {SCHEDULED, COMPLETED, CANCELLED}`;
`AppointmentRepository` (`findAllByTrainerOrderByStartAtAsc`, `findAllByClientOrderByStartAtAsc`);
`AppointmentService` (create/listMy/update/cancel; ROLE_TRAINER-only writes via service-layer check —
ADMIN/GYM_OWNER do NOT bypass; accepted TRAINER connection owner=client/viewer=trainer; title+startAt
required, endAt-after-startAt validation; **DELETE = soft cancel** status→CANCELLED); `AppointmentController`
(`GET /api/appointments/my` role-aware, `POST /api/trainer/clients/{clientId}/appointments`,
`PUT|DELETE /api/trainer/appointments/{id}`). DTOs `AppointmentDTO`, `AppointmentPartyDTO`,
`Create/UpdateAppointmentRequest`. `AppointmentServiceTest` 10 green (direct-`java`). Mini-fix: exercise
write ops now ROLE_ADMIN-gated in `ExerciseService.requireAdmin()` (`ExerciseServiceTest` 7 green).

**Phase 6F (Articles / Tips MVP):** `Article` entity (`articles` table) + `ArticleCategory`
enum (`@Enumerated(STRING)`); `ArticleRepository` (`findAllByPublishedTrueOrderByCreatedAtDesc`,
`findByIdAndPublishedTrue`, `findAllByOrderByCreatedAtDesc`); `ArticleService` (published read +
admin CRUD, `requireAdmin()` service-layer 403 — no `@PreAuthorize`); read endpoints
`GET /api/discover/articles[/{id}]` on `DiscoverController`; new `AdminArticleController`
`/api/admin/articles` (GET/POST/PUT/DELETE). DTOs `DiscoverArticle{Card,Details}DTO`,
`ArticleAdminRequest`, `ArticleAdminDTO`. `ArticleServiceTest` 10 tests green (direct-`java` runner).
Note: existing `ExerciseController` `@PreAuthorize` is inert (no `@EnableMethodSecurity` in project) —
left as-is; Article admin gating uses the service-layer pattern instead.
(The "## Implemented / Entities / Not implemented" sections below predate Phase 6 — kept as
history; this top block is authoritative.)

## Phase 6 backend additions (branch `dev`)

**New enums** (`Models/Enums/`): `Sex {MALE, FEMALE}`; `ActivityLevel` (multipliers
SEDENTARY 1.2 / LIGHT 1.375 / MODERATE 1.55 / ACTIVE 1.725 / VERY_ACTIVE 1.9);
`FitnessGoal {LOSE 0.85, MAINTAIN 1.0, GAIN 1.10}`.

**`UserInfo` new fields** (all nullable, `ddl-auto=update` auto-adds columns): `heightCm`,
`weightKg`, `dateOfBirth`, `sex`, `activityLevel`, `fitnessGoal` (6A); `specialization`,
`imageUrl` (6D, public trainer fields); `gym` `@ManyToOne` → selected gym (6C, column
`selected_gym_id`, separate from `Gym.gymOwner`).

**`Gym` new fields**: `imageUrl` (String), `isPublic` (Boolean; `@PrePersist` defaults null→true).

**Services**: `CalorieCalculatorService` (Mifflin-St Jeor BMR/TDEE; maintain/lose −15%/gain +10%;
round to 10 kcal; lose floor F≥1200/M≥1500; returns `complete=false` + message when inputs
missing/out-of-range). `DiscoverService` (public gym list/details, select/leave gym, trainer
list/details, connect-by-id). `ConnectionService` gained `requestTrainerConnection(trainerId, ownerEmail)`
and a shared private `inviteViewer(owner, viewer, type)` (email `sendInvite` delegates to it;
duplicate messages now `"Request already sent"` / `"Already connected"`). `UserService` gained
`updateProfile`, `changePassword`, `updateBodyMetrics`, `updateTrainerProfile` + private
`currentUser()`/`trimToNull()` helpers (injected `PasswordEncoder`). `AuthService.generateFullResponse`
now builds its DTO via `userMapper.toDTO(user)` (single source; includes `selectedGym`).

**Controllers / endpoints** (see `API_CONTRACT.md`): `UserController` +`PUT /api/user/me`,
`POST /api/user/me/password`, `PUT /api/user/me/body-metrics`, `PUT /api/user/me/trainer-profile`;
`NutritionController` +`GET /api/nutrition/calculator`; **new `DiscoverController`** `/api/discover/**`
(gyms list/details/select/leave, trainers list/details/connect).

**DTOs**: requests `UpdateProfileRequest`, `ChangePasswordRequest`, `BodyMetricsRequest`,
`UpdateTrainerProfileRequest`; responses `CalorieEstimateDTO`, `SelectedGymDTO`,
`Discover{GymCard,GymDetails,TrainerCard,TrainerDetails,TrainerSummary}DTO`, `TrainerConnectResultDTO`.
`UserDTO` +`selectedGym`; `UserInfoDTO` +metrics +specialization +imageUrl (only constructor site is `UserMapper`).

**Repos**: `UserRepository.findAllByRole(Role)`; `GymRepository.findPublicGyms()` (JPQL, null isPublic = public);
`GymTrainerRepository.countByGym` + `findFirstByTrainer`.

**Security / access**: no `SecurityConfig` change — `/api/discover/**` covered by `anyRequest().authenticated()`.
Role checks in service layer (no `@PreAuthorize`): trainer-profile requires `ROLE_TRAINER` (403 else);
connect targets must be `ROLE_TRAINER`, no self, dedup. **Discover DTOs are hand-built whitelists**
(no owner/trainer email, phone, password, auth provider, or client/private data). Password change verifies
current via BCrypt, rejects non-LOCAL/blank-password accounts, never returns password.

## Gradle / Cyrillic-path test-worker workaround (important)
`gradlew test` **fails** on this machine: `compileJava`/`compileTestJava`/`testClasses` succeed, but
the JUnit **test worker** dies — `Could not find or load main class worker.org.gradle.process.internal.worker.GradleWorkerMain`
+ "pipe is being closed" — because the Cyrillic username path (`C:\Users\Влад\...`) corrupts the worker
argfile. Even with the daemon fix it cannot launch the test runner. Workarounds that DO run tests:
- **Compile**: `JAVA_HOME=C:\Program Files\Java\jdk-21`, `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\Temp`,
  `gradlew -p <repo> compileTestJava --no-daemon`.
- **Run tests** without Gradle's worker: a tiny `LauncherFactory`/`SummaryGeneratingListener` `main`
  placed outside the repo (e.g. `C:\Temp\RunTest.java`), classpath = `build/classes/java/main` +
  `build/classes/java/test` + all jars from `~/.gradle/caches/modules-2/files-2.1` (exclude `-sources`/`-javadoc`),
  run with JDK 21; Mockito self-attaches fine. Or run the test class in IntelliJ (right-click → Run).
- Verified 2026-06-18: service tests green via direct-`java` — CalorieCalculator 9, UserService 11,
  DiscoverService 10, ConnectionService 24, GymService 26, TrainerService 8, ShareService 7.

---

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
