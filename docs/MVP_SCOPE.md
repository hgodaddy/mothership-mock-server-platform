# MMSP Sprint 1 — MVP Scope & Acceptance Criteria

**Project:** Mothership Mock Server Platform (MMSP)  
**Author:** Hemant Srivastava  
**Team:** POS QA Engineering  
**Status:** Sprint 1 POC — Executable Baseline (**Java / Spring Boot**)  
**Aligned Proposal:** Leadership Architecture & MVP Approval Review (Option 3)

---

## 1. Purpose

Define the Sprint 1 MVP boundary so Engineering and QA can validate Mission Control integrations without physical devices or unstable backend dependencies.

This document is the source of truth for:

| Deliverable | Location |
|-------------|----------|
| API List | [API_LIST.md](./API_LIST.md) |
| Scenario List | [SCENARIO_LIST.md](./SCENARIO_LIST.md) |
| Success / Acceptance Criteria | [ACCEPTANCE_CRITERIA.md](./ACCEPTANCE_CRITERIA.md) |
| Local & Docker Deployment | [DEPLOYMENT.md](./DEPLOYMENT.md) |

---

## 2. In Scope (Sprint 1)

### 2.1 Included APIs

| API | Method | Path | Purpose |
|-----|--------|------|---------|
| Device Registration | `POST` | `/api/v1/devices/register` | Register a simulated device |
| Device Status Update | `PUT` | `/api/v1/devices/{deviceId}/status` | Update device operational state |
| Heartbeat | `POST` | `/api/v1/devices/{deviceId}/heartbeat` | Keepalive / liveness signal |
| Command Execution | `POST` | `/api/v1/devices/{deviceId}/commands` | Accept and simulate command handling |
| Device Telemetry | `POST` | `/api/v1/devices/{deviceId}/telemetry` | Ingest telemetry payload |

### 2.2 Included Scenarios

| Scenario Key | Behavior |
|--------------|----------|
| `success` | Happy-path 2xx responses |
| `timeout` | Artificial delay then timeout-style failure |
| `device_offline` | Device unavailable (e.g. 503 / offline state) |
| `internal_error` | Simulated 500 platform fault |
| `validation_failure` | Request schema / field validation errors (400) |

### 2.3 Included Platform Features

- Runtime scenario configuration (global + per-device)
- In-memory device state engine, with Redis-backed state when `REDIS_ENABLED=true` (Compose default)
- Swagger / OpenAPI UI
- Request & response logging (Winston + Morgan)
- Prometheus metrics endpoint
- Health / readiness probes
- Docker & Docker Compose local deployment
- Stability Agent & Enhancement Agent **stubs** (signal hooks only in Sprint 1)

### 2.4 Explicitly Out of Scope (Post–Sprint 1)

- Full Kubernetes production charts / multi-cluster HA
- PostgreSQL persistence (optional later)
- Self-service Configuration Portal UI
- AI-assisted scenario generation (Enhancement Agent full implementation)
- Automated K8s rollback orchestration (Stability Agent full implementation)
- WebSockets (REST-only for MVP unless leadership revises)
- Microsoft Teams / PagerDuty alert routing (stub logging only)

---

## 3. Architecture Slice (Sprint 1)

```
Client / Mission Control Integration Tests
                 |
                 v
        ┌─────────────────┐
        │ Spring Boot App │  ← API Gateway (lightweight)
        │  /api/v1/*      │
        └────────┬────────┘
                 |
    ┌────────────┼────────────┐
    v            v            v
 Mock APIs   Scenario Mgr   Device Sim
    |            |            |
    +------+-----+------+-----+
           v            v
     State Engine   Response Engine
           |            |
           +------+-----+
                  v
         Logging / Metrics
                  |
         Stability / Enhancement
              Agent Stubs
```

---

## 4. Decision Defaults (Open Questions — POC Answers)

| Open Question | Sprint 1 Default |
|---------------|------------------|
| Mandatory Mission Control APIs? | Five APIs listed above |
| REST-only sufficient? | **Yes** for MVP |
| WebSockets in MVP? | **No** |
| Redis vs PostgreSQL? | In-memory for native Path A; **Redis wired** in Docker Compose |
| Dedicated vs shared QA env? | Local / Docker Compose dedicated POC |
| Grafana mandatory in MVP? | **No** — Prometheus scrape endpoint only |
| Auth / secrets? | Optional `X-API-Key` (`MMSP_API_KEY_ENABLED`, default **off**) |

---

## 5. Sprint 1 Task Mapping

| Task | Outcome in this repo |
|------|----------------------|
| 1. Define MVP Scope & Acceptance Criteria | `docs/MVP_SCOPE.md`, `API_LIST.md`, `SCENARIO_LIST.md`, `ACCEPTANCE_CRITERIA.md` |
| 2. Initial Environment & Repository Setup | Maven Wrapper, `.env.example`, Docker, Compose (Redis wired), CI, CODEOWNERS, GitHub bootstrap |
| 3. Core Mock Server Framework Skeleton | Runnable Spring Boot app with routing, health, scenarios, MVP APIs |
