# SIH Demo Flow

A live, end-to-end walkthrough using the seeded demo data (`SEED_DATA=true` — see README). Two browser windows side by side (or two tabs) make the live WebSocket moment land better: one logged in as a student, one as their teacher.

Runtime: ~6–8 minutes.

---

## Setup (before judges arrive)
1. Backend and frontend both running, `SEED_DATA=true` has been run once.
2. Tab A: logged in as **`student4@ecoquest.com`** / `Demo@1234` (a fresh student, no prior activity — `student1`–`student3` already have seeded activity, so use `student4`+ for a clean "before" state).
3. Tab B: logged in as **`teacher1@ecoquest.com`** / `Demo@1234` (same school as `student4`).
4. If deployed on Render's free tier: hit `/api/health` a minute beforehand so the service isn't cold.

---

## The walkthrough

**1. Learning (Tab A)**
"This is EcoQuest India — it starts where every EVS class starts: learning." → `/student/learn` → open **Waste Management** → show a lesson, point out the "local context" note (Swachh Bharat framing) and the language switcher (English/Hindi/Marathi).

**2. Quiz → first reward**
Open the Waste Management quiz → answer the 3 questions → submit.
"Notice it's graded instantly, shows explanations for every question, and — this part matters — it actually pays out: real Eco-Coins and XP, not a fake number." → glance at the dashboard: Eco-Coins/XP just went from 0 to something real.

**3. Real-world quest**
`/student/quests` → open **Tree Guardian** (gpsRequired, 200 coins). "This is the actual point of the platform — EVS isn't just something you read about."
→ **Start Quest** → upload a photo (a tree photo you have ready) → tap **Capture My Location** (your actual browser location) → **Submit Proof**.
"That request just went to our AI service for an advisory first pass, and landed in the teacher's queue — nothing gets rewarded yet."

**4. AI + teacher review (Tab B)**
`/teacher/submissions` → filter **AI Reviewed** → open the new submission.
"Here's the photo, the AI's advisory read — confidence and reasoning — and the GPS distance from the expected planting location. The AI never makes the final call." → click **Approve**.

**5. The payoff, live (Tab A)**
Switch back to Tab A **without refreshing**. A toast should appear: *"🏆 You moved from #X → #Y!"* — pushed over the WebSocket the instant the teacher approved.
"That's real-time — no polling, no refresh." → open the dashboard: Eco-Coins/XP updated, streak now at 1.

**6. Badges**
`/student/badges` → **First Step** is now unlocked (color, with an earned date) — the rest still grayed out. "Badges unlock automatically the moment criteria are met — this one just did, live."

**7. Impact**
`/student/impact` → "🌳 Trees Planted: 1" appears, with the estimate disclaimer visible. "This is a configurable formula, not a precise measurement — and admins can tune it per category."

**8. Leaderboard**
`/student/leaderboard` → toggle Weekly/Monthly/All-time, Individual/School. Point out the student's own row is highlighted.

**9. Admin view (new tab, `admin@ecoquest.com`)**
`/admin/dashboard` → point out totals moved (students, coins issued, trees planted) reflecting exactly what just happened. `/admin/analytics` → show the same Tree Guardian impact rolled up platform-wide, and the editable impact-formula table.

---

## If something goes wrong live
- **WebSocket toast doesn't appear:** not fatal — say so, refresh the dashboard, and continue; the reward and badge are still real, just not the live push.
- **AI section shows nothing:** confirm `AI_PROVIDER=mock` is set; if it's `gemini` and the key/network fails, the system silently falls back to mock — also fine, say "verified with our mock AI service since we're on a venue network."
- **Render free-tier cold start:** if the very first request lags, narrate it: "this is a free-tier cold start, not the platform being slow."

## What this flow proves, in one sentence
Every number the judges see — coins, XP, badge, impact, rank — was computed by the system in response to something that just happened on screen, not pre-loaded or faked.
