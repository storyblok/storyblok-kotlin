---
name: plan
description:
  Use when asked to plan, design, or architect a solution, before writing implementation code
model: sonnet
effort: high
---

# Plan

Create an implementation plan for: $ARGUMENTS

## Current state

- Branch: !`git branch --show-current`
- Working tree: !`git status --short 2>/dev/null | head -20`

## Instructions

1. **Understand the request**: read the relevant files and the module's `README.md`.
2. **Identify scope**: which modules, source sets and targets change? Does the public API change?
   (Check the `api/` dumps.)
3. **Design the approach**: prefer extending existing patterns. Put shared code in `commonMain` and
   use `expect`/`actual` only where a platform forces it.
4. **Plan the tests**: what fails before the change and passes after it? Use payloads shaped like
   real API and Visual Editor responses.

## Output format

```markdown
## Summary

[1-2 sentences describing what this plan achieves]

## Files to modify

| File                           | Change            |
| ------------------------------ | ----------------- |
| module/src/commonMain/.../X.kt | Brief description |

## Public API impact

[None / additive / breaking: list new or changed declarations, and the CHANGELOG entry needed]

## Implementation steps

1. Step one
2. Step two

## Test plan

- [ ] Test case 1
- [ ] Test case 2

## Verification commands

\`\`\`bash
./gradlew :<module>:jvmTest
./gradlew checkLegacyAbi    # or updateLegacyAbi if the API change is intended
\`\`\`

## Risks / open questions

- Assumptions, unknowns, platform-specific concerns
```

## Output

Write the plan to `claude-output/plan-<identifier>.md`.

## Rules

- Keep the plan focused and actionable.
- Only include files that actually need changes.
- Verification commands must be real and runnable (see `AGENTS.md`).
- If the requirements are unclear, ask before planning.
