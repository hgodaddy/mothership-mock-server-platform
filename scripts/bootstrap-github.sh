#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

REPO_NAME="${REPO_NAME:-mothership-mock-server-platform}"
VISIBILITY="${VISIBILITY:-private}"

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: GitHub CLI (gh) is required to create the remote repository."
  echo "       Install: https://cli.github.com/"
  echo "       Then run: gh auth login"
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "ERROR: gh is not authenticated. Run: gh auth login"
  exit 1
fi

if [[ -z "$(git rev-parse --git-dir 2>/dev/null)" ]]; then
  git init -b main
fi

if ! git rev-parse HEAD >/dev/null 2>&1; then
  echo "ERROR: Create the initial commit before bootstrapping GitHub."
  exit 1
fi

if git remote get-url origin >/dev/null 2>&1; then
  echo "==> origin already exists: $(git remote get-url origin)"
else
  echo "==> Creating GitHub repository $REPO_NAME ($VISIBILITY)"
  gh repo create "$REPO_NAME" --source=. --remote=origin --"$VISIBILITY" --description "Mothership Mock Server Platform (MMSP) Sprint 1 Java POC"
fi

git push -u origin HEAD

echo "==> Applying recommended branch protection on main (best effort)"
OWNER_REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner)"
if ! gh api -X PUT "repos/${OWNER_REPO}/branches/main/protection" \
  -H "Accept: application/vnd.github+json" \
  --input - <<'JSON'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["unit-and-jar-smoke", "docker-compose-smoke"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": null,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
then
  echo "WARN: Could not apply branch protection (org/plan permissions). See docs/REPOSITORY_SETUP.md"
fi

echo "==> Remote bootstrap complete"
echo "    $(git remote get-url origin)"
echo "    CI: https://github.com/${OWNER_REPO}/actions"
