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
2. **Repeatable checks** — Same commands every time (`mvn verify`), same JDK on a clean machine (`ubuntu-latest`), fewer “works on my laptop” surprises.
3. **Protect the trunk** — Pull requests can **require** green checks before merge (branch protection). Students practice the same discipline as industry repos.
4. **Living documentation** — The workflow file [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) *is* the agreed definition of “good enough to merge.”

---

## What GitHub Actions is

**GitHub Actions** runs automated **workflows** when **events** happen on GitHub (push, pull request, schedule, etc.).

- **Workflow** — YAML file under `.github/workflows/` (we use `ci.yml`).
- **Job** — A group of steps that run on a **runner** (e.g. `ubuntu-latest`).
- **Step** — Checkout code, install JDK 17, run `mvn -B verify`.

When you open a PR, GitHub shows **Checks**: green ✅ if tests passed, red ❌ if something failed (with logs).

---

## What this project’s CI job does

File: [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

On **push** or **pull_request** to `main` or `master`:

1. Checks out your code.
2. Installs **Temurin JDK 17** (matches this Spring Boot 3 lab).
3. Runs **`mvn -B verify`** — compiles and runs tests (including `BookApiIT` under the `test` profile).

If any step fails, the workflow fails and the PR should **not** be merged until fixed.

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
- Wait until **Maven verify** completes: green ✅ means tests passed.

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
- Require status checks — select **CI / build** (exact name may vary).

Students then **cannot** push broken code straight to `main` without failing checks or bypass rules.

---

## Troubleshooting CI failures

| Symptom | What to check |
|--------|----------------|
| Workflow never runs | Wrong branch name in `ci.yml` (`main` vs `master`) or PR targets a branch not listed under `on`. |
| JDK errors | Workflow must use **Java 17** (Spring Boot 3). |
| Tests pass locally but fail in CI | Same command locally? Run `mvn -B verify`. Time-sensitive tests? This repo avoids flaky clocks in ITs. |

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
