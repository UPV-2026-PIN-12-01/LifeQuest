---
name: Signup F1 landing
overview: F1 of frontend signup. Full-bleed mockup landing + account step + login stub. No POST. No JWT, no generated tests.
todos:
  - id: f1-landing-step1
    content: "F1 implement + browser test: landing + account step + login stub. No generated tests. Next validates then placeholder step 2. Stop for review of SignupPage, AccountStep."
    status: pending
isProject: false
---

# F1 — landing + step 1

Slice 2 of 4. Depends on [F0 playerName](signup_f0_playername.plan.md). Next: [F2 character](signup_f2_character.plan.md), then [F3 POST](signup_f3_api_confetti.plan.md). Spec: [ai/signup-frontend-spec.md](../../ai/signup-frontend-spec.md). Index: [signup frontend](signup_frontend_321c27fc.plan.md).

Replace the `/api/hello` demo with the mockup shell. Account fields only. No signup POST (F3).

## Spec (this slice)

- Gather: optional photo, required username/email/password (min 6)
- Validate email format + password length on the client (duplicate of backend; named constants + keep-in-sync comment)
- Code English, UI Spanish
- Skip JWT / `GET /api/me`

## Noise — keep unless you say otherwise

- No router. Local state for tabs + steps
- Login tab: chrome only, coming soon
- Full-bleed purple + white card. Relax `#root` (drop 1126px / center / side borders). This page ignores `prefers-color-scheme`
- Photo client check: jpeg/png/webp/gif, 2MB (same as backend). Preview; skip is fine
- `index.html` `lang="es"`
- Do not use [frontend/src/utils/constants.js](../../frontend/src/utils/constants.js) (`MIN_PASSWORD_LENGTH: 8` is wrong and unused)
- Do not generate tests

## Lands

- [frontend/src/App.jsx](../../frontend/src/App.jsx) → shell, render `SignupPage`
- `frontend/src/pages/SignupPage.jsx` — left marketing + card, tabs, stepper
- `frontend/src/components/signup/AccountStep.jsx` — photo, username, email, password
- `frontend/src/utils/validation.js` — `MIN_PASSWORD_LENGTH = 6`, same regex as Java `EMAIL_SHAPE`
- `frontend/src/styles/signup.css`
- [frontend/src/styles/index.css](../../frontend/src/styles/index.css) — `#root` full viewport
- Next: client validation, then step 2 as a **heading-only placeholder** (body is F2)
- Login tab stub

## Does not land

Class cards, character name, emoji picker, `services/auth.js`, POST, confetti, class image files.

## You read

`SignupPage`, `AccountStep`, `validation.js`.

## Test

Frontend: `cd frontend; npm run dev` → http://localhost:5173. Backend optional this slice. **Do not generate tests.**

- Desktop matches mockup 1 (purple left, white card, stepper on Cuenta)
- Empty Next → Spanish field errors. Bad email / password < 6 → stay on step 1
- Photo preview works; skip is fine
- Valid Next → stepper on Personaje (empty until F2)
- Login tab → coming soon. Crear cuenta returns to step 1 with data kept
- Narrow viewport usable
