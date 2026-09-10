#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${JAVA_HOME:-}" ]]; then
  if [[ -d /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
  fi
fi

export PORT="${PORT:-8080}"
mkdir -p logs

JAR="target/mothership-mock-server-platform-0.1.0.jar"
if [[ ! -f "$JAR" ]]; then
  echo "==> JAR missing; building..."
  ./mvnw -q -DskipTests package
fi

echo "==> Starting MMSP on port $PORT"
exec "$JAVA_HOME/bin/java" ${JAVA_OPTS:-} -jar "$JAR"
