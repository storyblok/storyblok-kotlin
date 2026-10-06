---
name: review-and-qa
description: Use when asked to review a commit, branch, or PR, or to produce a QA plan for changes
model: opus
context: fork
agent: reviewer
effort: high
---

# Review Changes

Review the changes for: $ARGUMENTS.

Perform a two-phase review: a **code review** of quality, correctness and fit, then a **QA review**
with manual test cases. `AGENTS.md` has the repo layout and conventions. Don't rediscover them.

## Step 0: Collect the context

Run this first, with the arguments unchanged (empty means the current branch and working tree):

```bash
bash .agents/skills/review-and-qa/scripts/review-context.sh $ARGUMENTS
```

It prints the PR description and CI status, commits with author emails, changed files, affected
modules, repo-specific flags and the host test command. It also writes the full diff to
`claude-output/review-<id>.diff`. Read that diff once, in full. Then open surrounding code only
where a finding depends on it. For a PR or commit that isn't checked out, read files at the head
with `git show "<sha>:<path>"` (quoted). If the script says the PR is already merged, review it as
merged code and frame findings as follow-ups.

Keep it fast:

- Trust CI for platform coverage. Don't run native, JS, Wasm or simulator tests, and don't build
  the samples.
- If the checked-out code contains the change (the script says so) and library code changed,
  start the suggested
  `./gradlew ... jvmTest checkLegacyAbi` command **in the background** right away. Collect the
  result before writing the report.
- Search with Grep across modules instead of reading whole files.

## Phase 1: Code review

### Required for all PRs

- [ ] **Small diff**: one logical change. If the PR does several, say how to split it.
- [ ] **Tests included**: new behaviour has tests, and a bug fix has a regression test that fails
      without the fix.
- [ ] **No secrets**: no tokens beyond the public demo tokens already used in `examples/` and the
      samples.
- [ ] **Commits**: conventional format with a module scope. Org members' commits use
      `@storyblok.com` emails (CI rejects others).
      Judge commit rules against the base's `.github/` (an older PR may predate a check).

### Public API and binary compatibility

- [ ] Every new public declaration is intended. Explicit API mode forces visibility, so check
      whether `internal` would do.
- [ ] The `api/*.api` / `api/*.klib.api` diff matches the intent. Removed or changed lines are
      breaking. If sources changed but the dump didn't, confirm there was no API change.
- [ ] Binary-breaking changes are called out: adding a parameter to a public function (even with a
      default), changing a `data class` constructor (`copy`/`componentN`), adding a subtype to a
      public `sealed` hierarchy, or narrowing a return type.
- [ ] Anything public only for the SDK's own modules is `@InternalAPI` (in `content-api-client`).
- [ ] Types exposed in public signatures come from `api(...)` dependencies, not
      `implementation(...)`.

### Multiplatform correctness

- [ ] `commonMain` uses no JVM-only APIs (`java.*`, `String.format`, `synchronized`, `System.*`).
- [ ] Every `expect` has an `actual` in each source set the module targets, with the same behaviour
      (JVM/Android, native, JS, Wasm).
- [ ] Nothing assumes threads or blocking on JS/Wasm (`runBlocking`, `Dispatchers.IO`).
- [ ] New dependencies support every target of the module they're added to (see `AGENTS.md`).

### Coroutines and Flow

- [ ] Structured concurrency: no `GlobalScope`, and scopes are owned and cancelled.
- [ ] `CancellationException` isn't swallowed by a broad `catch (e: Exception)` or
      `runCatching`.
- [ ] Flow semantics are deliberate (cold vs hot, `distinctUntilChanged`, buffering), and
      collection doesn't leak.

### Storyblok API behaviour

- [ ] Ktor plugin: retry and backoff only on 429/5xx and network errors, rate limits per API (CDN
      vs MAPI), `cv` handling, and stable cache keys (parameter order).
- [ ] Errors: request failures surface as `StoryblokClientException`.
      `SerializationException` isn't wrapped.
