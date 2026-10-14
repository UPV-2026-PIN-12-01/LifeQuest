---
name: Signup F3 POST + confetti
overview: F3 of frontend signup. FormData POST, errors, confetti success popup with visible TODO. No JWT, no /api/me, no generated tests.
todos:
  - id: f3-api-confetti
    content: "F3 implement + real-app test: FormData signup + confetti TODO popup. No generated tests. Browser e2e photo/skip/409. Stop for review of auth.js and success popup."
    status: completed
isProject: false
---

# F3 — POST + confetti

Slice 4 of 4. Depends on [F0 playerName](signup_f0_playername.plan.md), [F1 landing](signup_f1_landing.plan.md), [F2 character](signup_f2_character.plan.md). Spec: [ai/signup-frontend-spec.md](../../ai/signup-frontend-spec.md). Index: [signup frontend](signup_frontend_321c27fc.plan.md).

First time the wizard hits the API. After 201: confetti, not a dashboard.

## Spec (this slice)

- `POST /api/auth/signup` from the UI
- After 201: confetti popup. Text `TODO remove once dashboard done` on the popup and in a code comment
- Leave `ProfileDto` as `{ username }`
- Skip JWT / `GET /api/me`. Removing the confetti is later, outside PIN-600

## Noise — keep unless you say otherwise

- `fetch` + `FormData`. Do not set `Content-Type` (boundary)
- Vite proxy `/api` → 8080
- Fields: `username`, `email`, `password`, `playerClass` (`Guerrero` / `Arquero` / `Mago`), `playerName`, optional `userIcon`, optional `photo`
- Omit `userIcon` when it is still the class default
- Map known English `{ error }` to Spanish. 400/409/5xx on the card, no confetti
- Disable the button while sending
- Confetti: CSS/JS overlay, no new npm package
- Do not generate tests

## Lands

- `frontend/src/services/auth.js` — `signup(formData)`
- Wire Empezar aventura on `SignupPage`
- Confetti overlay + TODO string (code comment + visible text)
- Stay on this screen after 201 (no dashboard)

## Does not land

JWT, `GET /api/me`, real login, router, expanding `ProfileDto`, real class art, removing the confetti.

## You read

`auth.js`, `SignupPage` submit path, confetti overlay.

## Test

Frontend 5173 + backend 8080. **Do not generate tests.** User-like in the browser:

- Full signup with photo + custom emoji + character name → 201, confetti, TODO text on the popup. Storage avatar + `profile_url` set. `playerName` is the hero name
- Second signup, skip photo, default icon → 201, `profile_url` null, class default icon in DB
- Duplicate username/email/playerName → 409 on the card, no confetti
- Password `123` on step 1 → never hits the API

```sql
select username, "playerName", user_icon, profile_url
from public.users
where username like 'test-can-delete-later-%'
order by username;
```
