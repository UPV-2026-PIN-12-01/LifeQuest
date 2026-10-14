---
name: Signup F2 character
overview: F2 of frontend signup. Class cards with 3 image placeholders, character name, emoji picker, wizard back/next. No POST. No generated tests.
todos:
  - id: f2-character-step
    content: "F2 implement + browser test: class cards, placeholders, character name, emoji picker. No generated tests. Volver keeps step 1. Stop for review of CharacterStep, playerClasses, assets/classes."
    status: completed
isProject: false
---

# F2 — step 2 character

Slice 3 of 4. Depends on [F0](signup_f0_playername.plan.md) and [F1 landing](signup_f1_landing.plan.md). Next: [F3 POST](signup_f3_api_confetti.plan.md). Spec: [ai/signup-frontend-spec.md](../../ai/signup-frontend-spec.md). Index: [signup frontend](signup_frontend_321c27fc.plan.md).

Fill the Personaje step. Still no fetch. Mockups 2–3 are the same page, scrolled.

## Spec (this slice)

- Clase (Guerrero / Arquero / Mago) required in UI, default Guerrero
- Icono emoji optional; class default if skipped
- Three class-portrait **placeholders**; real images later
- Code English, UI Spanish

## Noise — keep unless you say otherwise

- Default Guerrero. Selected state matches mockup (red-ish on Guerrero)
- Emoji picker ~18; class defaults sword / bow / wand. Changing class resets the icon only if it was still the previous default
- Placeholders: `frontend/src/assets/classes/guerrero.svg`, `arquero.svg`, `mago.svg` (labeled boxes; swap files later). Not emoji portraits, not fake photos
- Empezar aventura: validate character name; no request yet
- Do not generate tests

## Lands

- `frontend/src/components/signup/CharacterStep.jsx`
- `frontend/src/utils/playerClasses.js` — three classes, copy, default icons, placeholder image imports
- `frontend/src/assets/classes/` — three placeholder SVGs
- Wizard: Volver keeps step 1 data. Empty name → error, stay on step 2

## Does not land

`services/auth.js`, POST, confetti, real class art, JWT.

## You read

`CharacterStep`, `playerClasses.js`, `SignupPage` (step wiring).

## Test

`cd frontend; npm run dev` → http://localhost:5173. **Do not generate tests.**

- From valid step 1, Next → class cards, Guerrero selected
- Scroll the card: name + icon grid like mockups 2–3
- Placeholders on all three cards; selecting a class keeps the matching placeholder
- Class change updates copy + default emoji unless a custom one was picked
- Volver keeps step 1 data
- Empezar with empty name → error, no network signup
