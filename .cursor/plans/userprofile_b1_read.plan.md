---
name: User profile B1 read
overview: B1 of GET /api/users/me and PATCH username plus userIcon. Email and password are B2. No profile photo, no frontend, no generated tests.
todos:
  - id: b1-read-username-icon
    content: "B1 implement + real-app test: GET /api/users/me plus PATCH username and userIcon, 409 for a taken username. No generated tests. Stop for review."
    status: completed
isProject: false
---

# B1 — read, username, icon

Slice 1 of 2. Next: [B2 email and password](userprofile_b2_email.plan.md). Spec: [userprofile-backend.spec.md](userprofile-backend.spec.md).

Frontend profile page stays as it is. It still reads email from `GET /api/auth/me` and does not call these endpoints. Stop for review after this slice. Email and password are not accepted yet.

## Decisions not in the spec

**Important**

- One `PATCH /api/users/me`, not one route per field. Together with the GET, that is the “new endpoints”. The form saves once; a blank `password` is a field on that body (B2).
- Writable fields: `username`, `email`, `userIcon`, `password`. This slice writes `username` and `userIcon` only. `playerName` and class stay as signup left them. The page’s “Nombre” is `users.username`.
- Partial JSON body. A missing field is unchanged. A present `username` or `userIcon` that is blank is 400. (`email` and `password` blank rules are B2.)
- PATCH returns the same `{ "user": { id, username, email, userIcon } }` as GET. `id` is `users.id`. No `profileUrl`. Email in that body comes from `users.email`, not the JWT.
- Saving your own username is 200, not 409. 409 only when another row already has it. Message already exists: `Username already exists`.

**Noise — keep unless you say otherwise**

- New `UserController` plus a `UserProfileService` that calls `UserService` and `SupabaseAuthService`. Same layering as signup. RestClient, no Supabase SDK.
- 400 via existing `InvalidRequestException`. 404 via existing “User not found” when the JWT subject has no `users` row. 401 is Spring Security (route is not public).
- `userIcon` is any non-blank string, same as signup. No emoji allow-list.
- No generated tests. Do not change [`OpenApiConfigTest`](../../backend/src/test/java/com/lifequest/config/OpenApiConfigTest.java); the new routes pick up the default bearer scheme.
- Test rows use `test-can-delete-later` in the username.

## Lands

- [`UserController`](../../backend/src/main/java/com/lifequest/controller/UserController.java) `GET /api/users/me` and `PATCH /api/users/me`.
- Resolve the row with [`UserService.getIdByAuthSubject`](../../backend/src/main/java/com/lifequest/service/UserService.java) (JWT subject = `users.inner_id`), same as [`PlayerController`](../../backend/src/main/java/com/lifequest/controller/PlayerController.java).
- GET body:

```json
{ "user": { "id": 1, "username": "Ada", "email": "ada@lifequest.test", "userIcon": "🦊" } }
```

- PATCH body fields this slice: `username`, `userIcon`. Unknown `email` / `password` keys are ignored until B2 (Jackson default).
- 409 when the new username belongs to someone else. Own username is fine.
- [`UserRepository`](../../backend/src/main/java/com/lifequest/repository/UserRepository.java): `existsByUsernameAndIdNot`.

Does not land: email write, password write, `profileUrl`, frontend wiring.

## Test

Implementer and tester are different agents. The implementer does not run this section.

Backend already on `http://localhost:8080`. No generated tests.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$username = "test-can-delete-later-$stamp"
$email = "test-can-delete-later-$stamp@lifequest.test"
$password = "secret1"
$other = "test-can-delete-later-other-$stamp"

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$username" `
  --data-urlencode "email=$email" `
  --data-urlencode "password=$password" `
  --data-urlencode "playerClass=Guerrero" `
  --data-urlencode "userIcon=⚔️"

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$other" `
  --data-urlencode "email=test-can-delete-later-other-$stamp@lifequest.test" `
  --data-urlencode "password=$password" `
  --data-urlencode "playerClass=Mago"

$login = curl.exe -s -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "email=$email" `
  --data-urlencode "password=$password" | ConvertFrom-Json
$token = $login.access_token

# 401
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/users/me"

# 200 — user.email is users.email; no profileUrl
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token"

$newName = "test-can-delete-later-renamed-$stamp"
$patch = @{ username = $newName; userIcon = "🦊" } | ConvertTo-Json -Compress
# 200
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary $patch

# 200 — same username again
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary (@{ username = $newName } | ConvertTo-Json -Compress)

# 409 — the other user's username
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary (@{ username = $other } | ConvertTo-Json -Compress)

# 400 — blank username
curl.exe -s -w "`nHTTP:%{http_code}" -X PATCH "http://localhost:8080/api/users/me" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  --data-binary '{"username":"   "}'
```

Expect: GET `user.username` is the renamed value and `user.userIcon` is `🦊`. `GET /api/players/me` still has the original `playerName`. 409 body is `{ "error": "Username already exists" }`.
