# API Reference

Full interactive documentation (every endpoint, request/response schema, try-it-out): **`/swagger-ui/index.html`** once the backend is running. This file is a summary map — see Swagger for exact field-level detail.

All responses are JSON. All non-2xx responses share this shape:
```json
{ "success": false, "message": "...", "timestamp": "2026-09-30T12:00:00Z", "errors": ["field: reason", "..."] }
```
`errors` only appears for validation failures. Paginated endpoints accept `?page=0&size=20&sort=field,asc|desc` and return Spring's standard `Page` envelope (`content`, `totalElements`, `totalPages`, ...).

## Authentication
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | public | `role`: STUDENT/TEACHER always allowed; ADMIN only while zero admins exist |
| POST | `/api/auth/login` | public | Returns `{token, userId, name, email, role}` |

Send the token as `Authorization: Bearer <token>` on every other request.

## Learning (public read)
| Method | Path | Auth |
|---|---|---|
| GET | `/api/topics?lang=en\|hi\|mr` | public |
| GET | `/api/topics/{id}?lang=...` | public |
| GET | `/api/quizzes?topicId=...` | public (id/title only, no questions) |
| GET | `/api/quizzes/{id}` | public (answers hidden) |
| POST | `/api/quizzes/{id}/attempt` | STUDENT |

## Student (`/api/student/**`, STUDENT role)
`profile` · `dashboard` · `quests` · `quests/{id}` · `submissions` (+ `/{id}`) · `coin-transactions` · `badges` · `leaderboard?scope=individual|school&period=WEEKLY|MONTHLY|ALL_TIME` · `impact`

Evidence upload is its own route (matches spec exactly): `POST /api/quests/{questId}/submit` (multipart: `image`, `description`, `latitude`, `longitude`).

## Teacher (`/api/teacher/**`, TEACHER role — all scoped to the teacher's own school)
`dashboard` · `students` · `submissions` (+ `/{id}`, `/{id}/approve`, `/{id}/reject`) · `quests` (full CRUD, own-creations-only edit/delete) · `leaderboard` · `impact`

## Admin (`/api/admin/**`, ADMIN role — unrestricted)
- `dashboard` — platform-wide stats
- `schools`, `students`, `teachers` — CRUD/status/delete
- `students/{id}/leaderboard-privacy`, `students/{id}/coin-transactions`, `students/{id}/coins/adjust`
- `topics`, `lessons`, `quizzes`, `quests`, `badges` — full CRUD
- `submissions` (+ `/{id}/approve`, `/{id}/reject`) — override, any school
- `leaderboard?scope=individual|school|city|state`
- `analytics/impact`, `impact-formulas` (GET/PUT/DELETE — configurable per-category formulas)

## WebSocket
Connect: `GET /ws?token=<jwt>` (SockJS-compatible STOMP endpoint). Subscribe to `/user/queue/leaderboard` for personal rank-change pushes (`{oldRank, newRank, message}`), sent automatically whenever a quiz or quest reward changes your rank.

## Error codes
| Status | Meaning |
|---|---|
| 400 | Validation failure or invalid business state (e.g. submitting past `maxSubmissionsPerStudent`) |
| 401 | Missing/invalid/expired JWT |
| 403 | Authenticated but wrong role, or scope violation (e.g. teacher acting outside their school) |
| 404 | Resource not found |
| 409 | Conflict (duplicate email, already-approved submission, duplicate badge code) |
| 500 | Unexpected server error |
