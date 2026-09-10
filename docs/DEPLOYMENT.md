# MMSP Sprint 1 — Deployment Sequential Steps (Java)

This guide deploys the **Java / Spring Boot** MMSP POC locally. Choose **Path A (Maven/JAR)** or **Path B (Docker Compose)**.

---

## Prerequisites

| Tool | Minimum Version | Check |
|------|-----------------|-------|
| JDK | 17+ (Temurin / OpenJDK) | `"$JAVA_HOME/bin/java" -version` |
| Maven | 3.9+ **or Maven Wrapper** | `./mvnw -v` |
| Docker runtime | Docker Desktop **or** Colima | `docker info` |
| Docker Compose | v2 plugin | `docker compose version` |
| curl / python3 | any | used by smoke/demo scripts |

### macOS Homebrew JAVA_HOME tip

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

### macOS without Docker Desktop (Colima)

```bash
./scripts/setup-docker-local.sh
# starts Colima and configures DOCKER_HOST for the docker CLI
```
---

## Path A — Native Java (fastest for development)

### Step 1 — Enter the repository

```bash
cd /Users/hsrivastava/Projects/mothership-mock-server-platform
```

### Step 2 — Set JAVA_HOME and create env file

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
cp .env.example .env
```

### Step 3 — Build

```bash
./scripts/setup-local.sh
# or
./mvnw -DskipTests package
```

### Step 4 — Start the mock server

```bash
./scripts/run-local.sh
# or
./mvnw spring-boot:run
```

Expected log lines include Spring Boot startup and:

```text
MMSP listening on port 8080
Swagger UI: http://localhost:8080/api/docs
```

### Step 5 — Verify health

```bash
curl -s http://localhost:8080/health | python3 -m json.tool
curl -s http://localhost:8080/ready | python3 -m json.tool
```

### Step 6 — Run unit + smoke tests

```bash
./mvnw test
./scripts/smoke-test.sh
```

### Step 7 — Open Swagger UI

Browser: [http://localhost:8080/api/docs](http://localhost:8080/api/docs)

### Step 8 — Stop

`Ctrl+C` in the server terminal.

---

## Path B — Docker Compose

### Step 1 — Enter the repository

```bash
cd /Users/hsrivastava/Projects/mothership-mock-server-platform
```

### Step 2 — Build images

```bash
docker compose build
```

Or run the automated evidence script (build + up + Redis `/ready` check + smoke + tear down):

```bash
./scripts/verify-compose.sh
```

### Step 3 — Start stack

```bash
docker compose up -d
```

| Service | Port | Role |
|---------|------|------|
| `mmsp-api` | `8080` | Spring Boot mock server |
| `redis` | `6379` | Runtime state (`REDIS_ENABLED=true` in Compose) |

### Step 4 — Tail logs

```bash
docker compose logs -f mmsp-api
```

### Step 5 — Verify

```bash
curl -s http://localhost:8080/health | python3 -m json.tool
./scripts/smoke-test.sh
```

### Step 6 — Tear down

```bash
docker compose down
# with volumes:
docker compose down -v
```

---

## Sequential Functional Validation (both paths)

```bash
# 1) Register device
curl -s -X POST http://localhost:8080/api/v1/devices/register \
  -H 'Content-Type: application/json' \
  -d '{
    "deviceSerial": "POS-DEVICE-1001",
    "deviceType": "PAYMENT_TERMINAL",
    "firmwareVersion": "2.4.1",
    "siteId": "STORE-42"
  }' | python3 -m json.tool

# Capture deviceId, then heartbeat / telemetry / scenario switches
bash scripts/demo-flow.sh
```

---

## CI

`.github/workflows/ci.yml` runs two jobs:

1. `./mvnw test package`, start JAR, `scripts/smoke-test.sh`
2. `docker compose up --build` and the same smoke tests against Redis-backed state

Local mirrors:

```bash
./scripts/ci-local.sh
./scripts/verify-redis.sh
./scripts/verify-compose.sh   # requires Docker Desktop
```

Shared GitHub remote: [`docs/REPOSITORY_SETUP.md`](./REPOSITORY_SETUP.md)

---

## Optional API key auth

Disabled by default. To enable locally:

```bash
export MMSP_API_KEY_ENABLED=true
export MMSP_API_KEY=change-me
```

Then send `X-API-Key: change-me` on `/api/v1/*` routes. `/health`, `/ready`, `/metrics`, and Swagger remain public.

---

## Rollback Targets (local POC)

| Level | Local action | Target |
|-------|--------------|--------|
| L1 Configuration | Reset scenario to `success` / restore `scenarios.yaml` | < 5 min |
| L2 Application | Restart process / `docker compose restart mmsp-api` | < 10 min |
| L3 Infrastructure | `docker compose down && docker compose up --build` | < 30 min |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Unable to locate a Java Runtime` | Export `JAVA_HOME` to Homebrew OpenJDK path above |
| Port 8080 in use | `lsof -ti:8080 \| xargs kill` or change `PORT` |
| Maven download failures | Ensure network access; retry `./mvnw -U package` |
| Redis not ready in Compose | Wait for `redis` healthcheck; confirm `/ready` includes `"backend":"redis"` |
| API returns 401 | Auth is enabled; set `MMSP_API_KEY_ENABLED=false` or send `X-API-Key` |
| Smoke timeout case fails | Latency is intentional; ensure client waits |
| Swagger 404 | Use `http://localhost:8080/api/docs` (SpringDoc UI path) |
