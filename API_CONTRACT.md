# API_CONTRACT.md — backend ↔ frontend

Source of truth: controllers in `src/main/java/com/mycompany/fitnesstracker/Controllers/`. A synced copy lives in the frontend repo (`C:\Users\Влад\WebstormProjects\fitnes_tracker_ui_v1\API_CONTRACT.md`) — update both when endpoints change. Do not invent endpoints; if unclear, mark "needs verification from backend controllers".

## Transport & auth rules

- API served on `http://localhost:8080`; frontend dev server on `http://localhost:5173`.
- Auth: JWT in **httpOnly cookie** (set by register/authentication, cleared by logout). Stateless (`SecurityConfig`), CSRF disabled. Frontend sends cookies via `withCredentials: true`.
- Token lifetime: cookie maxAge is 365 days, but `JwtService` computes token expiration as `365 * 24 * 60 * 60` **milliseconds**, so the actual JWT lifetime is ~8.75 hours (see `TODO_BACKEND.md`).
- CORS (`WebConfig`): origin `http://localhost:5173` only, credentials allowed, GET/POST/PUT/DELETE/OPTIONS, on `/api/**` and `/uploads/**`.
- Public: `/api/auth/**`. All other `/api/**` require authentication.
- Exercise images: stored by `FileStorageService`, served under `/uploads/exercises/...` (entity `imageUrl`).
- Roles: `ROLE_USER` (register default), `ROLE_TRAINER`, `ROLE_ADMIN`, `ROLE_GYM_OWNER`.
- Errors: `CustomExceptionController` returns `ErrorResponse` for `BaseException`.

## Endpoints

FE = called by frontend today (`src/api/` in UI repo).

### AuthController — `/api/auth`
| Method | Path | Request DTO | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/auth/register` | RegisterRequest `{email, name, password}` | UserDTO + JWT cookie | public | ✓ |
| POST | `/api/auth/authentication` | AuthenticationRequest `{email, password}` | UserDTO + JWT cookie | public | ✓ |
| POST | `/api/auth/logout` | — | cookie cleared | public | ✓ |

### UserController — `/api/user`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/user/me` | — | UserDTO | authenticated | ✓ |

### ExerciseController — `/api/exercise`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/exercise` | — | List\<ExerciseDTO\> | authenticated | ✓ |
| POST | `/api/exercise/create` | multipart: name, bodyPart, description?, file? | ExerciseDTO | ADMIN | ✓ |
| PUT | `/api/exercise` | multipart: id, name, bodyPart, description?, file? | ExerciseDTO | ADMIN | ✓ |
| DELETE | `/api/exercise/{id}` | path id | 204 | ADMIN | ✓ |

### WorkOutController — `/api/workout`
| Method | Path | Request DTO | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/workout/save` | WorkOutDTO (startTime, endTime, exerciseSets[ExerciseSetDTO]) | success string | authenticated | ✓ |
| GET | `/api/workout/history` | — | List\<WorkOutDTO\> | authenticated | ✓ |
| DELETE | `/api/workout/{id}` | path id | success string | authenticated | ✓ |

### WorkoutProgramController — `/api/workout/program`
| Method | Path | Request DTO | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/workout/program/create` | ProgramRequestDTO | ProgramRequestDTO | authenticated | ✓ |
| GET | `/api/workout/program/all` | — | List\<ProgramRequestDTO\> | authenticated | ✓ |
| PUT | `/api/workout/program/update` | ProgramRequestDTO (with id) | ProgramRequestDTO | authenticated | ✓ |
| DELETE | `/api/workout/program/delete/{id}` | path id | 200 | authenticated | ✓ |

### NutritionController — `/api/nutrition`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/nutrition/log` | query `date` (ISO.DATE, optional) | List\<NutritionLogDTO\> | authenticated | ✓ |
| POST | `/api/nutrition/log` | NutritionLogDTO | NutritionLogDTO (201) | authenticated | ✓ |
| DELETE | `/api/nutrition/log/{id}` | path id | 204 | authenticated | ✓ |
| GET | `/api/nutrition/summary` | query `date` (ISO.DATE, optional) | DailyNutritionSummaryDTO | authenticated | ✗ |
| GET | `/api/nutrition/goals` | — | NutritionGoalDTO | authenticated | ✗ |
| PUT | `/api/nutrition/goals` | NutritionGoalDTO | NutritionGoalDTO | authenticated | ✗ |

