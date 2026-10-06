---
name: Test Plan Example
description: Example of a manual test plan for an SDK change, verified through a sample app
---

# Story Search Paging Manual Test Plan

## Environment setup

Build the SDK into Maven Local under an unpublished version, then point JetNews at it. See the
`run-sample` skill for the full steps and how to revert.

```bash
# repo root: temporarily set storyblok-kotlin = "0.0.0-local" in gradle/libs.versions.toml
./gradlew $(for m in ktor-client-storyblok content-api-client storyblok-compose storyblok-material3; do printf ':%s:publishKotlinMultiplatformPublicationToMavenLocal :%s:publishAndroidPublicationToMavenLocal ' $m $m; done)
cd samples/JetNews
# temporarily set storyblok = "0.0.0-local" in gradle/libs.versions.toml
./gradlew :app:installDebug
adb shell am start -n com.example.jetnews/.MainActivity
```

The search bar on the home screen runs `StoryblokClient.stories<Post> { searchTerm = ... }` and
shows the `Pager` through `collectAsLazyPagingItems()`.

## Test cases

### 1. Search results

#### 1.1 Blank term lists every post

Open the search bar without typing.

**Verify:**

- [ ] Every post in the space is listed once. The request has no `search_term` parameter
      (`adb logcat | grep -i storyblok`).

#### 1.2 Matching term

Type a word from one post's title.

**Verify:**

- [ ] Only matching posts are listed.
- [ ] Typing more characters replaces the results instead of appending to them.

### 2. Paging

#### 2.1 Scrolling past the first page

With a blank term in a space that has more posts than one page holds, scroll to the end.

**Verify:**

- [ ] The next page loads as you approach the end, with no duplicate or missing posts.
- [ ] Loading stops after the last page. No empty trailing page is requested.

### 3. Negative and edge cases

#### 3.1 No matches

Search for `zzzz-no-such-post`.

**Verify:**

- [ ] The list is empty, without an error or a spinner that never stops.

#### 3.2 Special characters

Search for `C++ & "quotes"`.

**Verify:**

- [ ] The request encodes the term correctly, and the app doesn't crash.

### 4. Error recovery

#### 4.1 Offline, then online

Turn on airplane mode, search, then turn it off and search again.

**Verify:**

- [ ] Offline, previously cached results show, or nothing does. The app doesn't crash.
- [ ] Back online, results load without restarting the app.

## Checklist summary

### Search

- [ ] A blank term lists everything.
- [ ] Matching terms filter.
- [ ] No matches are handled.

### Paging

- [ ] Later pages load without duplicates.
- [ ] Loading stops at the end.

### Resilience

- [ ] Offline and recovery work.
- [ ] Special characters are encoded.
