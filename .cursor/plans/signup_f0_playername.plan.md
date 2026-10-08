---
name: Signup F0 playerName
overview: F0 of frontend signup. Optional playerName on POST /api/auth/signup. No UI. No JWT, no /api/me, no generated tests.
todos:
  - id: f0-playername
    content: "F0 implement + real-app test: optional playerName on signup. No generated tests. Curl 201 distinct name, 201 omit=username, 409 duplicate. Stop for review of SignupService, AuthController."
    status: completed
isProject: false
---

# F0 — playerName on signup

Slice 1 of 4. Next: [F1 landing](signup_f1_landing.plan.md), [F2 character](signup_f2_character.plan.md), [F3 POST](signup_f3_api_confetti.plan.md). Spec: [ai/signup-frontend-spec.md](../../ai/signup-frontend-spec.md). Index: [signup frontend](signup_frontend_321c27fc.plan.md).

Independent backend slice. UI comes in F1. JWT / `GET /api/me` stay out.

`playerName` is no longer forced to username. Optional param; blank/omit still equals username so old curl works.

## Spec (this slice)

- Expand signup so character name can differ from username
- API/DB name stays `playerName` (UI later: "Nombre del personaje")
- Leave `ProfileDto` as `{ username }`
- Validation constants: keep-in-sync comment on the existing Java names (`MIN_PASSWORD_LENGTH`, `EMAIL_SHAPE`)

## Noise — keep unless you say otherwise

- Omit/blank `playerName` → username
- Unique in DB — 409 before Auth create. Check in `CharacterStatsService` (own repo)
- Integrity catch message includes player name
- Do not generate tests

## Lands

- [AuthController.java](../../backend/src/main/java/com/lifequest/controller/AuthController.java): optional `@RequestParam String playerName`
- [SignupService.java](../../backend/src/main/java/com/lifequest/service/SignupService.java): resolve text or fall back to username; pass that into `CharacterStatsService.createForUser` (today it passes `trimmedUsername`)
- `CharacterStatsService.existsByPlayerName` + repo method
- Duplicate `playerName` → 409, same `{ "error": "..." }` shape
- Comment on `MIN_PASSWORD_LENGTH` / `EMAIL_SHAPE`: keep in sync with `frontend/src/utils/validation.js` (file lands in F1)

## Does not land

Frontend, photo changes, JWT, `/api/me`, expanding `ProfileDto`.

## You read

`SignupService`, `AuthController`, `CharacterStatsService`.

## Test

Backend on `http://localhost:8080`. **Do not generate tests.**

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$hero = "test-can-delete-later-hero-$stamp"
$user = "test-can-delete-later-pn-$stamp"
# 201 — playerName distinct from username
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$user" `
  --data-urlencode "email=$user@lifequest.test" `
  --data-urlencode "password=secret1" `
  --data-urlencode "playerClass=Guerrero" `
  --data-urlencode "playerName=$hero"
# 201 — omit playerName → stored as username
$skip = "test-can-delete-later-pnskip-$stamp"
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$skip" `
  --data-urlencode "email=$skip@lifequest.test" `
  --data-urlencode "password=secret1"
# 409 — same playerName
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=test-can-delete-later-pndup-$stamp" `
  --data-urlencode "email=test-can-delete-later-pndup-$stamp@lifequest.test" `
  --data-urlencode "password=secret1" `
  --data-urlencode "playerName=$hero"
```

Supabase: first row `playerName` = `$hero`; skip row `playerName` = username; dup has no Auth / `public.users` row.

```sql
select username, "playerName"
from public.users
where username like 'test-can-delete-later-%'
order by username;
```
