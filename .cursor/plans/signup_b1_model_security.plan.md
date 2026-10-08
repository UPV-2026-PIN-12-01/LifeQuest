---
name: Signup B1 model + security
overview: B1 of backend-only POST /api/auth/signup. Map User + CharacterStats, permit-all security. No signup HTTP, no JWT, no frontend.
todos:
  - id: b1-model
    content: "B1 implement + real-app test: model + permit-all security. No generated tests. Boot app, GET /api/hello still 200. Stop for review of User, CharacterStats, PlayerClass."
    status: completed
isProject: false
---

# B1 — model + security (no signup HTTP)

Slice 1 of 3. Next: [B2 signup](signup_b2_signup.plan.md), then [B3 photo](signup_b3_photo.plan.md). Spec: [ai/implement-signup-spec.md](../../ai/implement-signup-spec.md).

Picked **B**: three review stops. Frontend / JWT / `GET /api/me` are **out**.

Spring can map `public.users` + `character_stats`. App still boots. Nothing creates an Auth user yet.

## Spec (target; this slice does not implement HTTP)

- `POST /api/auth/signup` later (B2/B3)
- Password only in `auth.users`, never in `public.users`
- `playerName` = username
- 201 profile DTO = `{ username }` only — land the DTO now, unused until B2

## Noise — keep unless you say otherwise

- Permit-all `SecurityConfig` (OAuth2 starter already in `pom.xml` would otherwise 401 everything)
- English class names + [backend/ARCHITECTURE.md](../../backend/ARCHITECTURE.md) layering
- Class default emojis ⚔️ / 🏹 / 🪄
- Class is on `character_stats` (`knight|archer|magician`) — map now so B2 can insert the row
- Guerrero/Arquero/Mago → knight/archer/magician
- `ddl-auto=none` — map the table, do not recreate it

## Lands

- `User` JPA → `users`: `id`, `username`, `email`, `profileUrl`, `playerName`, `innerId`, `userIcon`, `playerId`, `accountType`. Password column does not exist; we never add one.
- `CharacterStats` JPA → `character_stats`: `playerName`, `userId`, `"class"`. Other stats use DB defaults (hp/level/xp/…).
- `PlayerClass` enum `knight|archer|magician`. Map Guerrero/Arquero/Mago. Default icons ⚔️/🏹/🪄. Blank class → knight.
- `AccountType` `BASIC|PREMIUM` (column exists, default BASIC).
- `UserRepository` / `CharacterStatsRepository` (Spring Data). `existsByUsername` / `existsByEmail` for B2.
- `UserService` / `CharacterStatsService` — create + link (`playerName` = username, `player_id` / `user_id`). Architecture: each service owns its repo. Nobody HTTP-calls them yet.
- `ProfileDto` = `{ username }` + TODO for frontend.
- `SecurityConfig`: all `/api/**` permitAll. No JWT check. No `/api/me`.

## Does not land

`AuthController`, Auth/Storage HTTP, photo, 400/409 signup errors.

## You read

`User`, `CharacterStats`, `PlayerClass`.

## Test

`mvn spring-boot:run`, `GET /api/hello` → 200. **Do not generate tests.**
