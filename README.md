# Mothership Mock Server Platform (MMSP) — Java

**Sprint 1 POC** — Centralized API simulation & device emulation for Mission Control integrations.

| Field | Value |
|-------|-------|
| Author | Hemant Srivastava |
| Team | POS QA Engineering |
| Status | Runnable local Java POC (Option 3 architecture slice) |
| Stack | Java 17 · Spring Boot 3.4 · OpenAPI/SpringDoc · Micrometer/Prometheus · Docker |

---

## What this POC delivers (aligned to Sprint 1 tasks)

| Task | Deliverable |
|------|-------------|
| **1. MVP Scope & Acceptance Criteria** | [`docs/MVP_SCOPE.md`](docs/MVP_SCOPE.md) · [`docs/API_LIST.md`](docs/API_LIST.md) · [`docs/SCENARIO_LIST.md`](docs/SCENARIO_LIST.md) · [`docs/ACCEPTANCE_CRITERIA.md`](docs/ACCEPTANCE_CRITERIA.md) |
| **2. Initial Environment & Repository Setup** | Maven Wrapper, `.env.example`, Docker/Compose with Redis state, CI, CODEOWNERS, setup scripts |
| **3. Core Mock Server Framework Skeleton** | Spring Boot app with routing, health, scenarios, 5 MVP APIs, Swagger, metrics, agent stubs |

Full sequential deployment steps: **[`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md)**

---

## Architecture (Sprint 1 slice)

```text
Client / Integration Tests
            │
            ▼
   Spring Boot API Gateway
            │
   ┌────────┼────────┐
   ▼        ▼        ▼
Mock APIs  Scenario  Device Sim
           Manager
   └────────┬────────┘
            ▼
    State + Response Engines
            │
   Logging / Prometheus Metrics
            │
 Stability Agent stub · Enhancement Agent stub
```

---

## Quick start (local — recommended)

```bash
cd /Users/hsrivastava/Projects/mothership-mock-server-platform

# Ensure JAVA_HOME points at a JDK (Homebrew OpenJDK example)
export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home

./scripts/setup-local.sh
./scripts/run-local.sh
```

In another terminal:

```bash
./scripts/smoke-test.sh
./scripts/demo-flow.sh
```

Or with Maven directly:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
./mvnw spring-boot:run
```

| Endpoint | URL |
|----------|-----|
| Health | http://localhost:8080/health |
| Ready | http://localhost:8080/ready |
| Swagger | http://localhost:8080/api/docs |
| Metrics | http://localhost:8080/metrics |
| API prefix | http://localhost:8080/api/v1 |

---

## Docker Compose

```bash
./scripts/verify-compose.sh
# or
docker compose up --build -d
curl -s http://localhost:8080/ready
./scripts/smoke-test.sh
docker compose down
```

Compose uses **Redis** for device/scenario state. Native `./scripts/run-local.sh` uses **in-memory** state unless `REDIS_ENABLED=true`.

---

## MVP APIs

| API | Method | Path |
|-----|--------|------|
| Device Registration | `POST` | `/api/v1/devices/register` |
| Device Status Update | `PUT` | `/api/v1/devices/{deviceId}/status` |
| Heartbeat | `POST` | `/api/v1/devices/{deviceId}/heartbeat` |
| Command Execution | `POST` | `/api/v1/devices/{deviceId}/commands` |
| Device Telemetry | `POST` | `/api/v1/devices/{deviceId}/telemetry` |

## MVP Scenarios

`success` · `timeout` · `device_offline` · `internal_error` · `validation_failure`

**Runtime control**

```bash
# Global
curl -X PUT http://localhost:8080/api/v1/scenarios/active \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"timeout","latencyMs":1500}'

# Per request
curl -H 'X-MMSP-Scenario: internal_error' ...

# Optional API key (when MMSP_API_KEY_ENABLED=true)
curl -H 'X-API-Key: change-me' ...
```

---

## Project layout

```text
.
├── mvnw / mvnw.cmd              # Maven Wrapper
├── .mvn/wrapper/
├── config/                      # reference YAML (also packaged under resources)
├── docs/                        # MVP scope, APIs, scenarios, acceptance, deployment
├── openapi/                     # OpenAPI reference specification
├── scripts/                     # setup, run, smoke, demo
├── src/main/java/com/mmsp/
│   ├── agents/                  # Stability + Enhancement stubs
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   ├── filter/
│   ├── model/
│   ├── service/                 # state, scenario, device sim
│   └── MmspApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── config/scenarios.yaml
├── src/test/java/
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## Acceptance

See the checklist in [`docs/ACCEPTANCE_CRITERIA.md`](docs/ACCEPTANCE_CRITERIA.md).  
Minimum gate for this POC: `./scripts/run-local.sh` + `./scripts/smoke-test.sh` exits `0`.

---

## Next (post Sprint 1)

- Expand Stability Agent (health anomaly → recovery actions)
- Expand Enhancement Agent (contract watch + scenario proposals)
- Kubernetes manifests + Grafana dashboards
- Configuration portal / dashboard
