# Trabajo_PIN_2026_Grupo12.01

Proyecto de la asignatura PIN 2026 (UPV) del grupo 12.01, con nombre provisional de LifeQuest.

Java (Spring Boot) backend + JavaScript (React) frontend.

## Prerequisites

- JDK 21
- Node.js 20.19+ (or 22.12+)

## Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```bash
cd backend
mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`

- `GET /api/health`
- `GET /api/hello`

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173`

Vite proxies `/api` to the backend on port 8080. Saving frontend files updates the page automatically (HMR).

## `ai/`

Empty folder for dumping AI context later (prompts, briefs, notes).
