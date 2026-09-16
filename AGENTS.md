# LifeQuest — agent notes

PIN 2026 (UPV) group 12.01. Java Spring Boot backend + JavaScript React frontend.

## Layout

- `backend/` — Java 21, Maven, Spring Boot, port 8080 (`/api/health`, `/api/hello`)
- `frontend/` — Vite + React + npm, port 5173, proxies `/api` to the backend
- `ai/` — dumps for prompts, briefs, notes, CONTEXT.
- How to run: see `RUN.md`

## Git

- Do not commit unless the user says so explicitly.
- If you commit, use the user's git author and committer, not the AI tool.
- Do not add `Co-authored-by` for the AI.
- Commit message format: `PIN-42 type(scope): short summary` — see `ai/git-conventions.md`
