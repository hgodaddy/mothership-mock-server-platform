# Contributing to MMSP

## Local development

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
./scripts/setup-local.sh
./scripts/run-local.sh
```

Use the Maven Wrapper (`./mvnw`) rather than a globally installed Maven version when possible.

## Branching

| Branch | Purpose |
|--------|---------|
| `main` | Stable Sprint baseline |
| `feature/<ticket>-short-name` | Feature work |
| `fix/<ticket>-short-name` | Bug fixes |

Open pull requests into `main`. CODEOWNERS will request review from the default owners.

## Checks before opening a PR

1. `./mvnw test`
2. `./scripts/smoke-test.sh` against a running instance
3. If you changed Docker files, `./scripts/setup-docker-local.sh && ./scripts/verify-compose.sh`

## Repository bootstrap (first time on GitHub)

See [`docs/REPOSITORY_SETUP.md`](docs/REPOSITORY_SETUP.md) and `./scripts/bootstrap-github.sh`.

## Secrets

Do not commit `.env` or real API keys. Use `.env.example` as the template. Optional auth is `MMSP_API_KEY_ENABLED` + `MMSP_API_KEY`.
