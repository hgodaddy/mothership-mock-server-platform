#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
API="$BASE_URL/api/v1"
TMP_DIR="${TMPDIR:-/tmp}"
REG_FILE="$TMP_DIR/mmsp-demo-reg.json"

echo "====================================================="
echo " MMSP Java Leadership Demo Flow"
echo " Base: $BASE_URL"
echo "====================================================="

echo
echo "[1/6] Health"
curl -s "$BASE_URL/health" | python3 -m json.tool

echo
echo "[2/6] Register device"
curl -s -X POST "$API/devices/register" \
  -H 'Content-Type: application/json' \
  -d '{
    "deviceSerial":"DEMO-POS-42",
    "deviceType":"PAYMENT_TERMINAL",
    "firmwareVersion":"2.4.1",
    "siteId":"STORE-42",
    "metadata":{"region":"us-east"}
  }' | tee "$REG_FILE" | python3 -m json.tool

DEVICE_ID="$(python3 -c 'import json; print(json.load(open("'"$REG_FILE"'"))["deviceId"])')"
echo "deviceId=$DEVICE_ID"

echo
echo "[3/6] Heartbeat + telemetry (success)"
curl -s -X POST "$API/devices/$DEVICE_ID/heartbeat" \
  -H 'Content-Type: application/json' \
  -d '{"cpuPercent":18,"memoryPercent":40}' | python3 -m json.tool

curl -s -X POST "$API/devices/$DEVICE_ID/telemetry" \
  -H 'Content-Type: application/json' \
  -d '{"metricType":"TRANSACTION_STATS","values":{"approvedCount":12,"declinedCount":1}}' \
  | python3 -m json.tool

echo
echo "[4/6] Switch global scenario -> internal_error"
curl -s -X PUT "$API/scenarios/active" \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"internal_error"}' | python3 -m json.tool

echo
echo "[5/6] Command under internal_error (expect 500)"
curl -s -X POST "$API/devices/$DEVICE_ID/commands" \
  -H 'Content-Type: application/json' \
  -d '{"commandType":"REBOOT","correlationId":"demo-1"}' | python3 -m json.tool

echo
echo "[6/6] Reset scenario -> success"
curl -s -X PUT "$API/scenarios/active" \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"success"}' | python3 -m json.tool

echo
echo "Swagger UI: $BASE_URL/api/docs"
echo "Metrics:    $BASE_URL/metrics"
echo "Demo complete."
