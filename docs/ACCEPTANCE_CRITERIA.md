# MMSP Sprint 1 — Success / Acceptance Criteria

## Definition of Done (Sprint 1)

The Sprint 1 POC is accepted when all criteria below pass on a local machine (native Java/Maven **or** Docker Compose).

---

## A. Scope Documentation

| ID | Criterion | Evidence |
|----|-----------|----------|
| DOC-01 | API List published for 5 MVP APIs | `docs/API_LIST.md` |
| DOC-02 | Scenario List published for 5 MVP scenarios | `docs/SCENARIO_LIST.md` |
| DOC-03 | Success criteria documented and reviewable | This file |
| DOC-04 | Deployment sequential steps documented | `docs/DEPLOYMENT.md` |

---

## B. Environment & Repository Setup

| ID | Criterion | Evidence |
|----|-----------|----------|
| ENV-01 | Repository builds with `./mvnw -DskipTests package` | Maven Wrapper + clean build |
| ENV-02 | `.env.example` documents required variables | Present at repo root |
| ENV-03 | Docker image builds successfully | `docker compose build` / `./scripts/verify-compose.sh` |
| ENV-04 | Compose stack starts mock API with Redis state | `/health` 200 and `/ready` `backend: redis` |
| ENV-05 | CI workflow exists and is runnable | `.github/workflows/ci.yml` + `./scripts/ci-local.sh` |
| ENV-06 | Shared git remote documented / bootstrapped | `docs/REPOSITORY_SETUP.md`, `./scripts/bootstrap-github.sh` |
| ENV-07 | Optional API key auth implemented (off by default) | `MMSP_API_KEY_ENABLED` / `X-API-Key` |

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

- [ ] DOC-01 … DOC-04
- [ ] ENV-01 … ENV-07
- [ ] CORE-01 … CORE-12
- [ ] OPS-01 … OPS-04
- [ ] Demo script completed

**Approver (Architecture):** _______________________  
**Approver (QA Engineering):** _______________________  
**Date:** _______________
