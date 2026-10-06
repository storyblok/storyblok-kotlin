---
name: qa-engineer-unit
description: Use when adding or changing unit tests for logic in a module
---

# QA Engineer for Unit Testing

## Responsibilities

- Every behaviour change and bug fix has a test that fails without it.
- Tests stay small, explicit and easy to reason about.
- Direct value assertions win over elaborate mocks.

## Where tests go

- `src/commonTest/kotlin` by default, so the test runs on every target in CI.
- A platform source set (`jvmTest`, `wasmJsTest`, ...) only when the code under test is platform
  code (an `actual`, file cache storage, the Visual Editor bridge) or needs a platform API.
- `examples/` is not a test suite. It holds docs snippets that call the live API.

## Stack and patterns

- `kotlin.test` (`@Test`, `assertEquals`, `assertFailsWith`) and `kotlinx-coroutines-test`
  (`runTest`, virtual time via `testTimeSource` / `advanceTimeBy`).
- HTTP goes through Ktor's `MockEngine`. Never hit the network from a unit test.
- Names are backticked sentences describing behaviour:
  `` fun `retries up to 5 times on server error or 429 too many requests status codes`() ``.
- Payload fixtures are shaped like what the API **or the Visual Editor** really sends, including
  fields the SDK ignores. A fixture built by a helper that only produces the happy CDA shape has
  hidden real bugs before.
- Reuse the helpers already in the module's tests (e.g. `respondJson`) before writing new ones.
- Explain *why* in a comment when a test pins a budget, a timing or a quirk of the API.

## Shape

```kotlin
class StoryblokTest {

    @Test
    fun `throws on client error status codes`() = runTest {
        val client = HttpClient(MockEngine { respondBadRequest() }) {
            install(Storyblok(CDN)) { accessToken = "mock-api-key" }
        }
        assertFailsWith<ClientRequestException> { client.get("stories/mock-slug") }
    }
}
```

## Coverage

For each change: the success case, the failure or missing-data case, and one or two edge cases
(empty, unknown fields, boundaries). For a bug fix, first confirm the test fails on the unfixed
code.

## Running

```bash
./gradlew :<module>:jvmTest --tests '*StoryblokTest*'   # fast loop
./gradlew :<module>:allTests                           # every target this host can run
```

`runBlocking` isn't available on JS/Wasm. If a test needs real time (as Paging does), put it in
`jvmTest` or a source set that excludes JS/Wasm.
