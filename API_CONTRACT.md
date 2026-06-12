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

## Not implemented (frontend must not call yet)

- Admin user management — not implemented. Frontend AdminUsersPage is only a placeholder; decide whether to implement minimal admin user management or hide the page for the demo (see `TODO_BACKEND.md`).
- Profile (`UserInfo`) read/update endpoints — planned.
- Body metrics endpoints — planned.
- Gym endpoints — entity only, no controller.
- Password change/reset; trainer client-assignment write endpoints.
