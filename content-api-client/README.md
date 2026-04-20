# Storyblok Content Delivery API Client

A Kotlin Multiplatform client for Storyblok's [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) built on the [Ktor Client Plugin](https://github.com/storyblok/storyblok-kotlin/tree/main/ktor-client-plugin).

With out-of-the-box support for reactive story fetching, automatic relation resolution, rich text parsing, and custom component serialization.

# Getting Started
## Add client dependency

The Content Delivery API Client requires adding the `content-api-client` artifact in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.storyblok:content-api-client:0.2.0")
}
```

> [!NOTE]
> The Content Delivery API Client uses Ktor under hood and depends on `ktor-client-storyblok` you can learn more in the [Storyblok Ktor Client Plugin
Guide](/../ktor-client-storyblok#add-plugin-dependency).

## Create the client

To create a client, invoke the [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) factory method and provide your access token and preferred content version:

```kotlin
val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft
)
```

API requests must be authenticated by providing an API access token. Learn more in the [Access Tokens concept](https://www.storyblok.com/docs/concepts/access-tokens).

## Fetch a story

The client returns a [`Flow`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) that emits the story data. Stories can be fetched by slug or UUID:

```kotlin
// Fetch by slug
client.story("articles/hello-world")
    .collect { story -> println(story.name) }

// Fetch by UUID
client.story(Uuid.parse("bfea4895-8a19-4e82-ae1c-1c8f3e4b6f9c"))
    .collect { story -> println(story.name) }
```

# Client Guide
## Creating a client

The [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) companion object provides multiple factory functions for creating a client:

### Simple configuration

Configuring default parameters for all requests is as simple as passing them to the factory function:

```kotlin
val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    language = "en",
    fallbackLanguage = "de",
    cv = "1706094649"
)
```
> [!TIP]
> Learn more about these parameters in the [Storyblok Ktor Client Plugin Guide](/../ktor-client-storyblok#configuring-default-parameters-for-all-requests).

### Advanced configuration

For more control, you can provide custom builders for API configuration, serialization, and JSON parsing:

```kotlin
val client = StoryblokClient(
    apiBuilder = {
        accessToken = "YOUR_ACCESS_TOKEN"
        version = Published
        region = Region.USA
    },
    serializersModuleBuilder = {
        // Register custom component serializers
    },
    jsonBuilder = {
        ignoreUnknownKeys = true
    }
)
```
> [!TIP]
> Learn more about the `apiBuilder` parameters in the [Storyblok Ktor Client Plugin Guide](/../ktor-client-storyblok#plugin-configuration).

## Registering custom components

Storyblok components are deserialized into Kotlin classes. To use custom components, extend the [`Component`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn.schema/-component/index.html) class and register them with the client's serializers module:

```kotlin
@Serializable
@SerialName("page")
class Page(
    val title: String,
    val body: List<Component>
) : Component()

@Serializable
@SerialName("article")
class Article(
    val headline: String,
    val author: String,
    val content: RichText.Document
) : Component()

val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    serializersModule = SerializersModule {
        polymorphic(Component::class, Page::class, Page.serializer())
        polymorphic(Component::class, Article::class, Article.serializer())
    }
)
```

> [!NOTE]
> The `@SerialName` annotation must match the component's technical name in Storyblok.

### Fetching typed stories

Once components are registered, if you know the type of `Component` your story contains you can fetch stories using the reified extension function:

```kotlin
client.story<Page>("home")
    .collect { story -> println(story.content.title) }
```

### Unknown components

Components that are not registered will be deserialized as [`Component.Unknown`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn.schema/-component/-unknown/index.html). This allows you to handle unrecognized components gracefully without causing deserialization errors.

## Story relations

The client automatically resolves [story relations](https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-with-resolved-relations) based on your component definitions. When a component has a property of type `Story<T>`, the client will:

1. Detect the relation field from the serializers module
2. Include the appropriate `resolve_relations` parameter in API requests  
3. Resolve the related stories and deserialize them in place

```kotlin
@Serializable
@SerialName("featured")
class FeaturedArticle(
    val article: Story<Article>  // Automatically resolved
) : Component()

@Serializable
@SerialName("popular")
class PopularArticles(
    val articles: List<Story<Article>>  // Lists are also supported
) : Component()
```

> [!TIP]
> The maximum number of relations that can be resolved is 50 stories per request. This is a Storyblok API limitation.

## Rich text fields

Rich text content from Storyblok is deserialized into a structured [`RichText`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn.schema/-rich-text/index.html) sealed class hierarchy. This allows you to render rich text content in a type-safe manner:

```kotlin
@Serializable
@SerialName("article")
class Article(
    val content: RichText.Document
) : Component()
```

### Supported rich text nodes

| Node Type | Class |
|-----------|-------|
| Document | `RichText.Document` |
| Paragraph | `RichText.Paragraph` |
| Heading | `RichText.Heading` |
| Text | `RichText.Text` |
| Bold/Italic/etc | `RichText.Mark.*` |
| Bullet List | `RichText.BulletList` |
| Ordered List | `RichText.OrderedList` |
| List Item | `RichText.ListItem` |
| Blockquote | `RichText.Blockquote` |
| Code Block | `RichText.CodeBlock` |
| Image | `RichText.Image` |
| Link | `RichText.Mark.Link` |
| Horizontal Rule | `RichText.HorizontalRule` |
| Table | `RichText.Table` |
| Embedded Blok | `RichText.Blok` |
| Emoji | `RichText.Emoji` |
| Hard Break | `RichText.HardBreak` |

### Traversing rich text

Composite rich text nodes implement the `Composite` interface which provides a `flatten()` function to traverse all nested nodes:

```kotlin
val document: RichText.Document = article.content

document.flatten().filterIsInstance<RichText.Text>().forEach { text ->
    println(text.text)
}
```

## Field types

Common Storyblok field types are provided as sealed classes for type-safe deserialization:

### Link field

```kotlin
@Serializable
@SerialName("page")
class Page(
    val link: Link
) : Component()

// Access link properties
println(page.link.url)
println(page.link.linkType) // "url", "story", "email", etc.
```

### Asset field

```kotlin
@Serializable
@SerialName("page")
class Page(
    val image: Asset
) : Component()

// Access asset properties
println(page.image.filename)
println(page.image.alt)
```

## Caching

The client leverages the underlying [ktor-client-storyblok](../ktor-client-storyblok) plugin's caching mechanism. When fetching stories:

1. The client first attempts to retrieve from cache (with `only-if-cached` header)
2. Then makes a network request to get the latest version
3. Emits distinct values, so cached and fresh responses are deduplicated if identical

This provides a "stale-while-revalidate" pattern for optimal user experience.

## Error handling

API errors are wrapped in [`StoryblokClientException`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client-exception/index.html):

```kotlin
client.story<Page>("non-existent")
    .catch { e: StoryblokClientException ->
        println("Failed to fetch story: ${e.message}")
    }
    .collect { story -> /* handle story */ }
```

## Closing the client

When you're done using the client, close it to release resources:

```kotlin
client.close()
```

## Other resources

You can find the full client reference at https://storyblok.github.io/storyblok-kotlin/content-api-client/index.html

