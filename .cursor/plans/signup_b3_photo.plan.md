---
name: Signup B3 photo upload
overview: B3 of backend-only POST /api/auth/signup. Optional photo to avatars Storage, set profile_url. No JWT, no /api/me, no frontend.
todos:
  - id: b3-photo
    content: "B3 implement + real-app test: photo to avatars. No generated tests. POST with image, confirm Storage object + profile_url; skip stays null. Stop for review of SupabaseStorageService."
    status: completed
isProject: false
---

# B3 — photo

Slice 3 of 3. Depends on [B1](signup_b1_model_security.plan.md) and [B2](signup_b2_signup.plan.md). Spec: [ai/implement-signup-spec.md](../../ai/implement-signup-spec.md).

Picked **B**: three review stops. Frontend / JWT / `GET /api/me` are **out**.

Same endpoint as B2, now accepts bytes. User already exists before Storage runs, so a failed upload does not delete them.

## Spec (this slice)

- Photo → Storage bucket `avatars` → `profile_url`; skip → null
- If photo upload fails after users insert: leave `profile_url` null (spec only compensates the users insert, not storage)

## Noise — keep unless you say otherwise

- Multipart on the same POST
- 400 also for bad photo
- Avatars bucket already exists
- Photo 2MB + jpeg/png/webp/gif
- RestClient, no Supabase Java SDK. No generated tests (development-plan)

```mermaid
sequenceDiagram
  participant API
  participant Storage
  participant DB
  Note over API: user + Auth already exist (B2)
  alt photo present
    API->>Storage: upload avatars
    API->>DB: set profile_url
  end
  alt upload fails or skipped
    API-->>API: profile_url stays null; user stays
  end
```



## Lands

- `photo` optional `MultipartFile` on the same POST (multipart). jpeg/png/webp/gif, 2MB (already in `application.properties` + bucket). Else 400.
- `SupabaseStorageService`: `POST /storage/v1/object/avatars/{innerId}/...` then public URL into `users.profile_url`.
- Skip `photo` → `profile_url` stays null.
- Storage fail → user + Auth stay; `profile_url` stays null. No Auth compensation (spec only compensates the users insert).

## You read

`SupabaseStorageService`.

## Test

Backend running on `http://localhost:8080`. **Do not generate tests.** Real app only.

- jpeg multipart → 201 `{ "username": "..." }`. Supabase: `avatars/{inner_id}/avatar.jpg` exists, `public.users.profile_url` is the public URL, GET that URL → 200 `image/jpeg`. Auth user stays.
- no photo (urlencoded, same as B2) → 201. `profile_url` null. Auth + `public.users` stay.
- `.txt` as `photo` → 400. No Auth user, no `public.users` row.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$jpegPath = Join-Path $env:TEMP "lifequest-avatar-$stamp.jpg"
$txtPath = Join-Path $env:TEMP "lifequest-notes-$stamp.txt"
$jpegB64 = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAn/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAGcP/EABQQAQAAAAAAAAAAAAAAAAAAACP/EABQRAQAAAAAAAAAAAAAAAAAAAAD/2gAIAQEAAT8Af//Z"
[IO.File]::WriteAllBytes($jpegPath, [Convert]::FromBase64String($jpegB64))
Set-Content -Path $txtPath -Value "not an image" -NoNewline

$photoUser = "test-can-delete-later-photo-$stamp"
$skipUser = "test-can-delete-later-skip-$stamp"
$badUser = "test-can-delete-later-badphoto-$stamp"
$password = "secret1"
Write-Host "STAMP=$stamp PHOTO=$photoUser SKIP=$skipUser BAD=$badUser"

# 201 — jpeg uploaded
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -F "username=$photoUser" `
  -F "email=$photoUser@lifequest.test" `
  -F "password=$password" `
  -F "playerClass=Guerrero" `
  -F "photo=@$jpegPath;type=image/jpeg"

# 201 — skip photo, profile_url stays null
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=$skipUser" `
  --data-urlencode "email=$skipUser@lifequest.test" `
  --data-urlencode "password=$password" `
  --data-urlencode "playerClass=Guerrero"

# 400 — bad photo, no user created
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
  -F "username=$badUser" `
  -F "email=$badUser@lifequest.test" `
  -F "password=$password" `
  -F "playerClass=Guerrero" `
  -F "photo=@$txtPath;type=text/plain"
```

Then in Supabase SQL (paste `$stamp` from the script output). Photo row has `profile_url`; skip row is null; bad username is missing. Then `curl.exe` GET the photo `profile_url` → 200 `image/jpeg`.

```sql
select username, profile_url, inner_id
from public.users
where username like 'test-can-delete-later-%'
order by username;

select name, metadata->>'mimetype' as mimetype
from storage.objects
where bucket_id = 'avatars'
  and name like '%/avatar.jpg'
  and created_at > now() - interval '10 minutes';

select email from auth.users
where email like 'test-can-delete-later-%';
```

