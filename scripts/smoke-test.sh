#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
API="$BASE_URL/api/v1"

pass=0
fail=0

assert_code() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  if [[ "$actual" == "$expected" ]]; then
    echo "PASS  $name (HTTP $actual)"
    pass=$((pass + 1))
  else
    echo "FAIL  $name (expected $expected, got $actual)"
    fail=$((fail + 1))
  fi
}

echo "==> MMSP Java smoke tests against $BASE_URL"

code="$(curl -s -o /tmp/mmsp-health.json -w "%{http_code}" "$BASE_URL/health")"
assert_code "GET /health" "200" "$code"

code="$(curl -s -o /tmp/mmsp-ready.json -w "%{http_code}" "$BASE_URL/ready")"
assert_code "GET /ready" "200" "$code"

code="$(curl -s -o /tmp/mmsp-metrics.txt -w "%{http_code}" "$BASE_URL/metrics")"
assert_code "GET /metrics" "200" "$code"

curl -s -X PUT "$API/scenarios/active" \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"success","latencyMs":20}' >/dev/null

code="$(curl -s -o /tmp/mmsp-reg.json -w "%{http_code}" \
  -X POST "$API/devices/register" \
  -H 'Content-Type: application/json' \
  -d '{"deviceSerial":"SMOKE-001","deviceType":"PAYMENT_TERMINAL","firmwareVersion":"2.4.1","siteId":"STORE-1"}')"
assert_code "POST /devices/register" "201" "$code"

DEVICE_ID="$(python3 - <<'PY'
import json
print(json.load(open("/tmp/mmsp-reg.json")).get("deviceId",""))
PY
)"

if [[ -z "$DEVICE_ID" ]]; then
  echo "FAIL  unable to parse deviceId from registration response"
  fail=$((fail + 1))
else
  echo "INFO  deviceId=$DEVICE_ID"

  code="$(curl -s -o /tmp/mmsp-hb.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/heartbeat" \
    -H 'Content-Type: application/json' \
    -d '{"cpuPercent":11,"memoryPercent":22}')"
  assert_code "POST /devices/{id}/heartbeat" "200" "$code"

  code="$(curl -s -o /tmp/mmsp-status.json -w "%{http_code}" \
    -X PUT "$API/devices/$DEVICE_ID/status" \
    -H 'Content-Type: application/json' \
    -d '{"state":"online","reason":"smoke"}')"
  assert_code "PUT /devices/{id}/status" "200" "$code"

  code="$(curl -s -o /tmp/mmsp-cmd.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/commands" \
    -H 'Content-Type: application/json' \
    -d '{"commandType":"REBOOT","correlationId":"smoke-cmd-1","payload":{"force":false}}')"
  assert_code "POST /devices/{id}/commands" "202" "$code"

  code="$(curl -s -o /tmp/mmsp-tel.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/telemetry" \
    -H 'Content-Type: application/json' \
    -d '{"metricType":"TRANSACTION_STATS","values":{"approvedCount":3}}')"
  assert_code "POST /devices/{id}/telemetry" "202" "$code"

  code="$(curl -s -o /tmp/mmsp-err.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/heartbeat" \
    -H 'Content-Type: application/json' \
    -H 'X-MMSP-Scenario: internal_error' \
    -d '{}')"
  assert_code "header scenario internal_error" "500" "$code"

  code="$(curl -s -o /tmp/mmsp-val.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/telemetry" \
    -H 'Content-Type: application/json' \
    -H 'X-MMSP-Scenario: validation_failure' \
    -d '{"metricType":"X"}')"
  assert_code "header scenario validation_failure" "400" "$code"

  curl -s -X PUT "$API/scenarios/active" \
    -H 'Content-Type: application/json' \
    -d '{"scenario":"timeout","latencyMs":200}' >/dev/null

  code="$(curl -s -o /tmp/mmsp-to.json -w "%{http_code}" \
    -X POST "$API/devices/$DEVICE_ID/heartbeat" \
    -H 'Content-Type: application/json' \
    -d '{}')"
  assert_code "global scenario timeout" "504" "$code"

  curl -s -X PUT "$API/scenarios/active" \
    -H 'Content-Type: application/json' \
    -d '{"scenario":"success","latencyMs":20}' >/dev/null
fi

echo "==> Results: $pass passed, $fail failed"
if [[ "$fail" -gt 0 ]]; then
  exit 1
fi
exit 0
