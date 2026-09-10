#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> Verifying Docker Compose stack (ENV-03 / ENV-04)"

export PATH="/usr/local/bin:/opt/homebrew/bin:/Applications/Docker.app/Contents/Resources/bin:$PATH"

# Prefer Colima socket when Docker Desktop is not installed
if [[ -z "${DOCKER_HOST:-}" ]]; then
  if [[ -S "${HOME}/.colima/default/docker.sock" ]]; then
    export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
  elif [[ -S "${HOME}/.colima/docker.sock" ]]; then
    export DOCKER_HOST="unix://${HOME}/.colima/docker.sock"
  fi
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is required. Install Docker Desktop or: brew install colima docker docker-compose && colima start"
  echo "       Native Redis proof (no Docker): ./scripts/verify-redis.sh"
  echo "       Remote proof: GitHub Actions job docker-compose-smoke after first push."
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker daemon is not reachable."
  if command -v colima >/dev/null 2>&1; then
    echo "       Try: colima start && export DOCKER_HOST=unix://\${HOME}/.colima/default/docker.sock"
  else
    echo "       Start Docker Desktop, or install Colima (brew install colima docker docker-compose)."
  fi
  exit 1
fi

docker compose down -v --remove-orphans >/dev/null 2>&1 || true
docker compose build
docker compose up -d

cleanup() {
  docker compose down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "==> Waiting for /health"
for i in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:8080/health >/dev/null 2>&1; then
    echo "==> API is healthy"
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "ERROR: API did not become healthy"
    docker compose logs
    exit 1
  fi
  sleep 2
done

READY="$(curl -fsS http://127.0.0.1:8080/ready)"
echo "$READY"
if ! echo "$READY" | grep -q '"backend":"redis"'; then
  echo "ERROR: expected Redis-backed state in Compose /ready response"
  exit 1
fi

bash scripts/smoke-test.sh
echo "==> Docker Compose verification passed"
