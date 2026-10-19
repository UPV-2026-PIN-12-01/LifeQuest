# Run

JDK 21, Node 20.19+

**Backend** — http://localhost:8080  
Swagger UI — http://localhost:8080/swagger.html

Copy `.env.example` to `.env` at the repo root (Spring Boot loads it).

```bash
cd backend
./mvnw spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

If port 8080 is already in use (PowerShell):
```powershell
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess -Force
```

**Frontend** — http://localhost:5173

```bash
cd frontend
npm run dev
```

First time (or after pulling dependency changes): `cd frontend && npm install`
