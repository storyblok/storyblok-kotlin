---
name: implement
description: Use when asked to implement, build, or code a change that has an approved plan
model: opus
effort: high
---

# Implement

Implement the approved plan: $ARGUMENTS

## Current state

- Branch: !`git branch --show-current`
- Working tree: !`git status --short 2>/dev/null | head -20`

## Instructions

1. **Follow the plan**: carry out the steps from the approved plan (e.g.
   `claude-output/plan-<id>.md`).
2. **Small diffs**: make one logical change at a time.
3. **No scope creep**: only implement what was planned.
4. **Verify as you go**: run the module's JVM tests after each meaningful change.

## Rules

- Don't add dependencies unless the plan says to. If you do, declare them in
  `gradle/libs.versions.toml`.
- Don't refactor unrelated code.
- Keep explicit visibility. New declarations are `internal` unless the plan makes them public.
- New or changed public API gets KDoc, an updated ABI dump (`./gradlew updateLegacyAbi`) and a
  `CHANGELOG.md` entry.
- Preserve the existing code style and patterns.

## Output format

```markdown
## Changes made

| File                  | Change       |
| --------------------- | ------------ |
| path/to/File.kt       | What changed |

## Verification

\`\`\`bash
# Commands run and their results
\`\`\`

## Notes

- Any deviations from the plan (with justification)
- Any follow-up items discovered
```

## Post-implementation checklist

- [ ] All planned changes completed
- [ ] Tests pass: `./gradlew :<module>:jvmTest`
- [ ] ABI is current: `./gradlew checkLegacyAbi` (or dumps updated deliberately)
- [ ] Docs build if KDoc changed: `./gradlew :dokkaGenerate`
- [ ] `CHANGELOG.md` updated for user-facing changes
