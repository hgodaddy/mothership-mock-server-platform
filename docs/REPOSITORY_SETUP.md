# MMSP — Repository & shared remote setup

This closes the Sprint 1 gap: the project must exist as a **versioned Git repository** with a **remote**, CODEOWNERS, CI, and (where permissions allow) branch protection.

---

## Current remote (verified 2026-09-10)

| Item | Value |
|------|--------|
| GitHub repo | https://github.com/hgodaddy/mothership-mock-server-platform |
| Visibility | Private |
| Local branch | `main` |
| `origin` | `https://github.com/hgodaddy/mothership-mock-server-platform.git` |
| CODEOWNERS | `@hgodaddy` (personal remote owner) |
| CI workflow | `.github/workflows/ci.yml` — jobs `unit-and-jar-smoke`, `docker-compose-smoke` |
| Branch protection | Enabled on `main` — required status checks `unit-and-jar-smoke` + `docker-compose-smoke` (solo-owner: PR reviews not required; admins may bypass for hotfixes) |

Auth scopes required for bootstrap / workflow push: `repo`, `workflow`.

```bash
gh auth refresh -h github.com -s repo,workflow
```

CI evidence URL: https://github.com/hgodaddy/mothership-mock-server-platform/actions

---

## 1. Local Git

```bash
cd /Users/hsrivastava/Projects/mothership-mock-server-platform
git status
```

Baseline includes the Java POC, Docker (arm64-safe image), Maven Wrapper, CI, Redis wiring, optional API-key auth, and docs.

---

## 2. Create GitHub remote and push

Prerequisites: [GitHub CLI](https://cli.github.com/) authenticated (`gh auth login`) **with `repo` and `workflow` scopes**.

```bash
./scripts/bootstrap-github.sh
```

Defaults:

| Variable | Default |
|----------|---------|
| `REPO_NAME` | `mothership-mock-server-platform` |
| `VISIBILITY` | `private` |

Example public repo:

```bash
VISIBILITY=public ./scripts/bootstrap-github.sh
```

If you already have an empty remote:

```bash
git remote add origin git@github.com:<org>/mothership-mock-server-platform.git
git push -u origin main
```

---

## 3. CODEOWNERS

`.github/CODEOWNERS` assigns `*` to `@hgodaddy` on this personal remote. Update to your GitHub org team when available:

```
*  @your-org/pos-qa-engineering
```

---

## 4. Branch protection (`main`)

Apply via bootstrap script or GitHub **Settings → Branches**. Current `main` rules:

- Require status checks to pass before merge/push (strict):
  - `unit-and-jar-smoke`
  - `docker-compose-smoke`
- Disallow force pushes and branch deletion
- PR review requirement deferred until a multi-reviewer org team exists (solo personal remote)

**Plan note:** classic branch protection is **enabled** on this repository's `main` branch with required CI checks. When the repo moves under an org with reviewers, re-enable required PR reviews in bootstrap.

---

## 5. Prove CI on the remote

After push:

1. Open **Actions** on the GitHub repository.
2. Confirm workflow **MMSP Java CI** ran green for `unit-and-jar-smoke` and `docker-compose-smoke`.
3. Keep the Actions URL as ENV-05 evidence.

Local equivalents:

```bash
./scripts/ci-local.sh
./scripts/setup-docker-local.sh   # Colima + docker CLI if Docker Desktop is absent
./scripts/verify-compose.sh
./scripts/verify-redis.sh
```

---

## 6. Secrets (do not commit)

| Secret | Purpose |
|--------|---------|
| `MMSP_API_KEY` | Optional API key when `MMSP_API_KEY_ENABLED=true` |
| Redis password (future) | Put in compose secrets / K8s secret, not in git |

`.env` is gitignored. Copy from `.env.example`.
