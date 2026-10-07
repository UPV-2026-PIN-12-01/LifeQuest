# Backend architecture

Layered Spring Boot app under `com.lifequest` (`backend/src/main/java/com/lifequest`).

## Packages

- `LifeQuestApplication` — Spring Boot entry point.
- `config/` — Spring configuration beans (CORS, security, etc.).
- `controller/` — REST endpoints; handle HTTP and call services, no business logic.
- `service/` — Business logic; calls repositories and maps entities to DTOs.
- `repository/` — Data access (Spring Data interfaces), one per entity.
- `model/entity/` — Persistent domain classes (`User`, `Group`, `CharacterStats`, `Equipment`, `Skin`, `Goal`, `Task`, `Challenge`, `Reward`). Class names are full English.
- `model/dto/` — Request/response objects exposed by the API; never return entities.
- `exception/` — Custom exceptions and the global error handler.

## Flow

`controller` → `service` → `repository` → DB, with DTOs at the controller edge and entities below it.

## Rules

- Controllers never touch repositories directly.
- Entities never leave the service layer.
- A service uses only its own repository; for other entities it calls their service.
- Tests mirror this structure under `src/test/java/com/lifequest/`.

## Details

### About the rule "A service uses only its own repository; for other entities it calls their service."

- `TaskService` may use `TaskRepository`, never `RewardRepository`.
- Need a reward? `TaskService` calls `RewardService`, which uses `RewardRepository`.
- Same for reads: to check a `Group` exists, call `GroupService`, not `GroupRepository`.
- Services may pass entities to each other, since both are in the service layer.
- The owner service holds that entity's rules (validation, "not found" errors, cascades).

```
OK:   TaskService -> RewardService -> RewardRepository
BAD:  TaskService -> RewardRepository
```
