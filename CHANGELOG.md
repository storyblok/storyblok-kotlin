### Changelog

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
