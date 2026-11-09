### Changelog

**0.8.0**

- In Storyblok's Visual Editor, on the `wasmJs` target, tapping a block drawn with `Block()` now opens its form in the
  editor, as clicking a block does on a web page. The first tap on a block selects it rather than doing what the block
  does with a tap. The selected block is outlined, and is scrolled into view when the author picks it in the editor's
  form, or adds, moves or duplicates it. A block's composable must apply the `Modifier` it is given for this to work.
- In Storyblok's Visual Editor, on the `wasmJs` target, the Content Delivery API client now requests content in the
  language the author picks in the editor, in place of the configured `language`. Field-level translations can now be
  previewed. In the space's default language no language is requested. A page outside the editor and every other
  target keep the configured `language`. The editor is also told the configured `fallbackLanguage`, so untranslated
  fields keep the fallback language's values as the author edits, rather than switching to the default language's.
- Added `BlockScope.blocks(blocks, state)`, which adds blocks to a `LazyColumn` or `LazyRow` keyed by their `_uid`.
  In the Visual Editor it also scrolls the list to a selected block that is off screen, which a lazy list cannot do by
  itself. `storyblok-compose` now exposes Compose Foundation as an `api` dependency.
- BREAKING CHANGE: Content that fails to deserialize now always surfaces from `StoryblokClient.story(...)` and
  `StoryblokClient.stories(...)` as a `SerializationException`, including a value a property's serializer cannot parse,
  such as a malformed date or uuid, and a `require` in a component's `init` block. These were reported as a
  `StoryblokClientException`, the type for a failed request. If you catch `StoryblokClientException` to handle content
  that does not deserialize, also catch `SerializationException`.
- Fixed a blank Date/Time, Number or Single-Option field, which Storyblok sends as `""`, failing to deserialize into a
  nullable or defaulted number, date, time, `Uuid` or `Story` property: it is now `null` or the property's default.
- A `List<Story<T>?>` relation keeps the stories that resolve, with `null` in place of each one that can't or that
  closes a cycle. A `null` entry in the list now stays `null` rather than failing decoding when it comes first.
- The unresolved and circular relation errors now name the type to model the field with: `Story<T>?` or `String`
  for a single story, `List<Story<T>?>` or `List<String>` for a list.
- Fixed `StoryblokClient.story(...)` failing with a `SerializationException`, without fetching the latest version,
  when its cached copy no longer deserialized (for example, after a required field was added to a component). Such a
  copy is now passed over and revalidated with the server; only the latest version failing to deserialize is reported.
  `StoryblokClient.stories(...)` likewise treats such a cached page as a miss rather than reporting a load error.
- Fixed the Ktor plugin not setting `Content-Type: application/json` on `PATCH` requests, so a `PATCH` with a JSON
  body failed to send.

**0.7.1**

- Fixed live updates never reaching a preview of a story with a resolved relation.
- Updated the [Compose Multiplatform sample](samples/JetNewsCMP) to read draft content on debug app builds. 

**0.7.0**

- Updated a [Compose Multiplatform sample](samples/JetNewsCMP), to target Android and iOS only, 
  with a web target specifically for Visual Editor preview.
- `StoryblokClient.story(...)` now emits live updates from Storyblok's Visual Editor on the `wasmJs` target, so a
  preview re-renders as an author types. Nothing to configure: the flow keeps emitting while the page is embedded in
  the editor, and is unchanged everywhere else.

**0.6.0**

- BREAKING CHANGE: `StoryblokClient.stories(...)` returns an `androidx.paging.Pager<Int, Story<T>>` rather than a
  `Flow<PagingData<Story<T>>>`.
- The Content Delivery API Client now declares `kotlinx-datetime` as an `api` dependency. `Story.sortByDate` and
  `StoryblokDateTimeSerializer` expose its types, so consumers needed it on their compile classpath already.
- Updated `kotlinx-datetime` to 0.8.0 and Ktor to 3.5.2.
- Added a [Compose Multiplatform sample](samples/JetNewsCMP), a port of the JetNews sample running the same shared
  UI on Android, iOS, desktop and the web.

**0.5.0**

- Added `StoryblokClient.stories(...)` for the
  [retrieve multiple stories](https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-multiple-stories)
  endpoint, returning a `Flow<androidx.paging.PagingData<Story<T>>>`.
- BREAKING CHANGE: the Content Delivery API Client no longer publishes the `iosX64` and
  `androidNativeArm32`/`androidNativeArm64`/`androidNativeX64`/`androidNativeX86` targets, which
  `androidx.paging:paging-common` does not support.

**0.4.0**

- The Content Delivery API Client now persists its HTTP cache to disk on JVM and Android, so cached
  responses survive a process restart. Other targets are unchanged and keep the in-memory cache.
- Fixed the `resolve_relations` query parameter being built in a non-deterministic order ensuring
  cache key is stable across processes.
- Fixed `SerializationException` getting wrongly wrapped in `StoryblokClientException`.

**0.3.0**

- BREAKING CHANGE: the `Storyblok(...)` composable's parameter is renamed `blokProvider` →
  `blockProvider` so **named-argument** callers must update the label. Positional callers are
  unaffected.

| Old | New |
|-----|-----|
| `RichText.Blok` | `RichText.Block` |
| `BlokScope` / `.Blok()` | `BlockScope` / `.Block()` |
| `BlokProvider` / `.blokScope` | `BlockProvider` / `.blockScope` |
| `BlokProviderScope` / `blok(){}` | `BlockProviderScope` / `block(){}` |
| `blokProvider()` | `blockProvider()` |
| `blokProviderWithoutRichText()` | `blockProviderWithoutRichText()` |

- Fixed resolving relations for blocks embedded in rich text
  (com.storyblok.cdn.StoryblokClientException: Expected JsonObject, but had JsonLiteral)
- Added a `resolveLevel` parameter to the `story()` functions to control how deeply story relations
  are resolved.
- Fixed a `StackOverflowError` when resolving circular story relations

**0.2.0**

- Added JetNews sample app
- Initial release of the Compose SDK
- Initial release of the Material 3 Rich Text Provider
- Initial release of the Content Delivery Client
- Add `only-if-cached` support to the Storyblok Ktor Client Plugin

**0.1.0**

- Initial release of the Storyblok Ktor Client Plugin
