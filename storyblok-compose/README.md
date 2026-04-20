# Storyblok Compose SDK

A Compose Multiplatform SDK for rendering Storyblok content in your app.

With out-of-the-box support for component mapping, rich text rendering, and reactive story updates.

# Getting Started
## Add SDK dependency

The Storyblok Compose SDK requires adding the `storyblok-compose` artifact in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.storyblok:storyblok-compose:0.2.0")
}
```

> [!TIP]
> For rich text rendering with Material 3 components, also add the [storyblok-material3](../storyblok-material3) module which provides default renderers for all rich text node types:
> ```kotlin
> dependencies {
>   implementation("com.storyblok:storyblok-compose:0.2.0")
>   implementation("com.storyblok:storyblok-material3:0.2.0")
> }
> ```

## Set up the Storyblok composable

Wrap your content in the [`Storyblok`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-storyblok.html) composable to provide access to the Storyblok client and blok rendering scope:

```kotlin
@Composable
fun App() {
    Storyblok(
        accessToken = "YOUR_ACCESS_TOKEN",
        version = Draft,
        blokProvider = myBlokProvider
    ) {
        // Your content here
    }
}
```

## Define a blok provider

A [`BlokProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose.provider/-blok-provider/index.html) maps your Storyblok components to Compose UI. Use `blokProvider` to create a provider:

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
            Text("By ${article.author}")
        }
    }
}
```

## Fetch and render stories

Inside the `Storyblok` scope, you have access to both the [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) and [`BlokScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-blok-scope/index.html):

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    blokProvider = myBlokProvider
) {
    val story by story<Page>("home").collectAsState(null)
    
    story?.let { Blok(it.content) }
}
```

# SDK Guide
## Creating a blok provider

The blok provider is the core concept of the SDK. It registers your Storyblok components and defines how they should be rendered as Compose UI.

### Basic provider setup

Use [`blokProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/blok-provider.html) to create a provider:

```kotlin
val provider = blokProvider {
    // Register components here
}
```
> [!NOTE]
> The [`blokProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/blok-provider.html) function is provided by the `storyblok-material3` module and comes with pre-configured Material 3 rich text renderers.
> If you want to use it without the Material 3 defaults, you can create a provider with [`blokProviderWithoutRichText`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose.provider/blok-provider-without-rich-text.html)

### Registering components

Register components using the `blok` function. The component class must extend [`Component`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn.schema/-component/index.html) and be annotated with `@Serializable` and `@SerialName`:

```kotlin
@Serializable
@SerialName("page")
class Page(
    val title: String,
    val body: List<Component>
) : Component()

val provider = blokProvider {
    blok<Page> { page, modifier ->
        Column(modifier) {
            Text(page.title)
            page.body.forEach { Blok(it) }
        }
    }
}
```

#### Headless components

You can register components without a composable renderer. This is useful for components that are only used as data containers or are rendered by their parent:

```kotlin
blokProvider {
    blok<Author>() // Registered for serialization only
    
    blok<Article> { article, modifier ->
        // Author is accessed as data, not rendered directly
        Text("By ${article.author.name}")
    }
}
```

#### Handling unknown components

By default, unknown components throw an exception. You can provide a custom fallback handler:

```kotlin
val provider = blokProvider(
    fallback = { component, modifier ->
        Text("Unknown component: ${component.component}", modifier)
    }
) {
    // ...
}
```

### Registering rich text renderers

If you want to customize how rich text content is rendered, you can register renderers for specific rich text node types using the `richText` function.

Rich text content can be rendered in two ways:

#### As standalone Compose UI

```kotlin
richText<RichText.Paragraph> { paragraph, modifier ->
    Text(
        text = buildAnnotatedString { /* ... */ },
        modifier = modifier
    )
}
```

#### Within an AnnotatedString.Builder

Use the builder variant for inline rendering within text:

```kotlin
richText<RichText.Text> { text ->
    text.marks.forEach { mark ->
        when (mark) {
            is RichText.Mark.Bold -> pushStyle(SpanStyle(fontWeight = Bold))
            is RichText.Mark.Italic -> pushStyle(SpanStyle(fontStyle = Italic))
            // ...
        }
    }
    append(text.text)
    text.marks.forEach { _ -> pop() }
}
```

#### Default rich text renderers

Use `defaultRichText` to register fallback renderers that won't override custom implementations:

```kotlin
blokProvider {
    // Custom heading renderer
    richText<RichText.Heading> { heading, modifier ->
        Text(/* custom styling */)
    }
    
    // Default for all other paragraphs
    defaultRichText<RichText.Paragraph> { paragraph, modifier ->
        Text(/* default styling */)
    }
}
```

> [!TIP]
> The [storyblok-material3](../storyblok-material3) module provides a `blokProvider` function with pre-configured default renderers for all rich text node types using Material 3 components.


## The BlokScope

Inside a blok provider's composables, you have access to [`BlokScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-blok-scope/index.html) which provides functions for rendering nested content:

### Rendering nested components

Use `Blok()` to render child components:

```kotlin
blok<Page> { page, modifier ->
    Column(modifier) {
        page.body.forEach { component ->
            Blok(component)
        }
    }
}
```

### Rendering rich text

Use `RichText()` to render rich text content:

```kotlin
blok<Article> { article, modifier ->
    Column(modifier) {
        Text(article.title)
        RichText(article.content, Modifier.fillMaxWidth())
    }
}
```

## The StoryblokScope

The [`StoryblokScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-storyblok-scope/index.html) combines access to both the [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) and [`BlokScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-blok-scope/index.html):

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    blokProvider = myBlokProvider
) {
    // Access StoryblokClient functions
    val story by story<Page>("home").collectAsState(null)
    
    // Access BlokScope functions
    story?.content?.let { Blok(it) }
}
```

### Fetching stories

Stories are fetched using the client's reactive [`story()`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/story.html) function which returns a `Flow`:

```kotlin
// By slug
val homeStory by story<Page>("home").collectAsState(null)

// By UUID  
val articleStory by story<Article>(Uuid.parse("...")).collectAsState(null)
```

## Configuration options

The `Storyblok` composable accepts the following configuration:

| Parameter | Description |
|-----------|-------------|
| `accessToken` | Your Storyblok access token |
| `version` | Content version (`Draft` or `Published`) |
| `language` | Language code for content retrieval |
| `fallbackLanguage` | Fallback language for untranslated fields |
| `cv` | Cache version timestamp |
| `blokProvider` | The blok provider for component rendering |

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Published,
    language = "en",
    fallbackLanguage = "de",
    cv = "1706094649",
    blokProvider = myBlokProvider
) {
    // ...
}
```

## Other resources

You can find the full SDK reference at https://storyblok.github.io/storyblok-kotlin/storyblok-compose/index.html

