# Database Reference (MongoDB)

## Relationship diagram

References are by string id (Mongo doesn't enforce foreign keys) — the diagram below shows the logical relationships.

```mermaid
erDiagram
    USER ||--o{ QUEST_SUBMISSION : submits
    USER ||--o{ QUIZ_ATTEMPT : attempts
    USER ||--o{ ECO_COIN_TRANSACTION : earns
    USER ||--o{ STUDENT_BADGE : unlocks
    USER }o--|| SCHOOL : "belongs to (studentProfile.schoolId / teacherProfile.assignedSchoolId)"
    QUEST ||--o{ QUEST_SUBMISSION : "has submissions"
    QUEST }o--|| SCHOOL : "scoped to (nullable = all schools)"
    EVS_TOPIC ||--o{ LESSON : contains
    EVS_TOPIC ||--o{ QUIZ : has
    QUEST_SUBMISSION ||--o{ IMPACT_METRIC : "generates on approval"
    BADGE ||--o{ STUDENT_BADGE : "awarded as"
    QUEST_CATEGORY ||--o| IMPACT_FORMULA_CONFIG : "maps to"

    USER {
        string id PK
        string email UK
        string passwordHash
        string role "STUDENT | TEACHER | ADMIN"
        object studentProfile "embedded - null unless role=STUDENT"
        object teacherProfile "embedded - null unless role=TEACHER"
    }
    SCHOOL {
        string id PK
        string name
        string city
        string state
    }
    QUEST {
        string id PK
        string title
        string category
        int ecoCoinsReward
        int xpReward
        string schoolId FK "nullable"
        string createdByUserId FK
    }
    QUEST_SUBMISSION {
        string id PK
        string questId FK
        string studentId FK
        string status "PENDING|AI_REVIEWED|APPROVED|REJECTED"
        bool rewardGranted
        object aiVerification "embedded"
    }
    ECO_COIN_TRANSACTION {
        string id PK
        string studentId FK
        int amount
        string type
        string referenceId
    }
    EVS_TOPIC {
        string id PK
        object title "LocalizedText: en/hi/mr"
    }
    LESSON {
        string id PK
        string topicId FK
    }
    QUIZ {
        string id PK
        string topicId FK
        array questions "embedded"
    }
    QUIZ_ATTEMPT {
        string id PK
        string studentId FK
        string quizId FK
        bool rewarded
    }
    BADGE {
        string id PK
        string criteriaType
        int criteriaThreshold
    }
    STUDENT_BADGE {
        string id PK
        string studentId FK
        string badgeId FK
    }
    IMPACT_METRIC {
        string id PK
        string type
        double value
        string submissionId FK
    }
    IMPACT_FORMULA_CONFIG {
        string id PK
        string category UK
        string impactType
    }
```

## Collections

| Collection | Purpose | Key indexes |
|---|---|---|
| `users` | Every person — role-specific data embedded (`studentProfile`/`teacherProfile`) | unique `email` |
| `schools` | School directory | — |
| `evs_topics` | Learning topics, localized (`title`/`description` as `{en,hi,mr}`) | — |
| `lessons` | Bite-sized content units under a topic | `topicId` |
| `quizzes` | Quiz + embedded `questions[]` (never sent to the client with `correctOptionIndex` before grading) | `topicId` |
| `quiz_attempts` | One row per attempt; `rewarded` guards against repeat-reward abuse | compound `(studentId, quizId)` |
| `quests` | Eco-challenge definitions, including geofence config | `createdByUserId` |
| `quest_submissions` | Evidence + AI result (embedded) + review outcome; `rewardGranted` is the once-only guard | `questId`, `studentId`, `status`, compound `(studentId, questId)` |
| `eco_coin_transactions` | **Source of truth** for all coin balances — append-only ledger | `studentId`, `referenceId` |
| `badges` | Badge definitions and unlock criteria | unique `code` |
| `student_badges` | Which student earned which badge, and when | unique compound `(studentId, badgeId)` |
| `leaderboard_snapshots` | Reserved for a future caching layer — leaderboards are currently computed live | — |
| `notifications` | Reserved — notification creation isn't wired into any flow yet | `userId` |
| `impact_metrics` | One row per approved submission's estimated environmental contribution | `studentId`, `submissionId` |
| `impact_formula_configs` | Admin overrides for the category→impact-metric formulas (falls back to hardcoded defaults) | unique `category` |

## Why cached balances instead of always summing the ledger

`User.studentProfile.ecoCoins`/`xp` are denormalized caches, updated atomically (`$inc`) by `EcoCoinService.award(...)` at the same time a ledger entry is appended. This keeps dashboard/leaderboard reads fast (no aggregation needed for the common case) while `eco_coin_transactions` remains the audit trail and the thing admin adjustments and weekly/monthly leaderboard windows actually compute from.
