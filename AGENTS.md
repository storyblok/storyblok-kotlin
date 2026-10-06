# Storyblok Kotlin

Kotlin Multiplatform SDKs for Storyblok, published to Maven Central under `com.storyblok`. Gradle
multi-module build. Every published module ships the same version, set by `storyblok-kotlin` in
`gradle/libs.versions.toml`.

## Layout

| Module                  | Depends on              | What it is                                                                                                                                       |
| ----------------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `ktor-client-storyblok` | Ktor client             | Ktor plugin: auth, regions, rate limiting, retry with backoff, `cv` cache invalidation, JSON. Package `com.storyblok.ktor`.                       |
| `content-api-client`    | `ktor-client-storyblok` | Typed Content Delivery API client (`StoryblokClient`), relation resolution, paging, HTTP cache, Visual Editor bridge. Package `com.storyblok.cdn`. |
| `storyblok-compose`     | `content-api-client`    | Compose Multiplatform integration: `Storyblok(...)` composable, `BlockProvider`. Package `com.storyblok.compose`.                                 |
| `storyblok-material3`   | `storyblok-compose`     | Default Material 3 rich text block provider. Package `com.storyblok.material3`.                                                                  |
| `examples`              | the two client modules  | Docs-site snippets as tests. Not published. **They call the live Storyblok APIs.**                                                               |

- `samples/JetNews` (Android) and `samples/JetNewsCMP` (Android, iOS, wasmJs web preview) are
  **separate Gradle builds**. They consume the SDK from Maven Central or `mavenLocal()`, not as
  project dependencies. See the `run-sample` skill.
- Each published module has `README.md` (GitHub), `Module.md` (Dokka) and `api/` (ABI dumps).
- `kotlin-js-store/` holds the committed yarn locks for the JS and Wasm test tooling.

### Targets

Targets differ per module. Check `build.gradle.kts` before using a platform API in `commonMain`.

- `ktor-client-storyblok` and `examples`: JVM, Android, JS (browser + Node), wasmJs, iOS, macOS,
  tvOS, watchOS, Linux, Windows (`mingwX64`), Android Native.
- `content-api-client`: the same, minus `iosX64` and `androidNative*` (androidx.paging does not
  support them).
- `storyblok-compose`, `storyblok-material3`: JVM, Android, JS, wasmJs, `iosArm64`,
  `iosSimulatorArm64`, `macosArm64`.

Platform code uses `expect`/`actual` across `jvmAndAndroidMain`, `nativeMain`, `jsMain` and
`wasmJsMain` (a custom hierarchy group in each build file).

## Commands

Run Gradle from the repo root. JVM tests are the fast inner loop. CI runs the rest.

```bash
./gradlew :content-api-client:jvmTest                    # one module, JVM
./gradlew :content-api-client:jvmTest --tests '*StoriesTest*'
./gradlew :ktor-client-storyblok:allTests                # every target the host can run
./gradlew checkLegacyAbi                                 # public API vs committed api/ dumps
./gradlew updateLegacyAbi                                # regenerate dumps after an intended API change
./gradlew :dokkaGenerate                                 # docs, fails on any Dokka warning
./gradlew publishToMavenLocal                            # unsigned, for the samples
```

- CI test tasks per module: `jvmTest`, `testAndroidHostTest`, `jsTest`, `wasmJsTest`,
  `linuxX64Test`, `mingwX64Test`, `macosArm64Test`, `iosSimulatorArm64Test`,
  `tvosSimulatorArm64Test`, `watchosSimulatorArm64Test`. Apple targets need macOS with Xcode.
- There is no linter or formatter. The compiler (explicit API mode), ABI validation and Dokka's
  `failOnWarning` are the checks. Follow `kotlin.code.style=official`.
- `storyblok-compose` and `storyblok-material3` have no tests yet. Their CI jobs only compile.
- `:examples` tests hit the real API with demo tokens. Run them on purpose, not as a regression
  suite: `./gradlew :examples:jvmTest --tests 'ktorplugin.cdn.*'`.
- Toolchain: Gradle wrapper 9.x on a JetBrains JDK 21 daemon (auto-provisioned via foojay). CI uses
  Temurin 17. Android needs an SDK (`ANDROID_HOME` or `sdk.dir` in `local.properties`).

## Conventions

- **Explicit API mode** in every published module. Public declarations need explicit visibility
  and return types. Default to `internal`.
- **ABI is tracked.** Any public API change updates the `api/*.api` and `api/*.klib.api` dumps:
  run `./gradlew updateLegacyAbi` and commit them. CI auto-commits dumps on same-repo PRs, but fork
  PRs fail. A dump diff in a PR is the public API diff, so read it.
- **Internal-but-public API** in `content-api-client` is marked `@InternalAPI` (opt-in level ERROR)
  and must stay out of user-facing docs.
- **Versions** live only in `gradle/libs.versions.toml`. Kotlin is held at 2.3.x (see the comment
  there, KT-89275). Don't bump it, or `kotlin-wrappers`, without checking that issue.
- **Context parameters** (`-Xcontext-parameters`) are enabled in `storyblok-compose` and
  `storyblok-material3` only.
- **Errors:** `content-api-client` surfaces request failures as `StoryblokClientException`. Don't
  wrap `SerializationException` (see 0.4.0 in `CHANGELOG.md`).
- **Logging:** `KtorSimpleLogger("com.storyblok...")`, never `println` in library code.
- **Tests:** `kotlin.test` + `kotlinx-coroutines-test` (`runTest`), Ktor `MockEngine` for HTTP.
  Backticked sentence names (`` fun `retries up to 5 times on 429`() ``). Put tests in `commonTest`
  unless they need a platform. A bug fix needs a regression test.
- **Docs:** KDoc on every public declaration. A user-facing change updates the module `README.md`
  and `Module.md` where they cover it, plus a `CHANGELOG.md` entry under the next version.
- **Storyblok terms:** Use "block", not "blok" (the API was renamed in 0.3.0). Use "story",
  "space", "Content Delivery API", "Management API" and "Visual Editor".
- **Samples** pin their own dependency versions. Keep their `storyblok` version in step with
  releases.

## Git and PRs

- Conventional commits, imperative: `type(scope): description`. Aim for a subject under 50 chars
  (`.github/git-commit-instructions.md`). Long module scopes make that hard, so keep the
  description short. The scope
  is the module or sample (`fix(content-api-client): ...`, `fix(JetNewsCMP): ...`). See
  `.github/git-commit-instructions.md`.
- Use `feat` and `fix` only for changes consumers can observe. Tests, CI, samples tooling and repo
  docs are `chore`, `ci`, `docs` or `test`. Releases are `chore(release): ...`.
- Org members must commit with their `@storyblok.com` email. CI (`commit-email-check.yml`) fails
  otherwise. External contributors are exempt.
- On `main`, only commit when explicitly asked. Never `git push --force`. Use
  `--force-with-lease`.
- PRs target `main`. Keep them to one logical change. Note any public API change and how the ABI
  dump moved.
