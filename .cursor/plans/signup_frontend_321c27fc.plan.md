---
name: Signup frontend
overview: "Index of four review slices. Implement F0 first. Spec: ai/signup-frontend-spec.md. No JWT, no /api/me, no generated tests."
todos:
  - id: f0-playername
    content: "F0: optional playerName on POST /api/auth/signup. See signup_f0_playername.plan.md"
    status: pending
  - id: f1-landing-step1
    content: "F1: landing + account step. See signup_f1_landing.plan.md"
    status: pending
  - id: f2-character-step
    content: "F2: character step + placeholders. See signup_f2_character.plan.md"
    status: pending
  - id: f3-api-confetti
    content: "F3: FormData POST + confetti. See signup_f3_api_confetti.plan.md"
    status: pending
isProject: false
---

# Signup frontend (index)

Spec: [ai/signup-frontend-spec.md](../../ai/signup-frontend-spec.md). One plan per slice, same pattern as B1–B3.

1. [F0 playerName](signup_f0_playername.plan.md) — backend, first
2. [F1 landing](signup_f1_landing.plan.md) — mockup + account step
3. [F2 character](signup_f2_character.plan.md) — class cards, placeholders, emoji
4. [F3 POST + confetti](signup_f3_api_confetti.plan.md) — wire API, success popup

Shared across slices (also in the spec): code English / UI Spanish; duplicate validation + keep-in-sync comments; leave `ProfileDto`; skip JWT / `GET /api/me`.

Decisions that live in the slice plans, not the spec: API field stays `playerName`; omit → username; no router; login tab stub; CSS confetti, no extra package.
