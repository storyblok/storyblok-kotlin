### Changelog

**0.3.0**

- BREAKING CHANGE: the `Storyblok(...)` composable's parameter is renamed
  `blokProvider` → `blockProvider` so **named-argument** callers
  must update the label. Positional callers are unaffected.

| Old | New |
|-----|-----|
| `RichText.Blok` | `RichText.Block` |
| `BlokScope` / `.Blok()` | `BlockScope` / `.Block()` |
| `BlokProvider` / `.blokScope` | `BlockProvider` / `.blockScope` |
| `BlokProviderScope` / `blok(){}` | `BlockProviderScope` / `block(){}` |
| `blokProvider()` | `blockProvider()` |
| `blokProviderWithoutRichText()` | `blockProviderWithoutRichText()` |

- Fixed resolving relations for blocks embedded in rich text (com.storyblok.cdn.StoryblokClientException: Expected JsonObject, but had JsonLiteral) 

**0.2.0**

- Added JetNews sample app
- Initial release of the Compose SDK
- Initial release of the Material 3 Rich Text Provider
- Initial release of the Content Delivery Client
- Add `only-if-cached` support to the Storyblok Ktor Client Plugin

**0.1.0**

- Initial release of the Storyblok Ktor Client Plugin
