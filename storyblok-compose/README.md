# Storyblok Compose SDK

A Compose Multiplatform SDK for rendering Storyblok content in your app.

With out-of-the-box support for component mapping, rich text rendering, and reactive story updates.

# Getting Started
## Add SDK dependency

The Storyblok Compose SDK requires adding the `storyblok-compose` artifact in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.storyblok:storyblok-compose:0.7.1")
}
```

> [!TIP]
> For rich text rendering with Material 3 components, also add the [storyblok-material3](../storyblok-material3) module which provides default renderers for all rich text node types:
> ```kotlin
> dependencies {
>   implementation("com.storyblok:storyblok-compose:0.7.1")
>   implementation("com.storyblok:storyblok-material3:0.7.1")
> }
> ```

## Set up the Storyblok composable

Wrap your content in the [`Storyblok`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-storyblok.html) composable to provide access to the Storyblok client and block rendering scope:

```kotlin
@Composable
fun App() {
    Storyblok(
        accessToken = "YOUR_ACCESS_TOKEN",
        version = Draft,
        blockProvider = myBlockProvider
    ) {
        // Your content here
    }
}
```

## Define a block provider

A [`BlockProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose.provider/-block-provider/index.html) maps your Storyblok components to Compose UI. Use `blockProvider` to create a provider:

```kotlin
val myBlockProvider = blockProvider {
    block<Page> { page, modifier ->
        Column(modifier) {
            Text(page.title, style = MaterialTheme.typography.headlineLarge)
            page.body.forEach { Block(it) }
        }
    }
    
    block<Article> { article, modifier ->
        Column(modifier) {
            Text(article.headline)
            Text("By ${article.author}")
        }
    }
}
```

## Fetch and render stories

