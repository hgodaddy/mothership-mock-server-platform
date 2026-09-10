# MMSP Sprint 1 — Scenario List

Scenarios are runtime-configurable. Resolution order:

1. `X-MMSP-Scenario` request header  
2. Per-device scenario override  
3. Global active scenario  
4. Default (`success`)

Configure via:

- `PUT /api/v1/scenarios/active`
- `PUT /api/v1/devices/{deviceId}/scenario`
- `config/scenarios.yaml`
- Environment: `DEFAULT_SCENARIO`

---

## Included Scenarios

| Key | HTTP Outcome (typical) | Description | Use Case |
|-----|------------------------|-------------|----------|
| `success` | 2xx | Happy path; device online; payload accepted | Baseline integration tests |
| `timeout` | 504 after delay | Artificial latency then gateway-timeout style response | Client retry / timeout handling |
| `device_offline` | 503 | Device marked offline / unavailable | Offline resilience paths |
| `internal_error` | 500 | Platform fault simulation | Error handling & alerting tests |
| `validation_failure` | 400 | Forces validation-style error body | Negative contract tests |

---

## Scenario Behavior Matrix

| API | success | timeout | device_offline | internal_error | validation_failure |
|-----|---------|---------|----------------|----------------|--------------------|
| Register | 201 | 504 | 503* | 500 | 400 |
| Status Update | 200 | 504 | 503 | 500 | 400 |
| Heartbeat | 200 | 504 | 503 | 500 | 400 |
| Command | 202 | 504 | 503 | 500 | 400 |
| Telemetry | 202 | 504 | 503 | 500 | 400 |

\* For registration, `device_offline` simulates dependency unavailability (service not ready to accept devices).

---

## Example Error Bodies

### validation_failure
```json
{
  "error": "VALIDATION_FAILURE",
  "message": "Request failed mock validation rules for scenario validation_failure",
  "details": [
    {
      "field": "payload",
      "issue": "Simulated invalid field for negative testing"
    }
  ],
  "scenario": "validation_failure",
  "correlationId": "..."
}
```

### device_offline
```json
{
  "error": "DEVICE_OFFLINE",
  "message": "Device is offline or unavailable",
  "scenario": "device_offline",
  "deviceId": "dev_..."
}
```

### timeout
```json
{
  "error": "TIMEOUT",
  "message": "Simulated upstream timeout",
  "scenario": "timeout",
  "latencyMs": 3000
}
```

### internal_error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "Simulated internal platform failure",
  "scenario": "internal_error"
}
```

---

## Post-MVP Scenario Candidates

- `rate_limit` (429)
- `retry_required` (409 / custom)
- `partial_success`
- `auth_failure` (401 / 403)
- `contract_drift` (schema mismatch simulation)
