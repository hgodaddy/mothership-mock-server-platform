#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> MMSP Java local setup"
echo "==> Working directory: $ROOT_DIR"

if [[ -z "${JAVA_HOME:-}" ]]; then
  if [[ -d /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
  fi
fi

if [[ -z "${JAVA_HOME:-}" || ! -x "${JAVA_HOME}/bin/java" ]]; then
  echo "ERROR: JAVA_HOME is not set to a valid JDK."
  echo "       Example: export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
  exit 1
fi

MVN="./mvnw"
if [[ ! -x "$MVN" ]]; then
  chmod +x "$MVN" 2>/dev/null || true
fi
if [[ ! -f "$MVN" ]]; then
  echo "ERROR: Maven Wrapper (./mvnw) is missing."
  exit 1
fi

echo "==> JAVA_HOME=$JAVA_HOME"
echo "==> Java: $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"
echo "==> Maven Wrapper: $(./mvnw -v | head -1)"

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "==> Created .env from .env.example"
fi

mkdir -p logs
echo "==> Building project (skip tests for setup speed; run ./mvnw test separately)"
./mvnw -q -DskipTests package

echo "==> Setup complete"
echo "    Start server:  ./scripts/run-local.sh"
echo "    Or:            ./mvnw spring-boot:run"
echo "    Tests:         ./mvnw test"
echo "    Smoke tests:   ./scripts/smoke-test.sh"
echo "    Compose verify: ./scripts/verify-compose.sh"
echo "    Swagger UI:    http://localhost:8080/api/docs"
echo "    Docker path:   docker compose up --build"
