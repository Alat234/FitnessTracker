# TODO_BACKEND.md — FitnessTracker

Remaining work to make the practical part demonstrable. Ordered roughly by demo impact.

## Missing features (diploma scope)
- [ ] **Body metrics tracking**: entity (weight, measurements, date, user), repository, service, controller (`/api/metrics` CRUD + history for charts). Frontend page is blocked on this.
- [ ] **Profile endpoints**: expose `UserInfo` — `GET` profile (or extend `/api/user/me`) and `PUT` update (firstName, lastName, bio, phoneNumber). Frontend Profile page is blocked on this.
- [ ] **Admin user management**: frontend `AdminUsersPage` exists as a placeholder; backend user-management endpoints are not implemented. Decide whether to implement minimal admin user management (list users + role change) or hide the page for the demo.
- [ ] **Trainer client assignment (write side)**: endpoint to assign/unassign a client to a trainer (currently only the `UserInfo.trainer` DB field, set manually).
- [ ] **Progress analytics**: decide if `/api/trainer/clients/{id}/progress` logic should also be exposed for the user's own progress page, or if frontend computes from `/api/workout/history` + future metrics.

## Consistency / quality
- [ ] Replace raw `RuntimeException` in `WorkOutService` and `WorkOutProgramService` with `BaseException` (consistent with Exercise/Nutrition services); unify UA/EN error messages.
- [ ] Externalize secrets: DB credentials and JWT secret out of `application.properties` (env vars); make upload dir configurable.
- [ ] **Fix JWT expiration calculation**: `JwtService` uses `365 * 24 * 60 * 60` as milliseconds, so the token actually lives ~8.75 hours while the cookie maxAge is 365 days. Fix the calculation (multiply by 1000 or choose an intended shorter lifetime) or document the real lifetime before the final demo.
- [ ] Pagination for list endpoints (at least workout history) — optional for demo size.
- [ ] Gym entity: build endpoints or drop from demo scope.

## Verification
- [ ] `gradlew build` + tests pass.
- [ ] Manual smoke test with frontend on `localhost:5173`: register → login → workout save/history → nutrition log/summary/goals → trainer clients (needs a user with ROLE_TRAINER and assigned clients in DB) → admin exercise CRUD.
- [ ] Seed demo data (users per role, exercises with images, sample workouts/nutrition) for diploma screenshots.
- [ ] Update both `API_CONTRACT.md` files after any endpoint change.
