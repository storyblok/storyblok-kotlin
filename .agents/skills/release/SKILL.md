---
name: release
description:
  Use when asked to prepare or cut a release, bump the SDK version, or write release notes for
  storyblok-kotlin
disable-model-invocation: true
---

# Release

Prepare the release: $ARGUMENTS

All published modules share one version: `storyblok-kotlin` in `gradle/libs.versions.toml`.
Publishing a GitHub release runs `.github/workflows/publish.yml`. It publishes every module to Maven
Central (`publishAndReleaseToMavenCentral`, signed in CI) and deploys the Dokka site to `gh-pages`.

## Current state

- Version: !`grep '^storyblok-kotlin' gradle/libs.versions.toml`
- Latest release: !`gh release list -L 1 2>/dev/null`
- Branch: !`git branch --show-current`

## Step 1: Choose the version

Read `CHANGELOG.md` and `git log <last tag>..origin/main --oneline`. Pre-1.0 semver: a breaking
change bumps the minor version (0.7.x → 0.8.0), and anything else bumps the patch. Confirm the
version with the user if they didn't give one.

## Step 2: Bump the version (one PR, `chore(release): bump the next release to X.Y.Z`)

Branch from `origin/main` (`chore/release-X.Y.Z`), then update every reference to the old version:

```bash
OLD=$(sed -n 's/^storyblok-kotlin = "\(.*\)"/\1/p' gradle/libs.versions.toml)
git grep -n "$OLD" -- gradle/libs.versions.toml '*/README.md' '*/Module.md' samples/*/README.md samples/*/gradle/libs.versions.toml
```

That covers:

- `gradle/libs.versions.toml`: `storyblok-kotlin`
- Each module's `README.md` and `Module.md`: the dependency snippets
- `samples/JetNews/README.md`, and `storyblok` in `samples/JetNews/gradle/libs.versions.toml` and
  `samples/JetNewsCMP/gradle/libs.versions.toml`

Leave unrelated matches alone (another library can share the version string). Don't rewrite old
entries in `CHANGELOG.md`.

## Step 3: Changelog

Add a `**X.Y.Z**` section at the top of `CHANGELOG.md`, in the existing style:

- One bullet per user-visible change, in past tense ("Fixed ...", "Added ...").
- Breaking changes first, prefixed `BREAKING CHANGE:`, with before/after or a migration table.
- Link samples and docs with relative links. Leave out internal-only changes (CI, tests, repo docs).

## Step 4: Verify

```bash
./gradlew checkLegacyAbi :dokkaGenerate :ktor-client-storyblok:jvmTest :content-api-client:jvmTest
```

The samples still point at the unreleased version, so they can't resolve it yet. That's expected.

## Step 5: Open the PR

Commit, push and open the PR against `main`. Stop there. The release itself waits for the PR to
merge.

## Step 6: Publish (only after the PR is merged, and only with the user's explicit go-ahead)

Publishing is public and irreversible: Maven Central versions can't be replaced or deleted. Show
the user the tag, title and notes first, and wait for a clear yes.

```bash
gh release create vX.Y.Z --target main --title "X.Y.Z" --generate-notes
```

- The tag is lowercase `v` + version (`v0.7.1`), and the title is the bare version.
- Use `--prerelease` only if the user asks for one. It still publishes to Maven Central.
- Afterwards, watch the run: `gh run list --workflow publish.yml -L 1`. Publishing takes 10–20
  minutes.
