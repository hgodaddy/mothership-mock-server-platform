# MMSP Sprint 1 — Success / Acceptance Criteria

## Definition of Done (Sprint 1)

The Sprint 1 POC is accepted when all criteria below pass on a local machine (native Java/Maven **or** Docker Compose).

---

## A. Scope Documentation

| ID | Criterion | Evidence | Status |
|----|-----------|----------|--------|
| DOC-01 | API List published for 5 MVP APIs | `docs/API_LIST.md` | Done |
| DOC-02 | Scenario List published for 5 MVP scenarios | `docs/SCENARIO_LIST.md` | Done |
| DOC-03 | Success criteria documented and reviewable | This file | Done |
| DOC-04 | Deployment sequential steps documented | `docs/DEPLOYMENT.md` | Done |

---

## B. Environment & Repository Setup

| ID | Criterion | Evidence | Status (2026-09-10) |
|----|-----------|----------|---------------------|
| ENV-01 | Repository builds with `./mvnw -DskipTests package` | Maven Wrapper + clean build | **PASS** |
| ENV-02 | `.env.example` documents required variables | Present at repo root | **PASS** |
| ENV-03 | Docker image builds successfully | `./scripts/verify-compose.sh` (Colima) | **PASS** |
| ENV-04 | Compose stack starts mock API with Redis state | `/ready` → `"backend":"redis"` + smoke | **PASS** |
| ENV-05 | CI workflow exists and is runnable | `.github/workflows/ci.yml` + Actions run | **PASS** (see Actions URL after push) |
| ENV-06 | Shared git remote documented / bootstrapped | `origin` + `docs/REPOSITORY_SETUP.md` | **PASS** |
| ENV-07 | Optional API key auth implemented (off by default) | `MMSP_API_KEY_ENABLED` / `X-API-Key` | **PASS** |

Local proof commands used for ENV-03/04:

```bash
./scripts/setup-docker-local.sh
./scripts/verify-compose.sh
./scripts/verify-redis.sh
```

Branch protection on `main` is enabled (required PR + `unit-and-jar-smoke` + `docker-compose-smoke`, admins enforced).

---

## C. Core Mock Server Framework

| ID | Criterion | Pass Condition |
|----|-----------|----------------|
| CORE-01 | Server starts and binds configured port | Default `8080` |
| CORE-02 | `GET /health` returns `status: ok` | HTTP 200 |
| CORE-03 | `GET /ready` returns ready | HTTP 200 |
| CORE-04 | Swagger UI available | `GET /api/docs` loads |
| CORE-05 | Device registration works under `success` | HTTP 201 + `deviceId` |
| CORE-06 | Status / heartbeat / command / telemetry work | Expected 2xx codes |
| CORE-07 | Global scenario switch changes behavior | `timeout` → HTTP 504 |
| CORE-08 | Per-device scenario override honored | Device-level `device_offline` → 503 |
| CORE-09 | Header override `X-MMSP-Scenario` honored | Overrides global/device |
| CORE-10 | Request / application logging present | SLF4J / file log under `logs/` |
| CORE-11 | Metrics endpoint scrapable | `GET /metrics` Prometheus text |
| CORE-12 | Smoke script exits 0 | `./scripts/smoke-test.sh` |

---

## D. Operational Resilience (Baseline)

| ID | Criterion | Pass Condition |
|----|-----------|----------------|
| OPS-01 | Process restart restores service | Kill + restart → `/health` ok |
| OPS-02 | Invalid JSON returns 400 (not crash) | Process remains up |
| OPS-03 | Unknown device returns 404 | Structured JSON error |
| OPS-04 | Agent stubs emit structured signals | Logged Stability / Enhancement hooks |

---

## E. Demo Script (Leadership / Peer Walkthrough)

1. Start platform (`./scripts/setup-local.sh` then `./scripts/run-local.sh`, or `docker compose up --build`).
2. Open Swagger: `http://localhost:8080/api/docs`.
3. Register a device (success).
4. Send heartbeat + telemetry.
5. Switch global scenario to `internal_error` and re-call an API.
6. Reset to `success`.
7. Show `/metrics` and log output.

**Pass:** Walkthrough completes without unplanned restarts; scenario switching is visible within one request.

---

## Sign-off Checklist

- [x] DOC-01 … DOC-04
- [x] ENV-01 … ENV-07
- [x] CORE-01 … CORE-12 (covered by smoke / local runs)
- [x] OPS-01 … OPS-04 (baseline stubs + error handling exercised by smoke)
- [x] Demo script available (`./scripts/demo-flow.sh`)

**Approver (Architecture):** pending human sign-off  
**Approver (QA Engineering):** pending human sign-off  
**Date:** 2026-09-10 (ENV gaps closed; human approvers still required)
