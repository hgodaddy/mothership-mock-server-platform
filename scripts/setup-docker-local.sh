#!/usr/bin/env bash
set -euo pipefail

# Ensures local Docker runtime for MMSP Path B (Colima preferred on macOS without Docker Desktop).

export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"

if ! command -v docker >/dev/null 2>&1; then
  echo "==> Installing docker CLI + compose + colima via Homebrew"
  brew install colima docker docker-compose
fi

mkdir -p "${HOME}/.docker/cli-plugins"
if [[ -f /opt/homebrew/lib/docker/cli-plugins/docker-compose ]]; then
  ln -sfn /opt/homebrew/lib/docker/cli-plugins/docker-compose \
    "${HOME}/.docker/cli-plugins/docker-compose"
fi

python3 - <<'PY'
import json, os
path = os.path.expanduser("~/.docker/config.json")
cfg = {}
if os.path.exists(path):
    with open(path) as fh:
        cfg = json.load(fh)
dirs = cfg.get("cliPluginsExtraDirs", [])
extra = "/opt/homebrew/lib/docker/cli-plugins"
if extra not in dirs:
    dirs.append(extra)
cfg["cliPluginsExtraDirs"] = dirs
os.makedirs(os.path.dirname(path), exist_ok=True)
with open(path, "w") as fh:
    json.dump(cfg, fh, indent=2)
    fh.write("\n")
print("Updated", path)
PY

if ! docker info >/dev/null 2>&1; then
  if ! command -v colima >/dev/null 2>&1; then
    brew install colima
  fi
  echo "==> Starting Colima (Docker runtime)"
  colima start --cpu 2 --memory 4 --disk 40
fi

export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
docker info >/dev/null
docker compose version
echo "==> Docker runtime ready (DOCKER_HOST=$DOCKER_HOST)"
