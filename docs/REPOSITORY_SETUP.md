# MMSP — Repository & shared remote setup

This closes the Sprint 1 gap: the project must exist as a **versioned Git repository** with a **remote**, CODEOWNERS, CI, and (where permissions allow) branch protection.

---

## 1. Local Git (already initialized)

```bash
cd /Users/hsrivastava/Projects/mothership-mock-server-platform
git status
```

The first baseline commit should include the Java POC, Docker, Maven Wrapper, CI, and docs.

---

## 2. Create GitHub remote and push

Prerequisites: [GitHub CLI](https://cli.github.com/) authenticated (`gh auth login`).

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

`.github/CODEOWNERS` currently assigns `*` to `@hsrivastava`. Update this to your GitHub org team, for example:

```
*  @your-org/pos-qa-engineering
```

---

## 4. Recommended branch protection (`main`)

Apply in GitHub **Settings → Branches**, or via the bootstrap script. Recommended rules:

- Require pull request reviews (1)
- Require status checks to pass:
  - `unit-and-jar-smoke`
  - `docker-compose-smoke`
- Do not allow bypassing for admins (optional but preferred)

If the API call fails (personal plan / missing admin scope), apply the same rules in the GitHub UI.

---

## 5. Prove CI on the remote

After the first push:

1. Open **Actions** on the GitHub repository.
2. Confirm workflow **MMSP Java CI** ran for `unit-and-jar-smoke` and `docker-compose-smoke`.
3. Keep the Actions URL as ENV-05 evidence.

Local equivalent (no GitHub required):

```bash
./scripts/ci-local.sh
./scripts/verify-compose.sh
```

---

## 6. Secrets (do not commit)

| Secret | Purpose |
|--------|---------|
| `MMSP_API_KEY` | Optional API key when `MMSP_API_KEY_ENABLED=true` |
| Redis password (future) | Put in compose secrets / K8s secret, not in git |

`.env` is gitignored. Copy from `.env.example`.
