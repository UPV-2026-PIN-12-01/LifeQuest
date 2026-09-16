# Run

JDK 21, Node 20.19+

**Backend** — http://localhost:8080

```bash
cd backend
./mvnw spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

If port 8080 is already in use:
```bash
netstat -ano | findstr ":8080"
taskkill /PID <pid> /F
```

**Frontend** — http://localhost:5173

```bash
cd frontend
npm run dev
```

First time (or after pulling dependency changes): `cd frontend && npm install`
