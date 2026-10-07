# Contributing

Thanks for helping improve the Storyblok Kotlin SDKs! The general process (open an issue to discuss
your proposal first, then fork, branch and open a pull request) is in Storyblok's
[contributing guide](https://github.com/storyblok/.github/blob/main/contributing.md), and we expect
everyone to follow the [code of conduct](https://www.storyblok.com/trust-center#code-of-conduct).
This page covers what's specific to this repository.

## Development setup

- **JDK**: the Gradle wrapper provisions the JDK it needs. Any JDK 17+ on your `PATH` will start it.
- **Android SDK**: set `ANDROID_HOME`, or `sdk.dir` in `local.properties`.
- **Xcode** (macOS only): needed to build and test the Apple targets.
- **IDE**: IntelliJ IDEA or Android Studio with the Kotlin Multiplatform plugin.

## Building and testing

```bash
./gradlew :content-api-client:jvmTest   # fast loop: one module on the JVM
./gradlew :content-api-client:allTests  # every target your machine can run
./gradlew checkLegacyAbi                # has the public API changed?
```

CI runs each module's tests on every platform (Linux, Windows and macOS runners), so you don't
need to run them all locally. [AGENTS.md](AGENTS.md) lists every module, target and command.

## Public API changes

Every published module tracks its public API in `api/` dumps. If you change the public API on
purpose, run `./gradlew updateLegacyAbi` and commit the updated files. Pull requests from forks fail
CI with a stale dump. New public API needs KDoc, and user-facing changes need a `CHANGELOG.md`
entry.

## Commits and pull requests

- Use [Conventional Commits](https://www.conventionalcommits.org/) with the module as the scope,
  e.g. `fix(content-api-client): tolerate unknown keys`. Write the subject in the imperative and
  aim for under 50 characters. With a long scope, keep the description short.
- Keep each pull request to one logical change, with tests. A bug fix needs a regression test.
- Storyblok employees: commit with your `@storyblok.com` email address. CI checks it.

## Using AI coding tools

This repository is set up for AI coding agents. Use them if you like, but you're responsible for
everything you submit: review, run and understand the code as if you wrote it yourself.

- **[AGENTS.md](AGENTS.md)**: the project guide for agents. It covers layout, targets, commands,
  conventions and commit rules. Claude Code (v2.1.277 or later), Codex, Cursor, GitHub Copilot,
  Gemini CLI and most other tools read it automatically.
- **`/review-and-qa <PR|branch>`**: a skill in [`.agents/skills/`](.agents/skills) (symlinked
  into `.claude/skills/`) that reviews a change against this repo's checklist and writes a QA plan
  to `claude-output/`, which is gitignored. Other agents can follow its `SKILL.md` as instructions.
- Please run `/review-and-qa` on your branch before you open a pull request. It catches the
  things reviewers here look for first: ABI dumps, missing regression tests and platform
  differences.
