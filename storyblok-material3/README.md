# Storyblok Material 3 Rich Text Provider

A Material 3 rich text provider for the [Storyblok Compose SDK](../storyblok-compose).

With out-of-the-box rendering for all rich text node types using Material 3 components and theming.

# Getting Started
## Add dependency

The Material 3 provider requires adding the `storyblok-material3` artifact in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.storyblok:storyblok-material3:0.2.0")
}
```

> [!NOTE]
> This module depends on `storyblok-compose`, which will be included transitively.

## Create a blok provider

Use the [`blokProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/blok-provider.html) function to create a provider with pre-configured Material 3 rich text renderers:

```kotlin
val myBlokProvider = blokProvider {
    blok<Page> { page, modifier ->
        Column(modifier) {
            Text(page.title, style = MaterialTheme.typography.headlineLarge)
            page.body.forEach { Blok(it) }
        }
    }
    
    blok<Article> { article, modifier ->
        Column(modifier) {
            Text(article.headline)
            RichText(article.content, Modifier.fillMaxWidth())
        }
    }
}
```

## Use with Storyblok composable

Pass the provider to the `Storyblok` composable:

```kotlin
@Composable
fun App() {
    MaterialTheme {
        Storyblok(
            accessToken = "YOUR_ACCESS_TOKEN",
            version = Draft,
            blokProvider = myBlokProvider
        ) {
            val story by story<Article>("articles/hello-world").collectAsState(null)
            
            story?.let { 
                Blok(it.content)
            }
        }
    }
}
```

# User Guide
## The blokProvider function

The [`blokProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/blok-provider.html) function extends `blokProviderWithoutRichText` by adding default renderers for all Storyblok rich text node types:

```kotlin
val provider = blokProvider(
    fallback = { component, modifier ->
        // Handle unknown components
    },
    storyLinkListener = { uuid, anchor ->
        // Handle story link clicks
    }
) {
    // Register your components here
}
```

### Story link handling

When users click on links to other stories in rich text content, the `storyLinkListener` callback is invoked:

```kotlin
val provider = blokProvider(
    storyLinkListener = { uuid, anchor ->
        // Navigate to the linked story
        navigator.navigate("story/$uuid")
    }
) {
    // ...
}
```

> [!NOTE]
> If no `storyLinkListener` is provided, clicking a story link will throw a `TODO` exception.

## Supported rich text nodes

The provider includes default renderers for all Storyblok rich text node types:

| Node Type | Rendering |
|-----------|-----------|
| Document | `LazyColumn` with vertical spacing |
| Heading | `Text` with typography level based on heading level (1-6) |
| Paragraph | `Text` with `AnnotatedString` |
| Text | Inline text with mark styling |
| Bullet List | Bulleted list with proper indentation |
| Ordered List | Numbered list with proper indentation |
| List Item | List item with bullet/number |
| Blockquote | `Text` with padding |
| Code Block | `Surface` with monospace font |
| Image | `AsyncImage` (via Coil) |
| Horizontal Rule | `HorizontalDivider` |
| Table | `Column` with `Row` layout |
| Table Cell | `Box` with border and padding |
| Table Header | `Box` with gray background |
| Embedded Blok | Renders nested components via `Blok()` |
| Emoji | Inline emoji text |
| Hard Break | Line break |

### Text marks

The following text marks are supported:

| Mark | Styling |
|------|---------|
| Bold | `fontWeight = Bold` |
| Italic | `fontStyle = Italic` |
| Underline | `textDecoration = Underline` |
| Strike | `textDecoration = LineThrough` |
| Code | Monospace font with background |
| Subscript | `baselineShift = Subscript` |
| Superscript | `baselineShift = Superscript` |
| Link (URL) | Clickable URL annotation |
| Link (Email) | `mailto:` URL annotation |
| Link (Story) | Clickable with `storyLinkListener` callback |
| Link (Asset) | Clickable URL annotation |
| Highlight | Background color from CSS |
| TextStyle | Text color from CSS |

## Customizing rich text rendering

You can override any default renderer by registering your own using `richText`:

```kotlin
val provider = blokProvider {
    // Override the heading renderer
    richText<RichText.Heading> { heading, modifier ->
        Text(
            text = buildAnnotatedString { heading.content.forEach { RichText(it) } },
            modifier = modifier,
            style = when (heading.level) {
                1 -> MaterialTheme.typography.displayLarge
                2 -> MaterialTheme.typography.displayMedium
                else -> MaterialTheme.typography.displaySmall
            }
        )
    }
    
    // Your component registrations
    blok<Page> { page, modifier -> /* ... */ }
}
```

> [!TIP]
> Custom `richText` registrations take precedence over the default renderers.

## Material 3 theming

The rich text renderers use Material 3 theme values from `MaterialTheme`:

- **Typography**: Heading levels map to Material 3 typography styles
- **Color scheme**: Code blocks, highlights, and links use theme colors
- **Shapes**: Code blocks use `MaterialTheme.shapes.small`

Wrap your `Storyblok` composable in a `MaterialTheme` to apply your theme:

```kotlin
MaterialTheme(
    colorScheme = darkColorScheme(),
    typography = Typography(/* ... */)
) {
    Storyblok(
        accessToken = "YOUR_ACCESS_TOKEN",
        version = Draft,
        blokProvider = myBlokProvider
    ) {
        // Rich text will use your theme
    }
}
```

## CSS color support

The provider includes a CSS color parser for handling colors in rich text marks (highlights, text styles, table cells). Supported formats:

| Format | Example |
|--------|---------|
| Hex (3-digit) | `#F00` |
| Hex (6-digit) | `#FF0000` |
| Hex (8-digit, with alpha) | `#80FF0000` |
| RGB | `rgb(255, 0, 0)` |
| RGBA | `rgba(255, 0, 0, 0.5)` |
| Named colors | `red`, `blue`, `transparent`, etc. |

## Image loading

Images in rich text are loaded using [Coil 3](https://coil-kt.github.io/coil/). The module includes the following Coil dependencies:

- `coil-compose` - Compose integration
- `coil-network-ktor3` - Ktor 3 network fetcher  
- `coil-svg` - SVG support
- `coil-gif` - GIF support (Android only)

> [!TIP]
> You can configure Coil's `ImageLoader` in your application to customize caching, placeholder images, and more.

## Other resources

- You can find the full module reference at https://storyblok.github.io/storyblok-kotlin/storyblok-material3/index.html
- For general guidance on using the Storyblok Compose SDK, see the [SDK guide](../storyblok-compose/README.md#sdk-guide).