Inside the `Storyblok` scope, you have access to both the [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) and [`BlockScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-block-scope/index.html):

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    blockProvider = myBlockProvider
) {
    val story by story<Page>("home").collectAsState(null)
    
    story?.let { Block(it.content) }
}
```

# SDK Guide
## Creating a block provider

The block provider is the core concept of the SDK. It registers your Storyblok components and defines how they should be rendered as Compose UI.

### Basic provider setup

Use [`blockProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/block-provider.html) to create a provider:

```kotlin
val provider = blockProvider {
    // Register components here
}
```
> [!NOTE]
> The [`blockProvider`](https://storyblok.github.io/storyblok-kotlin/storyblok-material3/com.storyblok.compose.provider/block-provider.html) function is provided by the `storyblok-material3` module and comes with pre-configured Material 3 rich text renderers.
> If you want to use it without the Material 3 defaults, you can create a provider with [`blockProviderWithoutRichText`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose.provider/block-provider-without-rich-text.html)

### Registering components

Register components using the `block` function. The component class must extend [`Component`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn.schema/-component/index.html) and be annotated with `@Serializable` and `@SerialName`:

```kotlin
@Serializable
@SerialName("page")
class Page(
    val title: String,
    val body: List<Component>
) : Component()

val provider = blockProvider {
    block<Page> { page, modifier ->
        Column(modifier) {
            Text(page.title)
            page.body.forEach { Block(it) }
        }
    }
}
```

#### Headless components

You can register components without a composable renderer. This is useful for components that are only used as data containers or are rendered by their parent:

```kotlin
blockProvider {
    block<Author>() // Registered for serialization only
    
    block<Article> { article, modifier ->
        // Author is accessed as data, not rendered directly
        Text("By ${article.author.name}")
    }
}
```

#### Handling unknown components

By default, unknown components throw an exception. You can provide a custom fallback handler:

```kotlin
val provider = blockProvider(
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
blockProvider {
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
> The [storyblok-material3](../storyblok-material3) module provides a `blockProvider` function with pre-configured default renderers for all rich text node types using Material 3 components.


## The BlockScope

Inside a block provider's composables, you have access to [`BlockScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-block-scope/index.html) which provides functions for rendering nested content:

### Rendering nested components

Use `Block()` to render child components:

```kotlin
block<Page> { page, modifier ->
    Column(modifier) {
        page.body.forEach { component ->
            Block(component)
        }
    }
}
```

To render a list of blocks in a `LazyColumn` or `LazyRow`, use `blocks()`, which also lets the list follow the
selection in the Visual Editor (see [Blocks in a lazy list](#blocks-in-a-lazy-list)):

```kotlin
block<Page> { page, modifier ->
    val state = rememberLazyListState()
    LazyColumn(modifier, state = state) {
        blocks(page.body, state)
    }
}
```

### Rendering rich text

Use `RichText()` to render rich text content:

```kotlin
block<Article> { article, modifier ->
    Column(modifier) {
        Text(article.title)
        RichText(article.content, Modifier.fillMaxWidth())
    }
}
```

## The StoryblokScope

The [`StoryblokScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-storyblok-scope/index.html) combines access to both the [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) and [`BlockScope`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-block-scope/index.html):

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    blockProvider = myBlockProvider
) {
    // Access StoryblokClient functions
    val story by story<Page>("home").collectAsState(null)
    
    // Access BlockScope functions
    story?.content?.let { Block(it) }
}
```

### Fetching stories

Stories are fetched using the client's reactive [`story()`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/story.html) function, which returns a `Flow` that emits first from cache (if present), then from the network (once connected):

```kotlin
// By slug
val homeStory by story<Page>("home").collectAsState(null)

// By UUID  
val articleStory by story<Article>(Uuid.parse("...")).collectAsState(null)
```

The `story()` functions also accept a `resolveLevel` parameter to control how deeply story relations are resolved — see [Story relations](../content-api-client/README.md#story-relations) in the Content Delivery API Client guide.

## Visual Editor preview

When a [Compose Multiplatform for Web](https://lp.jetbrains.com/cmp-for-web/) (`wasmJs`) build is previewed in Storyblok's
[Visual Editor](https://www.storyblok.com/docs/concepts/visual-editor), every block drawn with `Block()` becomes part of the
editing experience, with nothing to configure:

- **Tapping a block opens its form in the editor.** A tap on a nested block opens the innermost one under it. The first tap
  on a block only selects it, so tapping a card that navigates opens the card instead of following it. A second tap does
  what the block does.
- **The selected block is outlined**, and when the author picks one in the editor's form, or adds, moves or duplicates one,
  it is scrolled into view.
- **The preview follows the editor's language.** Content is requested in the language the author picks in the editor, in
  place of the `language` passed to `Storyblok(...)`, so field-level translations show as they are edited. The space's
  default language keeps the app's own.

For this to work, a block's composable must apply the `Modifier` it is given to its outermost layout, as in the examples
above. Outside the editor, and on every other target, blocks behave exactly as before.

### Blocks in a lazy list

A lazy list only composes the items on or near the screen, so a block selected further down has nothing on screen to
scroll into view. Add blocks to a `LazyColumn` or `LazyRow` with
[`blocks()`](https://storyblok.github.io/storyblok-kotlin/storyblok-compose/com.storyblok.compose/-block-scope/blocks.html),
passing the same state the list was given, and the list scrolls to the item holding the selected block itself:

```kotlin
block<Page> { page, modifier ->
    val state = rememberLazyListState()
    LazyColumn(modifier, state = state) {
        item { Text(page.title) }
        blocks(page.body, state)
    }
}
```

This works for a block nested anywhere inside an item, as long as the editor says which blocks it is nested in. It does
when the author picks a block in the form, but not when they add, move or duplicate one inside another. The list
follows each selection once, while at least one of the blocks is on screen, so the author can still scroll away.

`blocks()` keys each item by the block's `_uid`, so a block keeps its state when the author moves it. Outside the
editor it simply adds the blocks.

## Configuration options

The `Storyblok` composable accepts the following configuration:

| Parameter | Description |
|-----------|-------------|
| `accessToken` | Your Storyblok access token |
| `version` | Content version (`Draft` or `Published`) |
| `region` | Server location of your space (`EU`, `USA`, `CAN`, `AUS`, `CHN`, or `Custom`). Defaults to `EU` |
| `language` | Language code for content retrieval. In the Visual Editor, the language the author picks takes its place |
| `fallbackLanguage` | Fallback language for untranslated fields |
| `cv` | Cache version timestamp |
| `blockProvider` | The block provider for component rendering |

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Published,
    region = USA,
    language = "en",
    fallbackLanguage = "de",
    cv = "1706094649",
    blockProvider = myBlockProvider
) {
    // ...
}
```

## Other resources

- You can find the full SDK reference at https://storyblok.github.io/storyblok-kotlin/storyblok-compose/index.html
- For details on the API client, see the [Content Delivery API Client Guide](../content-api-client/README.md#client-guide).
- Check out the [JetNews sample app](../samples/JetNews/README.md) for a complete example of the SDK in action 
