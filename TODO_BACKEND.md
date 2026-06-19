# TODO_BACKEND.md — FitnessTracker

Remaining work to make the practical part demonstrable. Ordered roughly by demo impact.

## Done — Feature Phases 6A–6E (2026-06-18, branch `dev`)
- [x] **6A** — body metrics on `UserInfo` (height/weight/DOB/sex/activity/goal) + enums; `CalorieCalculatorService` (Mifflin-St Jeor); `GET /api/nutrition/calculator`; `PUT /api/user/me/body-metrics`. Apply reuses `PUT /api/nutrition/goals`.
- [x] **6B** — `PUT /api/user/me` (personal data), `POST /api/user/me/password` (BCrypt verify+re-encode, non-LOCAL/blank rejected, never returns password).
- [x] **6C** — `Gym` +imageUrl/isPublic; `UserInfo.gym` ManyToOne; `SelectedGymDTO` in `UserDTO`; new `DiscoverController`/`DiscoverService` (`/api/discover/gyms*`, select/leave); `GymRepository.findPublicGyms`. GymOwner CRUD extended with imageUrl/isPublic.
- [x] **6D** — `/api/discover/trainers*`; connect by trainer id via `ConnectionService.requestTrainerConnection`; whitelist Discover trainer DTOs (no email/phone); `UserRepository.findAllByRole`, `GymTrainerRepository.findFirstByTrainer`.
- [x] **6E** — `PUT /api/user/me/trainer-profile` (ROLE_TRAINER only) editing specialization+imageUrl; `UserInfoDTO` exposes them.
- [x] **Tests** (Mockito service units): CalorieCalculator 9, UserService 11, DiscoverService 10, ConnectionService 24 (+ existing GymService 26, TrainerService 8, ShareService 7) — all green via the **direct-`java` runner** (Gradle `test` worker broken on Cyrillic path; see `PROJECT_STATE.md`).
- [x] **6F** — Articles / Tips MVP: `Article` entity + `ArticleCategory` enum, `ArticleRepository`, `ArticleService` (published read + admin CRUD, service-layer `requireAdmin` 403), `GET /api/discover/articles[/{id}]`, new `AdminArticleController` `/api/admin/articles` CRUD, Discover/admin DTOs. `ArticleServiceTest` 10 green (direct-`java`). `imageUrl` strings only; no comments/likes/feed.

> Previously-listed gaps now CLOSED: profile read/update endpoints, password change, Gym endpoints, body metrics on UserInfo, nutrition goals/summary wired on FE. Still open below.

## Remaining (backend)
- [ ] **Body-metrics time series** — current metrics are single-value on `UserInfo`; a history entity (+charts) and wiring into `SharedProgressDTO.bodyMetrics` (still reserved null) remain.
- [x] Articles/Tips — done (Phase 6F). Remaining out of scope: comments/likes/feed, trainer reviews/results.
- [x] **7B** — Trainer-client scheduled workouts: `TrainingAppointment` + `AppointmentStatus`, `AppointmentRepository`, `AppointmentService` (ROLE_TRAINER-only writes, accepted-connection check, validation, soft-cancel), `AppointmentController` (`/api/appointments/my`, `/api/trainer/clients/{id}/appointments`, `/api/trainer/appointments/{id}`). `AppointmentServiceTest` 10 green. No recurring/notifications/WebSocket.
- [x] **Mini-fix** — exercise write ops truly ROLE_ADMIN-only via `ExerciseService.requireAdmin()` (service-layer; `@PreAuthorize` was inert without method security). `ExerciseServiceTest` 7 green.
- [ ] Image upload pipeline — out of scope (imageUrl strings only).
- [ ] Public/unauthenticated Discover — intentionally not implemented (locked: auth-only).
- [ ] Seed demo data: a `ROLE_TRAINER` with specialization/imageUrl, public gyms with images, for screenshots.

## Done (invite-based progress sharing, 2026-06-12)
- [x] **Phase 1 — UserConnection invite lifecycle**: `UserConnection` entity + enums, `/api/connections` endpoints (invite by email, outgoing/incoming lists, accept/decline/revoke), per-row permission flags, re-invite reuses DECLINED/REVOKED rows.
- [x] **Phase 2 — shared progress endpoint**: `GET /api/share/{ownerId}/progress` (`SharedProgressDTO`), gated by ACCEPTED connections, flags OR-merged, `bodyMetrics` reserved null.
- [x] **Phase 3 — trainer rewire**: `TrainerService` uses ACCEPTED TRAINER connections; `UserInfo.trainer` + `findAllClientsByTrainer` `@Deprecated`; endpoint paths and DTO shapes unchanged.
- [x] **Trainer client assignment (write side)** — solved by the invite lifecycle (client invites trainer, trainer accepts).
- [x] **Automated tests**: `ConnectionServiceTest` (19), `ShareServiceTest` (7), `TrainerServiceTest` (8) — Mockito unit tests; `gradlew test` 35/35.

## Missing features (diploma scope)
- [ ] **Body metrics tracking**: entity (weight, measurements, date, user), repository, service, controller (`/api/metrics` CRUD + history for charts). Frontend page is blocked on this. Also wire into `SharedProgressDTO.bodyMetrics` (currently reserved null).
- [ ] **Profile endpoints**: expose `UserInfo` — `GET` profile (or extend `/api/user/me`) and `PUT` update (firstName, lastName, bio, phoneNumber). Frontend Profile page is blocked on this.
- [ ] **Admin user management**: frontend `AdminUsersPage` exists as a placeholder; backend user-management endpoints are not implemented. Decide whether to implement minimal admin user management (list users + role change) or hide the page for the demo.
- [ ] **Connection permission editing** (optional polish): `PATCH /api/connections/{id}/permissions` — owner edits flags on an existing connection.
- [ ] **Progress analytics**: own-progress page — frontend computes from `/api/workout/history` (shared progress for viewers is covered by `/api/share/{ownerId}/progress`).

## Consistency / quality
- [ ] Replace raw `RuntimeException` in `WorkOutService` and `WorkOutProgramService` with `BaseException` (consistent with Exercise/Nutrition services); unify UA/EN error messages.
- [ ] Externalize secrets: DB credentials and JWT secret out of `application.properties` (env vars); make upload dir configurable.
- [ ] **Fix JWT expiration calculation**: `JwtService` uses `365 * 24 * 60 * 60` as milliseconds, so the token actually lives ~8.75 hours while the cookie maxAge is 365 days. Fix the calculation (multiply by 1000 or choose an intended shorter lifetime) or document the real lifetime before the final demo.
- [ ] Pagination for list endpoints (at least workout history) — optional for demo size.
- [ ] Gym entity: build endpoints or drop from demo scope.
- [ ] Legacy trainer link cleanup (post-diploma): one-time backfill SQL for old `UserInfo.trainer` rows (see PROJECT_STATE.md), then `ALTER TABLE user_info DROP COLUMN trainer_id` and delete deprecated `findAllClientsByTrainer`.

## Verification
- [x] `gradlew test` passes (35/35; connection/share/trainer service logic covered by unit tests).
- [ ] `gradlew build` clean.
- [ ] Manual smoke test with frontend on `localhost:5173`: register → login → workout save/history → nutrition log/summary/goals → invite flow (friend + trainer) → trainer clients via accepted TRAINER connection → shared progress view → admin exercise CRUD.
- [ ] Seed demo data (users per role, exercises with images, sample workouts/nutrition) for diploma screenshots.
- [ ] Update both `API_CONTRACT.md` files after any endpoint change. Frontend copy NOT yet updated with `/api/connections` + `/api/share` — do it with the frontend integration phase.
