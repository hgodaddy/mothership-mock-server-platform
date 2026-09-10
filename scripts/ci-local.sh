#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> Local CI mirror (unit + JAR smoke)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -B test package

JAR="target/mothership-mock-server-platform-0.1.0.jar"
java -jar "$JAR" &
PID=$!
trap 'kill "$PID" >/dev/null 2>&1 || true' EXIT

for i in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:8080/health >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "ERROR: MMSP failed to start for local CI"
    exit 1
  fi
  sleep 1
done

bash scripts/smoke-test.sh
echo "==> Local CI mirror passed"