- [ ] Serialization tolerates what the API **and** the Visual Editor send: unknown keys, missing
      optional fields, `null`s, and relations inlined by the editor. Test fixtures should be shaped
      like real payloads, not built by a helper that only produces the happy shape.
- [ ] Draft vs published, `resolve_relations`/`resolve_level` and live preview (`wasmJs` bridge)
      behave as documented.

### Compose (`storyblok-compose`, `storyblok-material3`)

- [ ] Composables take `modifier: Modifier = Modifier` as the first optional parameter.
- [ ] `remember`/`LaunchedEffect` keys are complete. No work happens on every recomposition.
- [ ] Rich text rendering covers the node and mark types the change touches, and unknown nodes
      degrade gracefully.

### Quality, reuse and design

- [ ] Changes fit existing patterns. No unnecessary coupling between modules.
- [ ] Existing helpers are reused. No duplicated logic or scattered constants. No magic numbers.
- [ ] Small, well-named functions. No dead or commented-out code.
- [ ] Error and log messages say what went wrong and what to do. Logging uses
      `KtorSimpleLogger`, never `println`.

### If user-facing

- [ ] KDoc on new or changed public API. Dokka runs with `failOnWarning`.
- [ ] Module `README.md` and `Module.md` are updated where they cover the change.
- [ ] `CHANGELOG.md` has an entry under the next version. Breaking changes start with
      `BREAKING CHANGE:` and include before/after.
- [ ] Deprecations use `@Deprecated(..., ReplaceWith(...))` rather than removal, where possible.
- [ ] Docs and messages use Storyblok terms ("block", not "blok").

### If adding or bumping dependencies

- [ ] Versions are declared only in `gradle/libs.versions.toml`. The Kotlin 2.3.x hold
      (KT-89275) is respected.
- [ ] The dependency is justified, maintained and MIT-compatible, and supports all targets.
- [ ] Yarn lock changes come from `kotlinUpgradeYarnLock` / `kotlinWasmUpgradeYarnLock`.

### Verify before reporting

For each candidate issue, re-read the code that proves it. Drop anything you can't point to.
Prefer five real issues over fifteen maybes. Mark anything you inferred but couldn't confirm as
**Unverified**.

### Severity

- **Critical**: wrong behaviour in a common path, data loss, a security problem, or an unintended
  binary break.
- **Major**: wrong in an edge case or on one platform, a missing regression test, or an
  undocumented user-facing change.
- **Minor**: clarity, naming, duplication, or docs polish.

## Phase 2: QA review

Use the [Test Plan Template](./templates/test-plan.md). See the
[Test Plan Example](./examples/stories-paging-test-plan.md) for the level of detail.

Write manual test cases a person can run, covering:

1. **Happy path**: the change working as intended.
2. **Negative cases**: an invalid token, a missing story or slug, network failure, 4xx/5xx.
3. **Edge cases**: empty content, unknown components or fields, large pages, special characters
   in slugs and queries.
4. **Error recovery**: retry/backoff, cache fallback, and live preview after a bad payload.
5. **Platforms**: which targets to spot-check by hand and why (e.g. Android and iOS via
   `samples/JetNewsCMP`, and the web target for Visual Editor preview). Use the `run-sample` skill
   steps for setup.

Skip cases automated tests already cover. Mention them in a line instead.

## Output

Write the report to `claude-output/review-<id>.md` (the `<id>` from step 0):

```markdown
# Review: <title>

## Code review summary

[LGTM / Needs changes / Blocking issues]: one or two sentences.

Verification: <commands run and results, or "CI: N passed, M failing">

## Issues found

### [Critical/Major/Minor]: Issue title

**File:** `path/to/File.kt:123`
**Problem:** What is wrong and when it happens.
**Suggestion:** How to fix it.

## QA test plan

<test plan>
```

Then reply with the verdict, the Critical and Major issues (one line each) and the report path.
