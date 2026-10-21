---
name: User profile B3 character
overview: B3 adds playerName and playerClass to GET and PATCH /api/users/me. Username is never written. A taken character name is 409. Settings saves the character name. No photo, no generated tests.
todos:
  - id: b3-character-name
    content: "B3 implement + real-app test: character fields on GET and PATCH /api/users/me, username ignored, 409 on a taken playerName, settings form writes playerName. No generated tests. Stop for review."
    status: completed
isProject: false
---

# B3 — character name

Slice 3. Depends on [B1](userprofile_b1_read.plan.md) and [B2](userprofile_b2_email.plan.md). Spec: [user-profile2.spec.md](user-profile2.spec.md).

Same `GET /api/users/me` and `PATCH /api/users/me`. Settings reads this response and sends `playerName` on save. Photo stays out.

## Decisions not in the spec

**Important**

- Settings needs two character fields on the user object: `playerName` (editable) and `playerClass` (shown, not edited). `playerClass` is the Spanish label (`Guerrero`, `Arquero`, `Mago`), same as `GET /api/players/me`. A sent `playerClass` is ignored.
- `username` is never written. Drop it from the PATCH body. A sent `username` is ignored. The account name on the page stays the signup username.
- `playerName`: trim. Missing means unchanged. Present and blank is 400 `Player name is required`. Saving your own name is 200. Another character already has it is 409 `Player name already exists` (signup already uses this).
- Write `character_stats.playerName` and flush first, then set `users.playerName` to the same value before the user row is saved. `users.playerName` references `character_stats.playerName` with `ON UPDATE CASCADE`. `User` is not dynamically updated, so a later user save would write the old in-memory name and undo the cascade.
- Uniqueness is checked before the Auth email call. A race on the unique key still returns 409, and a failure after the Auth email changed still puts the old Auth email back. Password stays last.
- Settings loads only `GET /api/users/me`. Save sends `playerName` with email and password. 409 on the name field shows `El nombre del personaje ya existe`. Photo stays a local preview.

**Noise — keep unless you say otherwise**

- `CharacterStatsService` renames and checks `existsByPlayerNameAndUserIdNot`. The rename runs inside `UserService.updateAccount` so the character row and the user row commit together. `UserProfileService` does the 409 check before Auth.
- `userIcon` stays writable on the API. This page does not send it.
- No generated tests. Usernames use `test-can-delete-later`.

## Lands

- Response:

```json
{ "user": { "id": 1, "username": "Ada", "email": "ada@lifequest.test", "userIcon": "🦊", "playerName": "Ada", "playerClass": "Guerrero" } }
```

- PATCH fields: `playerName`, `userIcon`, `email`, `password`. `username` does nothing.
- [`ProfilePage.jsx`](../../frontend/src/pages/ProfilePage.jsx) writes `playerName` and reads `playerName` plus `playerClass` from `/api/users/me`.

Does not land: photo, `profileUrl`, class changes, username changes.

## Test

Implementer and tester are different agents. The implementer does not run this section.

Backend on `http://localhost:8080` (devtools restart after compile). Frontend on `http://localhost:5173`. No generated tests.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$username = "test-can-delete-later-$stamp"
$email = "test-can-delete-later-$stamp@lifequest.test"
$password = "secret1"
$other = "test-can-delete-later-other-$stamp"
$hero = "test-can-delete-later-hero-$stamp"
$otherHero = "test-can-delete-later-other-hero-$stamp"

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$username" `
  --data-urlencode "email=$email" `
  --data-urlencode "password=$password" `
  --data-urlencode "playerClass=Guerrero" `
  --data-urlencode "playerName=$hero" `
  --data-urlencode "userIcon=⚔️"

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$other" `
  --data-urlencode "email=test-can-delete-later-other-$stamp@lifequest.test" `
  --data-urlencode "password=$password" `
  --data-urlencode "playerClass=Mago" `
  --data-urlencode "playerName=$otherHero"

$login = curl.exe -s -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "email=$email" `
  --data-urlencode "password=$password" | ConvertFrom-Json
$token = $login.access_token

# 200 — playerName and playerClass are present; username is the account name
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token"

$renamed = "test-can-delete-later-renamed-$stamp"
$patchFile = Join-Path $env:TEMP "lq-users-me-$stamp.json"

# 200 — character name changes; username in the body is ignored
@{ playerName = $renamed; username = $other } | ConvertTo-Json -Compress | Set-Content -Path $patchFile -Encoding ascii -NoNewline
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary "@$patchFile"

# 200 — home still reads the new character name; username unchanged
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/players/me" `
  -H "Authorization: Bearer $token"

# 200 — saving the same name again
@{ playerName = $renamed } | ConvertTo-Json -Compress | Set-Content -Path $patchFile -Encoding ascii -NoNewline
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary "@$patchFile"

# 409 — the other character's name; then GET still has $renamed and the original username
@{ playerName = $otherHero } | ConvertTo-Json -Compress | Set-Content -Path $patchFile -Encoding ascii -NoNewline
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary "@$patchFile"

curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token"

# 400 — blank character name
Set-Content -Path $patchFile -Value '{"playerName":"   "}' -Encoding ascii -NoNewline
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary "@$patchFile"
```

PATCH bodies go through a file. Passing JSON straight to `curl.exe` from PowerShell drops the quotes.

Expect: GET `user.playerName` is `$hero` and `user.playerClass` is `Guerrero`. After the rename, `user.username` is still `$username`, `user.playerName` is `$renamed`, and `GET /api/players/me` `playerName` is `$renamed`. 409 body is `{ "error": "Player name already exists" }`. The following GET still has `$renamed` and `$username`.
