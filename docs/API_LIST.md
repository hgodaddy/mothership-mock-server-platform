# MMSP Sprint 1 — API List

Base URL (local): `http://localhost:8080`  
API Prefix: `/api/v1`  
Content-Type: `application/json`

Runtime scenario override header (optional): `X-MMSP-Scenario: <scenario_key>`  
Optional auth header (off by default): `X-API-Key` when `MMSP_API_KEY_ENABLED=true`

---

## 1. Health & Platform

### `GET /health`

Liveness probe.

**Success (200)**
```json
{
  "status": "ok",
  "service": "MMSP",
  "version": "0.1.0",
  "uptimeSeconds": 12.4
}
```

### `GET /ready`

Readiness probe (scenario manager + state store).

### `GET /metrics`

Prometheus metrics scrape endpoint.

### `GET /api/docs`

Swagger UI.

---

## 2. Device Registration

### `POST /api/v1/devices/register`

Registers a new simulated device.

**Request**
```json
{
  "deviceSerial": "POS-DEVICE-1001",
  "deviceType": "PAYMENT_TERMINAL",
  "firmwareVersion": "2.4.1",
  "siteId": "STORE-42",
  "metadata": {
    "region": "us-east"
  }
}
```

**Success (201)**
```json
{
  "deviceId": "dev_8f3c2a91",
  "deviceSerial": "POS-DEVICE-1001",
  "status": "registered",
  "state": "online",
  "registeredAt": "2026-08-10T09:00:00.000Z",
  "scenario": "success"
}
```

**Validation Failure (400)** — missing `deviceSerial` or `deviceType`.

---

## 3. Device Status Update

### `PUT /api/v1/devices/{deviceId}/status`

**Request**
```json
{
  "state": "maintenance",
  "reason": "scheduled_update"
}
```

Allowed `state` values: `online` | `offline` | `busy` | `error` | `maintenance`

**Success (200)**
```json
{
  "deviceId": "dev_8f3c2a91",
  "previousState": "online",
  "state": "maintenance",
  "updatedAt": "2026-08-10T09:05:00.000Z"
}
```

---

## 4. Heartbeat

### `POST /api/v1/devices/{deviceId}/heartbeat`

**Request**
```json
{
  "timestamp": "2026-08-10T09:06:00.000Z",
  "cpuPercent": 22,
  "memoryPercent": 41
}
```

**Success (200)**
```json
{
  "deviceId": "dev_8f3c2a91",
  "ack": true,
  "serverTime": "2026-08-10T09:06:00.120Z",
  "state": "online"
}
```

---

## 5. Command Execution

### `POST /api/v1/devices/{deviceId}/commands`

**Request**
```json
{
  "commandType": "REBOOT",
  "correlationId": "cmd-corr-001",
  "payload": {
    "force": false
  }
}
```

**Success (202)**
```json
{
  "commandId": "cmd_a1b2c3",
  "deviceId": "dev_8f3c2a91",
  "commandType": "REBOOT",
  "status": "accepted",
  "correlationId": "cmd-corr-001",
  "acceptedAt": "2026-08-10T09:07:00.000Z"
}
```

---

## 6. Device Telemetry

### `POST /api/v1/devices/{deviceId}/telemetry`

**Request**
```json
{
  "metricType": "TRANSACTION_STATS",
  "recordedAt": "2026-08-10T09:08:00.000Z",
  "values": {
    "approvedCount": 12,
    "declinedCount": 1,
    "avgLatencyMs": 180
  }
}
```

**Success (202)**
```json
{
  "telemetryId": "tel_9d8e7f",
  "deviceId": "dev_8f3c2a91",
  "accepted": true,
  "receivedAt": "2026-08-10T09:08:00.050Z"
}
```

---

## 7. Scenario Management (Control Plane — Sprint 1)

### `GET /api/v1/scenarios`

List available scenarios and current active global scenario.

### `PUT /api/v1/scenarios/active`

Set global active scenario.

**Request**
```json
{
  "scenario": "timeout",
  "latencyMs": 3000
}
```

### `PUT /api/v1/devices/{deviceId}/scenario`

Set per-device scenario override.

### `GET /api/v1/devices`

List registered simulated devices and their states.

### `GET /api/v1/devices/{deviceId}`

Fetch a single device record.
