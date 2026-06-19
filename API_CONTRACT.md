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
| PUT | `/api/user/me` | UpdateProfileRequest `{firstName, lastName, phoneNumber, bio}` | UserDTO | authenticated | ✓ |
| POST | `/api/user/me/password` | ChangePasswordRequest `{currentPassword, newPassword}` | 204 | authenticated | ✓ |
| PUT | `/api/user/me/body-metrics` | BodyMetricsRequest `{heightCm, weightKg, dateOfBirth, sex, activityLevel, fitnessGoal}` | UserDTO | authenticated | ✓ |
| PUT | `/api/user/me/trainer-profile` | UpdateTrainerProfileRequest `{specialization, imageUrl}` | UserDTO | authenticated + **ROLE_TRAINER** | ✓ |

- `UserDTO {email, firstName, role, userInfoDTO, selectedGym}` — never includes password.
- `userInfoDTO {firstName, lastName, BIO, phoneNumber, heightCm, weightKg, dateOfBirth, sex, activityLevel, fitnessGoal, specialization, imageUrl}`; `selectedGym {id, name, city, imageUrl}` (null if none).
- `PUT /me` = personal data only (never email/role/id/password/metrics). `POST /me/password`: BCrypt `matches` on current, rejects non-LOCAL / blank-password accounts (400 `"Password change is only available for password-based accounts."`), wrong current → 400, success 204. `PUT /me/trainer-profile`: sets specialization+imageUrl only, 403 for non-trainers.
- Enums: `sex` MALE|FEMALE; `activityLevel` SEDENTARY|LIGHT|MODERATE|ACTIVE|VERY_ACTIVE; `fitnessGoal` LOSE|MAINTAIN|GAIN; `dateOfBirth` `yyyy-MM-dd`.

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
| GET | `/api/nutrition/summary` | query `date` (ISO.DATE, optional) | DailyNutritionSummaryDTO | authenticated | ✗ (FE sums logs client-side) |
| GET | `/api/nutrition/goals` | — | NutritionGoalDTO | authenticated | ✓ |
| PUT | `/api/nutrition/goals` | NutritionGoalDTO | NutritionGoalDTO | authenticated | ✓ |
| GET | `/api/nutrition/calculator` | — | CalorieEstimateDTO | authenticated | ✓ |

- `CalorieEstimateDTO {bmr, tdee, maintainCalories, loseCalories, gainCalories, selectedGoalCalories, complete, message}` — Mifflin-St Jeor from `UserInfo` metrics; `complete=false` (calorie fields null) + message when height/weight/DOB/sex missing or out of range. `updateGoalFromDTO` only overwrites non-null fields → FE "Apply" sends `{calorieGoal}` and leaves macros intact.

### TrainerController — `/api/trainer`
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/trainer/clients` | — | List\<ClientDTO\> | TRAINER/ADMIN/GYM_OWNER | ✓ `trainerApi.js` → TrainerPage |
| GET | `/api/trainer/clients/{id}/progress` | path id | ClientProgressDTO | TRAINER/ADMIN/GYM_OWNER | ✓ `trainerApi.js` → TrainerPage |

Behavior (since 2026-06-12): clients are users with an **ACCEPTED TRAINER `UserConnection`** where viewer = current trainer (client sends the invite, trainer accepts). FRIEND connections never appear here. `/clients/{id}/progress`: 404 `"Client not found"` for unknown id; 403 `"This client is not assigned to you"` without an accepted TRAINER connection. Legacy `UserInfo.trainer` is deprecated and not consulted.

### ConnectionController — `/api/connections` (invite-based progress sharing)
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/connections/invites` | SendInviteRequest `{email, type: TRAINER\|FRIEND}` | ConnectionDTO (201) | authenticated (caller = owner) | ✓ `connectionsApi.js` |
| GET | `/api/connections/outgoing` | — | List\<ConnectionDTO\> (all statuses, caller = owner) | authenticated | ✓ |
| GET | `/api/connections/incoming` | — | List\<ConnectionDTO\> (PENDING + ACCEPTED, caller = viewer) | authenticated | ✓ |
| POST | `/api/connections/{id}/accept` | path id | ConnectionDTO | viewer only | ✓ |
| POST | `/api/connections/{id}/decline` | path id | ConnectionDTO | viewer only | ✓ |
| DELETE | `/api/connections/{id}` | path id | 204, status → REVOKED | owner only | ✓ |

- Phase 6D: duplicate-invite messages are now `"Request already sent"` (PENDING) / `"Already connected"` (ACCEPTED). `ConnectionService.requestTrainerConnection(trainerId, ownerEmail)` (used by Discover connect) shares the same invite core but resolves the viewer by **trainer id** — no email needed.

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

