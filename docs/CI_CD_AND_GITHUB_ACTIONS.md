# CI/CD and GitHub Actions — classroom walkthrough

This doc matches what we demo on GitHub: **why automation exists**, what **GitHub Actions** does in this repo, and the **full cycle** from branch to merge.

---

## CI vs CD (quick definitions)

| Term | Meaning | Analogy |
|------|---------|--------|
| **CI — Continuous Integration** | Every time someone pushes code or opens a PR, machines **automatically build and test** so breaks are caught early. | Spell-check + grammar run on every essay draft before the teacher reads the final version. |
| **CD — Continuous Delivery / Deployment** | After CI passes, **automatically ship** to staging or production (or “one click” to release). | Approved essays go straight to the school magazine printer. |

This lab repo focuses on **CI** (tests run on GitHub). **CD** is optional and depends on where you deploy (not configured here).

---

## Why CI is useful (especially for QA-minded teams)

1. **Fast feedback** — You learn within minutes if `main` would break, instead of discovering it on demo day.
2. **Repeatable checks** — Same steps every time on a clean runner (`ubuntu-latest`), fewer “works on my laptop” surprises. Mirror locally with **`mvn verify`**.
3. **Protect the trunk** — Pull requests can **require** green checks before merge (branch protection). Students practice the same discipline as industry repos.
4. **Living documentation** — The workflow file [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) *is* the agreed definition of “good enough to merge.”

---

## What GitHub Actions is

**GitHub Actions** runs automated **workflows** when **events** happen on GitHub (push, pull request, schedule, etc.).

- **Workflow** — YAML file under `.github/workflows/` (we use `ci.yml`).
- **Job** — A group of steps that run on a **runner** (e.g. `ubuntu-latest`).
- **Step** — Checkout code, install JDKs (**17** and **21** in CI), run `mvn -B verify`, then Spectral on the exported OpenAPI file.

When you open a PR, GitHub shows **Checks**: green if every step passed, red if something failed (with logs).

---

## Workflow files (quick map)

