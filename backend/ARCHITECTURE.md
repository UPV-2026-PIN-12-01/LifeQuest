# Backend architecture

Layered Spring Boot app under `com.lifequest` (`backend/src/main/java/com/lifequest`).

## Packages

- `LifeQuestApplication` — Spring Boot entry point.
- `config/` — Spring configuration beans (CORS, security, etc.).
- `controller/` — REST endpoints; handle HTTP and call services, no business logic.
- `service/` — Business logic; calls repositories and maps entities to DTOs.
- `repository/` — Data access (Spring Data interfaces), one per entity.
- `model/entity/` — Persistent domain classes (`Usuario`, `Grupo`, `Personaje`, `Equipo`, `Skin`, `Objetivo`, `Tarea`, `Desafio`, `Recompensa`).
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

- `TareaService` may use `TareaRepository`, never `RecompensaRepository`.
- Need a reward? `TareaService` calls `RecompensaService`, which uses `RecompensaRepository`.
- Same for reads: to check a `Grupo` exists, call `GrupoService`, not `GrupoRepository`.
- Services may pass entities to each other, since both are in the service layer.
- The owner service holds that entity's rules (validation, "not found" errors, cascades).

```
OK:   TareaService -> RecompensaService -> RecompensaRepository
BAD:  TareaService -> RecompensaRepository
```