### GymController — `/api/gyms` (gym-owner management; permissions in `GymService`)
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| POST | `/api/gyms` | GymRequest | GymDTO (201) | GYM_OWNER/ADMIN | ✓ GymOwnerPage |
| GET | `/api/gyms/my` | — | List\<GymDTO\> (0 or 1, MVP) | authenticated | ✓ |
| GET | `/api/gyms/{id}` | path id | GymDTO (with trainers) | authenticated | ✓ GymPage |
| PUT | `/api/gyms/{id}` | GymRequest | GymDTO | owner/ADMIN | ✓ |
| POST | `/api/gyms/{id}/trainers` | AddTrainerRequest `{email}` | GymTrainerDTO (201) | owner/ADMIN | ✓ |
| DELETE | `/api/gyms/{id}/trainers/{trainerId}` | path = **trainer User id** | 204 | owner/ADMIN | ✓ |
| GET | `/api/gyms/{id}/trainers` | — | List\<GymTrainerDTO\> | authenticated | (UI reads `GymDTO.trainers`) |

- One gym per owner (MVP, `gymOwner` OneToOne; 409 on duplicate). `GymRequest`/`GymDTO` include `imageUrl` + `isPublic` (6C). Add-trainer target must have ROLE_TRAINER.

### DiscoverController — `/api/discover` (Phase 6C–6D, authenticated-only)
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/discover/gyms` | — | List\<DiscoverGymCardDTO\> (public only) | authenticated | ✓ `discoverApi.js` |
| GET | `/api/discover/gyms/{id}` | path id | DiscoverGymDetailsDTO (404 if missing/private) | authenticated | ✓ |
| POST | `/api/discover/gyms/{id}/select` | path id | UserDTO (selectedGym set) | authenticated | ✓ |
| DELETE | `/api/discover/gyms/selection` | — | UserDTO (selectedGym null) | authenticated | ✓ |
| GET | `/api/discover/trainers` | — | List\<DiscoverTrainerCardDTO\> (ROLE_TRAINER users) | authenticated | ✓ |
| GET | `/api/discover/trainers/{id}` | path id | DiscoverTrainerDetailsDTO (404 if not trainer) | authenticated | ✓ |
| POST | `/api/discover/trainers/{id}/connect` | path id | TrainerConnectResultDTO `{status, message}` | authenticated | ✓ |
| GET | `/api/discover/articles` | — | List\<DiscoverArticleCardDTO\> (published only, newest first) | authenticated | ✓ `discoverApi.getArticles` |
| GET | `/api/discover/articles/{id}` | path id | DiscoverArticleDetailsDTO (404 if missing/unpublished) | authenticated | ✓ `discoverApi.getArticle` |

- **Whitelist DTOs — no owner/trainer email, phone, password, auth provider, or client/private data.**
  `DiscoverGymCardDTO {id, name, city, address, imageUrl, trainerCount}`;
  `DiscoverGymDetailsDTO {id, name, description, city, address, phoneNumber, email` (gym contact)`, imageUrl, trainerCount, trainers:[{firstName,lastName}]}`;
  `DiscoverTrainerCardDTO {id, firstName, lastName, specialization, imageUrl, gymName}`;
  `DiscoverTrainerDetailsDTO {id, firstName, lastName, bio, specialization, imageUrl, gymName}`.
- Connect: creates a TRAINER `UserConnection` (owner = caller, viewer = trainer-by-id). 400 self/non-trainer, 404 missing, 409 `"Request already sent"`/`"Already connected"`. Private gym (`isPublic=false`) hidden as 404; null isPublic = public.

### Articles / Tips — `DiscoverController` (read) + `AdminArticleController` (admin), Phase 6F
| Method | Path | Request | Response | Access | FE |
|---|---|---|---|---|---|
| GET | `/api/admin/articles` | — | List\<ArticleAdminDTO\> (all, newest first) | authenticated + **ROLE_ADMIN** (403 else) | ✓ `articleApi.getAdminArticles` |
| POST | `/api/admin/articles` | ArticleAdminRequest | ArticleAdminDTO (201) | ROLE_ADMIN | ✓ `articleApi.createArticle` |
| PUT | `/api/admin/articles/{id}` | ArticleAdminRequest | ArticleAdminDTO (404 if missing) | ROLE_ADMIN | ✓ `articleApi.updateArticle` |
| DELETE | `/api/admin/articles/{id}` | — | 204 (404 if missing) | ROLE_ADMIN | ✓ `articleApi.deleteArticle` |

- Read DTOs (whitelist, safe fields only):
  `DiscoverArticleCardDTO {id, title, summary, category, imageUrl, createdAt}`;
  `DiscoverArticleDetailsDTO {id, title, summary, content, category, imageUrl, createdAt, updatedAt}`.
- Admin DTO/request: `ArticleAdminDTO {id, title, summary, content, category, imageUrl, published, createdAt, updatedAt}`;
  `ArticleAdminRequest {title, summary, content, category, imageUrl, published}`.
- `category` enum `ArticleCategory {TRAINING, NUTRITION, RECOVERY, PLATFORM, GENERAL}` (`@Enumerated(STRING)`; null → GENERAL on persist). `published` null → false on persist; only published articles appear in Discover.
- **ROLE_ADMIN enforced in `ArticleService` (service-layer check, 403), NOT `@PreAuthorize`** — this project does not enable method security (mirrors GymService/DiscoverService). Image is `imageUrl` string only (no upload). Routing by article id (no slug).

### Appointments — `AppointmentController` (Phase 7B, scheduled workouts)
| Method | Path | Request | Response | Access |
|---|---|---|---|---|
| GET | `/api/appointments/my` | — | List\<AppointmentDTO\> | authenticated; role-aware (ROLE_TRAINER → trainer-side, else client-side) |
| POST | `/api/trainer/clients/{clientId}/appointments` | CreateAppointmentRequest | AppointmentDTO (201) | **ROLE_TRAINER** + ACCEPTED TRAINER connection to client |
| PUT | `/api/trainer/appointments/{appointmentId}` | UpdateAppointmentRequest | AppointmentDTO | owning trainer only |
| DELETE | `/api/trainer/appointments/{appointmentId}` | — | 204 (**soft cancel**, status→CANCELLED, record preserved) | owning trainer only |

- `AppointmentDTO {id, title, startAt, endAt, notes, status, trainer:AppointmentPartyDTO, client:AppointmentPartyDTO, createdAt, updatedAt}`; `AppointmentPartyDTO {id, firstName, lastName, email}`.
- `CreateAppointmentRequest {title, startAt, endAt?, notes?}`; `UpdateAppointmentRequest {title?, startAt?, endAt?, notes?, status?}`. Times ISO `yyyy-MM-dd'T'HH:mm:ss` LocalDateTime.
- `status` enum `AppointmentStatus {SCHEDULED, COMPLETED, CANCELLED}` (`@Enumerated(STRING)`, null→SCHEDULED on persist).
- Validation (service-layer): create requires non-blank title + startAt; if endAt present it must be after startAt; update re-validates effective start/end.
- **ROLE_TRAINER-only writes enforced in `AppointmentService`** (no `@PreAuthorize`; ADMIN/GYM_OWNER do NOT bypass). Connection check: accepted TRAINER `UserConnection` (owner=client, viewer=trainer). Scheduling only — never creates workout logs.

### Chat — `ChatController` (Phase 7C + admin-support, text chat, no real-time)
| Method | Path | Request | Response | Access |
|---|---|---|---|---|
| GET | `/api/chat/partners` | — | List\<ChatPartnerDTO\> | authenticated; ROLE_TRAINER → accepted clients + admins, ROLE_USER → accepted trainers + admins, ROLE_ADMIN → users with existing support threads, else 403 |
| GET | `/api/chat/{partnerId}/messages` | — | List\<ChatMessageDTO\> (oldest→newest) | authenticated + allowed pair |
| POST | `/api/chat/{partnerId}/messages` | SendMessageRequest `{text}` | ChatMessageDTO (201) | authenticated + allowed pair |

- `ChatPartnerDTO {id, firstName, lastName, email, support}` (`support=true` when the partner is an admin / thread is support); `ChatMessageDTO {id, text, fromMe, senderName, createdAt}` — **`fromMe` computed server-side** (`UserDTO` carries no id).
- `ChatMessage` stores a **canonical participant pair** `userLow`/`userHigh` (by id) + `sender` — one thread per pair, role-agnostic. (Replaced the earlier trainer/client columns.)
- **Allowed pair** = accepted TRAINER `UserConnection` (owner=client, viewer=trainer, ACCEPTED, either direction) **OR exactly one side is ROLE_ADMIN** (admin support). Enforced on read + send. Admin is only ever a participant of its own support threads → never bypasses private trainer-client chats; unrelated non-admins → 403.
- Validation: text trimmed, non-blank → 400, max 1000 chars → 400. No WebSocket/attachments/edit/read-flags/group.

## Not implemented (frontend must not call yet)

- Admin user management — not implemented (FE AdminUsersPage is a placeholder).
- Connection permission editing (`PATCH /api/connections/{id}/permissions`) — not implemented.
- Article comments/likes/feed, trainer reviews/results photos — not implemented (Articles MVP is read + admin CRUD only).
- Public/unauthenticated Discover — intentionally not implemented (locked: auth-only).
- Image **upload** — only legacy exercise images; gym/trainer use `imageUrl` strings.
- Body-metrics **history/time-series** — metrics are single-value on `UserInfo`; `SharedProgressDTO.bodyMetrics` still reserved null.

> Now implemented (were listed here): profile update + password change (`UserController`), body metrics + calorie calculator, Gym CRUD + Discover module, trainer public profile. Both `API_CONTRACT.md` copies (frontend + backend) updated together on 2026-06-18.
