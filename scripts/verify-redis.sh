#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> Verifying Redis-backed native run (REDIS_ENABLED=true)"

if [[ -z "${JAVA_HOME:-}" ]]; then
  if [[ -d /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
  fi
fi
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"

if ! command -v redis-server >/dev/null 2>&1; then
  echo "ERROR: redis-server not found. Install with: brew install redis"
  exit 1
fi

lsof -ti:8080 | xargs kill -9 2>/dev/null || true

REDIS_STARTED=0
if ! redis-cli ping >/dev/null 2>&1; then
  redis-server --daemonize yes --port 6379 --save "" --appendonly no
  REDIS_STARTED=1
  sleep 1
fi

if ! redis-cli ping | grep -q PONG; then
  echo "ERROR: Redis did not respond to PING"
  exit 1
fi

JAR="target/mothership-mock-server-platform-0.1.0.jar"
if [[ ! -f "$JAR" ]]; then
  ./mvnw -q -DskipTests package
fi

export REDIS_ENABLED=true
export REDIS_URL=redis://localhost:6379
export PORT=8080

java -jar "$JAR" &
PID=$!
cleanup() {
  kill "$PID" >/dev/null 2>&1 || true
  if [[ "$REDIS_STARTED" -eq 1 ]]; then
    redis-cli shutdown nosave >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

for i in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:8080/health >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "ERROR: MMSP failed to start with Redis"
    exit 1
  fi
  sleep 1
done

READY="$(curl -fsS http://127.0.0.1:8080/ready)"
echo "$READY"
if ! echo "$READY" | grep -q '"backend":"redis"'; then
  echo "ERROR: expected Redis-backed state in /ready"
  exit 1
fi

bash scripts/smoke-test.sh
echo "==> Redis-backed native verification passed"