| File | When it runs | Purpose |
|------|----------------|---------|
| [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) | Push / PR to `main` or `master` | JDK **17** and **21** matrix; `mvn verify`; Spectral on `target/openapi.json`; uploads SBOM + JaCoCo HTML |
| [`.github/workflows/dependency-review.yml`](../.github/workflows/dependency-review.yml) | Pull requests | **Optional.** Dependency Review — runs only if repo variable `ENABLE_DEPENDENCY_REVIEW` is `true` *and* [Dependency graph](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-the-dependency-graph) is enabled (see comments in the workflow file). |
| [`.github/workflows/secrets-scan.yml`](../.github/workflows/secrets-scan.yml) | Push / PR | TruffleHog scan for leaked secrets in changed commits |
| [`.github/workflows/semantic-pr.yml`](../.github/workflows/semantic-pr.yml) | Pull requests | Validates PR title against Conventional Commits-style prefixes |
| [`.github/workflows/nightly-pitest.yml`](../.github/workflows/nightly-pitest.yml) | Daily schedule + manual | [PIT](https://pitest.org/) mutation tests (`-Ppitest`); uploads HTML report |
| [`.github/dependabot.yml`](../.github/dependabot.yml) | Weekly | Opens PRs for Maven + GitHub Actions dependency bumps |

**Note:** [Dependency review](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-dependency-review) needs **Dependency graph** enabled under repo **Settings → Code security**. This repo’s workflow is **gated** by the Actions variable `ENABLE_DEPENDENCY_REVIEW=true` so CI does not fail when graph is off (see `.github/workflows/dependency-review.yml`). Docker-based deployment is intentionally **not** part of this lab.

---

## What `mvn verify` does (local = CI core)

Run from the repo root:

```bash
mvn -B verify
```

Phases and plugins include:

1. **Spotless** (`spotless:check` on `validate`) — Google Java Format on `src/main/java` and `src/test/java`. Fix locally with `mvn spotless:apply`.
2. **Checkstyle** (`validate`) — [`config/checkstyle/checkstyle.xml`](../config/checkstyle/checkstyle.xml).
3. **JaCoCo prepare-agent** — Instruments **Surefire** test JVMs; writes `target/jacoco.exec`.
4. **Compile / test-compile** — Main + test bytecode.
5. **PMD** (`process-test-classes`) — [`config/pmd/ruleset.xml`](../config/pmd/ruleset.xml).
6. **SpotBugs + FindSecBugs** (`process-test-classes`) — [`config/spotbugs/exclude.xml`](../config/spotbugs/exclude.xml).
7. **Surefire** — Unit tests matching `**/*Test.java` (TestNG).
8. **Package** — Spring Boot repackaged jar + **CycloneDX** SBOM → `target/bom.json`.
9. **JaCoCo prepare-agent-integration** — Same `jacoco.exec` with **`append=true`** so **Failsafe** coverage merges with unit-test coverage.
10. **Failsafe** — Integration tests `**/*IT.java`; `OpenApiExportIT` writes **`target/openapi.json`** from `/v3/api-docs`.
11. **JaCoCo report + check** — HTML under `target/site/jacoco/` and a **minimum line coverage** threshold on the bundle.

Quick checks without integration tests:

```bash
mvn test
```

Mutation testing (optional; heavy):

```bash
mvn -B test-compile org.pitest:pitest-maven:mutationCoverage -Ppitest
```

The `pitest` profile uses **`pitest-testng-plugin` 1.0.0** alongside **`pitest-maven` 1.17.x** (plugins use independent versions).

---

## CI-only step: Spectral

After `mvn verify`, CI runs:

```bash
npx --yes @stoplight/spectral-cli lint target/openapi.json --ruleset .spectral.yaml
```

Rules live in [`.spectral.yaml`](../.spectral.yaml). If this step fails, fix the OpenAPI document produced at runtime or relax rules deliberately (and document why).

---

## Previous simplified CI (removed)

Earlier versions of this repo ran Checkstyle, compile, Surefire, and Failsafe as **separate** Maven invocations. The project now relies on a **single** `mvn verify` so local runs and CI stay aligned.

---

## Demo script: full cycle (what students do live)

Assume default branch is **`main`** (GitHub’s default). If yours is **`master`**, swap the name in commands and in branch protection.

### 1. Fork and clone (already done in your course)

Students work on their fork; they sync from your upstream if you use one.

### 2. Create a branch

```bash
git checkout -b lesson/ci-demo
```

Branch names should be meaningful (`lesson/ci-demo`, `fix/login-validation`, …).

### 3. Make a small, safe change

**Example A — documentation only**

- Edit `README.md` or this file: add your name and date in a bullet under “Class notes.”

**Example B — version bump (Maven)**

- Open [`pom.xml`](../pom.xml).
- Change `<version>1.0.0-SNAPSHOT</version>` to e.g. `<version>1.0.1-SNAPSHOT</version>` (teaching-only; real teams follow semver + release process).

### 4. Commit and push

```bash
git add .
git status          # review what will commit
git commit -m "chore: bump version for CI demo"
git push -u origin lesson/ci-demo
```

### 5. Open a Pull Request (Merge Request)

On GitHub: **Compare & pull request** → target **`main`** (or **`master`**) → describe what changed.

### 6. Watch CI while the PR is open

- Open the PR → tab **Checks** (or **Actions** on the repo).
- You should see workflow **CI** running.
- Wait until the **CI** workflow completes for both JDK versions (and optional workflows such as dependency review or semantic PR pass your policy).

**Talking points while it runs**

- “This is running on GitHub’s machine, not my laptop.”
- “If I broke compilation or tests, everyone would see it before merge.”

### 7. Merge

After review (and green CI):

- **Merge pull request** → confirm.
- Default branch now contains the change; CI also runs on **`push`** to `main`, so the merged commit is verified again.

### 8. Optional: branch protection

Repo **Settings → Branches → Branch protection rule** for `main`:

- Require pull request before merging.
- Require status checks — select **CI** jobs you care about (the matrix produces one check per JDK, e.g. `build (17)` and `build (21)`), plus any other required workflows.

Students then **cannot** push broken code straight to `main` without failing checks or bypass rules.

---

## Troubleshooting CI failures

| Symptom | What to check |
|--------|----------------|
| Workflow never runs | Wrong branch name in `ci.yml` (`main` vs `master`) or PR targets a branch not listed under `on`. |
| JDK errors | CI uses Temurin **17** and **21**; locally match one of those if behavior differs. |
| Tests pass locally but fail in CI | Same command locally? Run `mvn -B verify`. Time-sensitive tests? This repo avoids flaky clocks in ITs. |
| Spotless fails | Run `mvn spotless:apply`, commit formatted sources. |
| Spectral fails | Ensure `mvn verify` completed so `target/openapi.json` exists; fix OpenAPI output or adjust `.spectral.yaml` with team agreement. |
| Semantic PR check fails | PR title needs conventional prefixes (`feat:`, `fix:`, …); edit the title or disable `.github/workflows/semantic-pr.yml` on your fork. |
| Mutation / nightly job fails | Run `mvn -B test-compile org.pitest:pitest-maven:mutationCoverage -Ppitest` locally and inspect `target/pit-reports/`. |
| Dependency review: “not supported” / graph error | Turn on **Dependency graph** (Settings → Code security). Until then, do **not** set `ENABLE_DEPENDENCY_REVIEW`; the workflow skips. To run review after enabling graph, set Actions variable `ENABLE_DEPENDENCY_REVIEW` = `true`. |

---

## Further reading (official)

- [GitHub Actions documentation](https://docs.github.com/en/actions)
- [Building and testing Java with Maven](https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing-java-with-maven)

---

## CI status badge (optional)

After your repo exists at `https://github.com/YOU/bookstore-api-lab`, add to [`README.md`](../README.md):

```markdown
[![CI](https://github.com/YOU/bookstore-api-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/YOU/bookstore-api-lab/actions/workflows/ci.yml)
```

Replace `YOU` and repo name with yours.
