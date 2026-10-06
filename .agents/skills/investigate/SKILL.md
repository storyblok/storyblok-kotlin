---
name: investigate
description: Use when asked to investigate, analyze, or dig into a GitHub issue or reported bug
model: opus
context: fork
agent: investigator
effort: high
---

# Investigate Issue

Investigate the following issue: $ARGUMENTS

## Instructions

### Phase 1: Fetch issue details

1. Parse the input:
   - `DX-123` or a `linear.app` URL → **Linear ticket**
   - `#123`, a bare number, or a `github.com` URL → **GitHub issue**
   - Anything else → a free-text bug description
2. Fetch the ticket:
   - **GitHub:** `gh issue view <number> --json title,body,labels,comments,author,createdAt,state`
   - **Linear:** `bash .agents/skills/triage/scripts/linear-fetch.sh issue DX-123`
3. Extract: error messages, stack traces, SDK version, Kotlin/Ktor/Compose versions, target
   platform(s), and reproduction steps.

### Phase 2: Identify the affected module(s) and platform(s)

| Symptom                                                         | Likely module           |
| --------------------------------------------------------------- | ----------------------- |
| Auth, region, rate limiting, retry, `cv`, raw `HttpClient` use  | `ktor-client-storyblok` |
| `StoryblokClient`, decoding, relations, paging, cache, preview  | `content-api-client`    |
| `Storyblok(...)` composable, `BlockProvider`, block rendering   | `storyblok-compose`     |
| Rich text rendering, Material 3 styles, colours, images         | `storyblok-material3`   |

If it only happens on one platform, start with that source set's `actual`
(`jvmAndAndroidMain`, `nativeMain`, `jsMain`, `wasmJsMain`).

### Phase 3: Deep analysis

1. Search for the error message and the referenced functions.
2. Trace the execution path from the public entry point to the reported behaviour.
3. Check recent changes: `git log --oneline -20 -- <module>` and `CHANGELOG.md`. Was it a
   regression in a specific version?
4. Search for similar issues:
   `gh issue list --search "<keywords>" --state all --limit 10`.
5. Where it settles the question, reproduce with a temporary failing test (Ktor `MockEngine`,
   payload shaped like the real API or Visual Editor response) and run it with
   `./gradlew :<module>:jvmTest --tests '<Class>'`.

### Phase 4: Root cause analysis

Categorise it: logic bug, API change, payload shape mismatch (API vs Visual Editor), platform
divergence, missing validation, race condition or cancellation, configuration, or a documentation
gap.

### Phase 5: Propose solutions

For each root cause, give the fix (file paths and line numbers), the regression test to add, any
public API/ABI impact, and the verification commands.

## Output

1. Extract an identifier from the arguments (issue `42` → `42`, ticket `DX-296` → `DX-296`, text
   → slugified).
2. `mkdir -p claude-output` and write `claude-output/investigate-<identifier>.md`.
3. Reply with a three-line summary and the report path.

### Output structure

```markdown
# Investigation: <title>

**URL:** | **Created:** | **State:** | **Author:** | **SDK version:** | **Platforms:**

## Summary

## Affected module(s) (table: Module, Platform, Confidence, Evidence)

## Issue details (Reported behaviour, Expected behaviour, Environment, Reproduction steps)

## Code analysis (Relevant files table, Execution flow, Code snippets)

## Root cause (Category, Explanation, Evidence)

## Proposed solutions (Changes table, Code before/after, Tests to add, ABI impact, Verification)

## Similar issues

## Notes / Risks

## Next steps
```
