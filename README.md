AI Resume Analyzer - Quick Run

Quick demo (no external DB) — H2 in-memory profile

1. Start the application using the H2 profile:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

2. Open http://localhost:8080/ in your browser.

Run with Docker Compose (app + MySQL):

```powershell
docker-compose up --build
```

Notes:
- When using Docker, DB credentials in `docker-compose.yml` are `root` / `rootpassword` for demo only.
- To stop Docker services: `docker-compose down`

If the app doesn't start, run `mvn spring-boot:run -Dspring-boot.run.profiles=h2` and paste the last 200 lines of terminal output here; I'll diagnose and fix failures.