### TrainerController — `/api/trainer`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/trainer/clients` | — | List\<ClientDTO\> | TRAINER/ADMIN/GYM_OWNER | ✗ (frontend stub) |
| GET | `/api/trainer/clients/{id}/progress` | path id | ClientProgressDTO | TRAINER/ADMIN/GYM_OWNER | ✗ (frontend stub) |

Behavior (since 2026-06-12): clients are users with an **ACCEPTED TRAINER `UserConnection`** where viewer = current trainer (client sends the invite, trainer accepts). FRIEND connections never appear here. `/clients/{id}/progress`: 404 `"Client not found"` for unknown id; 403 `"This client is not assigned to you"` without an accepted TRAINER connection. Legacy `UserInfo.trainer` is deprecated and not consulted.

### ConnectionController — `/api/connections` (invite-based progress sharing)
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/connections/invites` | SendInviteRequest `{email, type: TRAINER\|FRIEND}` | ConnectionDTO (201) | authenticated (caller = owner) | ✗ not called yet |
| GET | `/api/connections/outgoing` | — | List\<ConnectionDTO\> (all statuses, caller = owner) | authenticated | ✗ |
| GET | `/api/connections/incoming` | — | List\<ConnectionDTO\> (PENDING + ACCEPTED, caller = viewer) | authenticated | ✗ |
| POST | `/api/connections/{id}/accept` | path id | ConnectionDTO | viewer only | ✗ |
| POST | `/api/connections/{id}/decline` | path id | ConnectionDTO | viewer only | ✗ |
| DELETE | `/api/connections/{id}` | path id | 204, status → REVOKED | owner only | ✗ |

Status codes / rules:
- Invite: 400 — blank email, null type, self-invite, TRAINER invite to a user without ROLE_TRAINER; 404 — unknown target email; 409 `"Invite already exists"` — existing PENDING/ACCEPTED row for the same (owner, viewer, type). A DECLINED/REVOKED row is reused: reset to PENDING, default permission flags re-applied, `respondedAt` cleared.
- Default permissions by type: TRAINER → all true; FRIEND → workouts + progressSummary only (nutrition and bodyMetrics false).
- Accept/decline: 404 unknown id; 403 `"Only the invited user can respond"`; 409 `"Invite already processed"` if not PENDING.
- Revoke: 404 unknown id; 403 `"Only the owner can revoke access"`; 409 `"Connection is not active"` unless PENDING/ACCEPTED. Row is kept (re-invite possible).

DTO shapes:
- `ConnectionDTO {id, type, status, owner: ConnectionUserDTO, viewer: ConnectionUserDTO, permissions: PermissionsDTO, createdAt, respondedAt}` (timestamps `yyyy-MM-dd HH:mm:ss`).
- `ConnectionUserDTO {id, email, firstName, lastName}`.
- `PermissionsDTO {workouts, nutrition, bodyMetrics, progressSummary}` (booleans).

### ShareController — `/api/share`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/share/{ownerId}/progress` | path ownerId | SharedProgressDTO | authenticated + ≥1 ACCEPTED connection (owner = ownerId, viewer = caller) | ✗ not called yet |

- 404 `"User not found"` — unknown ownerId; 403 `"No access to this user's progress"` — no ACCEPTED connection (PENDING/DECLINED/REVOKED don't count). Permission flags are OR-merged when both FRIEND and TRAINER rows exist for the pair.
- `SharedProgressDTO {owner: ConnectionUserDTO, permissions: PermissionsDTO, workoutHistory: List<WorkOutDTO>|null, totalWorkouts|null, totalVolumeKg|null, todayNutrition: DailyNutritionSummaryDTO|null, bodyMetrics: always null (reserved)}` — sections without permission are null; `permissions` always present and tells the frontend what to render.

## Not implemented (frontend must not call yet)

- Admin user management — not implemented. Frontend AdminUsersPage is only a placeholder; decide whether to implement minimal admin user management or hide the page for the demo (see `TODO_BACKEND.md`).
- Profile (`UserInfo`) read/update endpoints — planned.
- Body metrics endpoints — planned (`SharedProgressDTO.bodyMetrics` is a reserved null until then).
- Connection permission editing (`PATCH /api/connections/{id}/permissions`) — not implemented.
- Gym endpoints — entity only, no controller.
- Password change/reset — none.

> Note: the frontend copy of this file is NOT yet updated with `/api/connections` and `/api/share` — sync it during the frontend integration phase.
