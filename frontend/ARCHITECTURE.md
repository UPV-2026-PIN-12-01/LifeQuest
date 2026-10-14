# Frontend architecture

Layered React (JavaScript) app built with Vite (`frontend/src`). Playwright e2e lives next to `src`, in `frontend/test`.

## Folders

- `main.jsx` — Vite/React entry; mounts `App`.
- `App.jsx` — Root shell: layout and routing, not page logic.
- `pages/` — One screen per route (`SignupPage`, later Home, etc.).
- `components/` — Reusable UI used by pages; not a whole route.
- `services/` — HTTP to `/api`; no React, no JSX.
- `hooks/` — Shared React hooks (auth, forms) used by more than one page.
- `styles/` — Global CSS (`index.css`) and shared layout styles.
- `routes/` — Route table / path constants (when you add a router).
- `assets/` — Images, fonts, static files imported by the app.
- `utils/` — Pure helpers and constants; no React, no fetch.
- `test/` — Playwright e2e against a running Vite + backend (`npm run test:e2e`). Not app source.

## Flow



## Rules



## Details
