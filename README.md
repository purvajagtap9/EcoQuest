# 🌱 EcoQuest India

**Gamified Environmental Education & Real-World Sustainability Platform** — an SIH prototype that turns Environmental Studies from passive textbook learning into interactive learning plus verified, measurable real-world action.

Students learn EVS topics → take quizzes → complete real-world eco-quests (photo + GPS evidence) → get an AI first-pass review → get final teacher approval → earn Eco-Coins, XP, badges, and see their real environmental impact — all backed by a live leaderboard.

---

## Table of contents
- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Quick start](#quick-start)
  - [Option A — Docker](#option-a--docker-fastest)
  - [Option B — Manual](#option-b--manual-setup)
- [Environment variables](#environment-variables)
- [Demo accounts & seed data](#demo-accounts--seed-data)
- [Documentation](#documentation)
- [Testing](#testing)
- [Building for production](#building-for-production)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Business rules this system enforces](#business-rules-this-system-enforces)
- [Future improvements](#future-improvements)

---

## Features

**Student:** interactive multi-language (English/Hindi/Marathi) EVS lessons, quizzes with instant scoring and explanations, real-world eco-quests with photo+GPS evidence upload, personal dashboard (Eco-Coins, level/XP, streak, rank), badges, leaderboards (individual/school, weekly/monthly/all-time), personal environmental impact tracking, live rank-change notifications.

**Teacher:** submission review queue (photo, AI advisory verdict, location/map, distance-from-target) with approve/reject + required rejection reason, quest creation scoped to their own school, class roster, class leaderboard, class impact.

**Admin:** platform analytics dashboard, full CRUD for schools/students/teachers/topics/lessons/quizzes/quests/badges, submission override (any school), manual Eco-Coin adjustments (ledgered), configurable impact formulas, city/state/national leaderboards.

**Platform-wide:** JWT auth with role-based access control, append-only Eco-Coin ledger (never a bare editable number), duplicate-reward prevention, AI verification that's strictly advisory (mock by default, real Gemini vision optional), WebSocket live leaderboard pushes, configurable per-category environmental impact formulas with clear "estimate" labeling.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18 + Vite, React Router, Axios, Tailwind CSS, Recharts, Lucide icons |
| Backend | Java 17, Spring Boot 3 (Web, Security, Data MongoDB, Validation, WebSocket), JWT (jjwt), Lombok, springdoc-openapi |
| Database | MongoDB 7 (Spring Data MongoDB — **not** JPA/SQL; see `docs/architecture.md` for why) |
| AI | Google Gemini (vision) or a built-in mock, selectable via `AI_PROVIDER` |
| Real-time | STOMP over WebSocket (SockJS-compatible) |
| Testing | JUnit 5 + Mockito |
| Containerization | Docker + docker-compose |

## Project structure

```
ecoquest-india/
├── backend/            Spring Boot API
├── frontend/            React/Vite SPA
├── database/            (MongoDB is schemaless — see docs/database.md for the collection reference)
├── docs/
│   ├── architecture.md   System diagram, layering, key design decisions
│   ├── database.md       Collection reference + ER-style relationship diagram
│   ├── api.md            API summary (full detail lives in Swagger UI at runtime)
│   └── deployment.md     Step-by-step deploy runbook (Atlas + Render + Vercel)
├── docker-compose.yml
├── render.yaml           Render blueprint for the backend
└── .env.example          Root env template for docker-compose
```

## Quick start

### Option A — Docker (fastest)

```bash
cp .env.example .env      # edit JWT_SECRET at minimum
docker compose up --build
```
- Frontend: http://localhost:3000
- Backend: http://localhost:8080 (health check: `/api/health`)

### Option B — Manual setup

**Prerequisites:** Java 17+, Maven, Node.js 20+, MongoDB (local or Atlas).

```bash
# 1. Database — local Mongo via Docker (or point at Atlas, see docs/deployment.md)
docker run -d -p 27017:27017 --name ecoquest-mongo mongo:7

# 2. Backend
cd backend
cp .env.example .env      # edit MONGODB_URI / JWT_SECRET as needed
mvn spring-boot:run
# → http://localhost:8080

# 3. Frontend (new terminal)
cd frontend
npm install
cp .env.example .env
npm run dev
# → http://localhost:5173
```

## Environment variables

See `backend/.env.example` and `frontend/.env.example` for the full annotated list. The essentials:

| Variable | Where | Required | Notes |
|---|---|---|---|
| `MONGODB_URI` | backend | yes | Local or Atlas connection string |
| `JWT_SECRET` | backend | yes | **≥32 characters** — app fails fast otherwise |
| `AI_PROVIDER` | backend | no | `mock` (default) or `gemini` |
| `GEMINI_API_KEY` | backend | only if `AI_PROVIDER=gemini` | Never sent to the frontend |
| `SEED_DATA` | backend | no | `true` once, on an empty DB, to populate demo data |
| `VITE_API_URL` | frontend | yes | Backend base URL |

Never commit a real `.env` file — only the `.env.example` templates are tracked.

## Demo accounts & seed data

Set `SEED_DATA=true` (once, on a fresh database) and restart the backend to populate 3 schools, ~13 demo accounts, 10 EVS topics with 20 lessons and 30 quiz questions, 15 quests across every category, 10 badges, and a couple of sample submissions/quiz attempts with real, consistent reward data. It's idempotent — it skips automatically once any school exists.

All demo accounts share the password **`Demo@1234`**:
- `admin@ecoquest.com`
- `teacher1@ecoquest.com`, `teacher2@ecoquest.com`, `teacher3@ecoquest.com`
- `student1@ecoquest.com` … `student9@ecoquest.com`

## Documentation
- **[Architecture](docs/architecture.md)** — system diagram, layering, why MongoDB, reward-integrity design
- **[Database](docs/database.md)** — collection reference, ER-style relationship diagram
- **[API](docs/api.md)** — endpoint map (full schema in Swagger UI: `/swagger-ui/index.html`)
- **[Deployment](docs/deployment.md)** — Atlas + Render + Vercel, step by step
- **[SIH Demo Flow](docs/demo-flow.md)** — the live presentation script, using the seeded demo accounts

## Testing

```bash
cd backend
mvn test
```
Unit tests (JUnit 5 + Mockito) cover: registration/duplicate-email/admin-bootstrap guard, login, quiz grading + duplicate-reward prevention, quest submission approval/rejection + the reward-once guard, teacher school-scoping, streak calculation, badge unlock criteria, and quest eligibility rules.

## Building for production

```bash
# Backend
cd backend && mvn clean package -DskipTests
java -jar target/*.jar

# Frontend
cd frontend && npm run build
# → dist/ is a static bundle, servable by any static host or the nginx config in Docker
```

## Deployment

See **[docs/deployment.md](docs/deployment.md)** for the full walkthrough (MongoDB Atlas → Render → Vercel), including the two free-tier caveats worth knowing before a live demo (Render free-tier cold starts, ephemeral local file storage).

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Backend won't start: `app.jwt.secret must be at least 32 characters` | `JWT_SECRET` too short — generate one with `openssl rand -base64 64` |
| `401` on every request despite a valid-looking token | Token expired (default 24h), or `Authorization: Bearer <token>` header missing |
| CORS error in browser console | `CORS_ALLOWED_ORIGINS` doesn't exactly match the frontend's origin (scheme/port/trailing slash) |
| Quiz/quest reward is `0` on a repeat attempt/submission | Intentional — duplicate-reward prevention (business rule #1) |
| Uploaded image 404s after a redeploy | Local disk storage is ephemeral on most free hosting tiers — see `docs/deployment.md` |
| WebSocket won't connect | Confirm `/ws?token=<jwt>` is reachable and not blocked by a proxy that strips WebSocket upgrade headers |

## Business rules this system enforces

1. A student cannot receive rewards twice for the same approved submission.
2. Rejected submissions never award Eco-Coins.
3. Teacher approval awards rewards exactly once.
4. A teacher can only verify submissions from students in their own assigned school.
5. Admin can override any verification decision, regardless of school.
6. Students cannot modify approved submissions (no update endpoint exists for them).
7. Students cannot directly modify their Eco-Coin balance — only `EcoCoinService.award(...)` can.
8. Leaderboard values are always derived from actual earned rewards, never hand-set.
9. AI verification is advisory only — it never itself approves, rejects, or awards anything.
10. GPS is only mandatory for quests explicitly configured to require it.
11. Uploads are validated for type, size, and non-emptiness before acceptance.
12. Every state-changing action is timestamped.

## Future improvements

- Real object storage (S3/Cloudinary) — `StorageService` is already an interface specifically so this is a drop-in swap
- Notification persistence + delivery (the `Notification` entity/collection exists but nothing writes to it yet)
- `LeaderboardSnapshot` caching + WebSocket broadcast diffing at scale (current leaderboards compute live, which is fine at prototype scale)
- An admin-only "create additional admin" endpoint, rather than the current bootstrap-only public registration path
- Push notification delivery to a mobile app
- Automated seed-data reset endpoint for repeated demo runs without a full DB wipe
