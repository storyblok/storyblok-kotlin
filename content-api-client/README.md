# Storyblok Content Delivery API Client

A Kotlin Multiplatform client for Storyblok's [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) built on the [Ktor Client Plugin](https://github.com/storyblok/storyblok-kotlin/tree/main/ktor-client-storyblok).

With out-of-the-box support for reactive story fetching, automatic relation resolution, rich text parsing, and custom component serialization.

# Getting Started
## Add client dependency

The Content Delivery API Client requires adding the `content-api-client` artifact in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.storyblok:content-api-client:0.5.1")
}
```

> [!NOTE]
> The Content Delivery API Client uses Ktor under hood and depends on `ktor-client-storyblok`, you can learn more in the [Storyblok Ktor Client Plugin
Guide](/../ktor-client-storyblok/README.md#add-plugin-dependency).

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

## Fetch multiple stories

Use [`stories(...)`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/stories.html) for the [retrieve multiple stories](https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-multiple-stories) endpoint. It returns a [`Pager`](https://developer.android.com/reference/kotlin/androidx/paging/Pager), whose `flow` emits [`PagingData`](https://developer.android.com/reference/kotlin/androidx/paging/PagingData) so pages are fetched as they are needed rather than all at once:

```kotlin
client.stories<Article> {
    startsWith = "articles/"
}
```

See [Fetching multiple stories](#fetching-multiple-stories) for how to consume the result and how to narrow, sort and filter it.

# Client Guide
## Creating a client

The [`StoryblokClient`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client/index.html) companion object provides multiple factory functions for creating a client:

### Simple configuration

Configuring default parameters for all requests is as simple as passing them to the factory function:

```kotlin
val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    region = Region.USA,
    language = "en",
    fallbackLanguage = "de",
    cv = "1706094649"
)
```
> [!TIP]
> Learn more about these parameters in the [Storyblok Ktor Client Plugin Guide](/../ktor-client-storyblok/README.md#configuring-default-parameters-for-all-requests).

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
> Learn more about the `apiBuilder` parameters in the [Storyblok Ktor Client Plugin Guide](/../ktor-client-storyblok/README.md#plugin-configuration).

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

## Fetching multiple stories

`stories(...)` returns a `Pager<Int, Story<T>>`. The `content_type` parameter is derived from `T`, so a query typed to a registered component asks only for that component's stories; typing it to `Component` asks for all of them.

```kotlin
val articles: Pager<Int, Story<Article>> = client.stories<Article> {
    startsWith = "articles/"
}
```

### Consuming the result

In Compose, collect it with [`collectAsLazyPagingItems()`](https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectAsLazyPagingItems) from `androidx.paging:paging-compose`, which drives the loading as the list scrolls:

```kotlin
val articles = remember { client.stories<Article> { startsWith = "articles/" }.flow }
    .collectAsLazyPagingItems()

LazyColumn {
    items(articles.itemCount) { index ->
        articles[index]?.let { story -> Text(story.content.headline) }
    }
}
```

Outside Compose, `asItemSnapshotListFlow()` presents each update as a plain list:

```kotlin
val pager = client.stories<Article> { startsWith = "articles/" }

pager.flow
    .asItemSnapshotListFlow()
    .collect { snapshot -> println(snapshot.items.map { it.content.headline }) }
```

A snapshot flow presents the loaded window and re-emits as more arrives, but it cannot request anything itself — paging is driven by access hints, which a presenter such as `collectAsLazyPagingItems` sends as the list scrolls and a plain flow has no way to send. Drive it from the `Pager` instead:

```kotlin
pager.append()   // load the next page
pager.refresh()  // reload from the top
pager.retry()    // recover after a failed load
```

### Page size

The Paging [`PagingConfig`](https://developer.android.com/reference/kotlin/androidx/paging/PagingConfig)'s `pageSize` is sent as the API's `per_page`, and defaults to 25:

```kotlin
client.stories<Article>(PagingConfig(pageSize = 50)) { startsWith = "articles/" }
```

> [!TIP]
> The `stories` endpoint caps `per_page` at 100. A larger size is refused by the API rather than quietly reduced.

### Narrowing the result

Every documented parameter has a type-safe entry point:

```kotlin
client.stories<Article> {
    startsWith = "articles/"                 // starts_with
    searchTerm = "spaceship"                 // search_term
    isStartpage = false                      // is_startpage
    bySlugs = listOf("articles/*")           // by_slugs
    excludingSlugs = listOf("articles/wip-*")// excluding_slugs
    byUuids = listOf(uuid)                   // by_uuids
    byUuidsOrdered = listOf(uuid)            // by_uuids_ordered, in the order given
    excludingIds = listOf(1L, 2L)            // excluding_ids
    withTag = listOf("featured")             // with_tag
    excludingFields = listOf("body")         // excluding_fields
    fromRelease = "12345"                    // from_release

    publishedAtGreaterThan = Instant.parse("2024-01-01T00:00:00Z")
    updatedAtLessThan = Instant.parse("2025-01-01T00:00:00Z")
}
```

### Sorting

Content fields are named by a property reference, which resolves to the serialized field name and adds the `content.` prefix the API expects. `Story` attributes are named the same way:

```kotlin
client.stories<Article> {
    sortBy(Article::headline)                   // sort_by=content.headline:asc
    sortByDescending(Story<*>::publishedAt)     // sort_by=published_at:desc
}
```

A numeric field is ordered numerically and everything else as text, which is also the right ordering for an ISO-8601 date. Storyblok serializes a number field as text, though, so a field holding a number may be modelled as a `String` — its type cannot settle the ordering, and a second argument states it:

```kotlin
client.stories<Product> {
    sortBy(Product::price, SortAs.WholeNumber)  // sort_by=content.price:asc:int
}
```

> [!NOTE]
> Only some `Story` attributes are sortable. The API answers the rest with `Not sortable by this column` rather than the SDK second-guessing which those are.

### Filter queries

[Filter queries](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries) are written with an infix DSL, addressing fields by property reference:

```kotlin
client.stories<Product> {
    filter {
        Product::headline like "*space*"
        Product::headline notLike "*draft*"
        Product::categories `is` Is.NotEmptyArray
        Product::headline isIn listOf("Spaceship", "Rocket")
        Product::headline notIn listOf("Paper")
        Product::categories allIn listOf("solar-system", "mars")
        Product::categories anyIn listOf("solar-system", "mars")
        Product::stock greaterThan 10          // Long    -> gt_int
        Product::rating lessThan 4.5           // Double  -> lt_float
        Product::releaseDate greaterThan Instant.parse("2024-01-01T00:00:00Z")
    }
}
```

The comparison operations pick their wire operation from the operand's type, and check the field can hold it — a decimal comparison against a whole-number field is reported rather than silently matching nothing. `allIn`/`anyIn` likewise require a multi-value field, where `isIn` is the operation that applies to a single-value one.

> [!NOTE]
> A property reference addresses one of the component's own fields. [Nested blocks and fields](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/nested-blocks-and-fields), which the API addresses with a dotted path, have no entry point — write those out with `parameter(...)`.

### Parameters the SDK does not know about

`parameter(name, value)` sets any query parameter directly, both for forward compatibility and to override one the SDK sends on your behalf:

```kotlin
client.stories<Article> {
    parameter("filter_query[seo.description][is]", "not_empty")
    parameter("some_future_param", "value")
}
```

> [!WARNING]
> Overriding `page`, `per_page`, `resolve_relations`, `resolve_level` or `content_type` can break the features that rely on them — changing `per_page`, for instance, desynchronises the page numbers Paging requests from the ones the HTTP cache holds.

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

### Resolution depth

How deeply relations are resolved can be controlled per request with the `resolveLevel` parameter of the `story()` functions:

```kotlin
// Resolve direct relations (the default)
client.story<Page>("home")

// Also resolve the relations of the resolved stories
client.story<Page>("home", resolveLevel = 2)

// Disable relation resolution entirely
client.story<Page>("home", resolveLevel = 0)
```

- With `resolveLevel = 2` or higher, the [`resolve_level`](https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-a-single-story) parameter is included in API requests so that relations of the resolved stories are resolved as well.
- With `resolveLevel = 0`, no relations are resolved and no `resolve_relations` parameter is sent — model relation fields as `Uuid` (or `String`) to receive the raw UUIDs.

### Circular and unresolved relations

Relations that cannot be resolved within the configured level resolve to `null`, so such fields must be modelled as nullable stories:

```kotlin
@Serializable
@SerialName("article")
class Article(
    val relatedArticle: Story<Article>?  // may reference this article back
) : Component()
```

This includes circular relations — stories referencing each other directly or indirectly — which are cut once the level is exhausted. If an unresolvable relation field is not nullable, decoding fails with a `SerializationException` naming the unresolved story's UUID.

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
| Embedded Block | `RichText.Block` |
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

The client leverages the underlying [ktor-client-storyblok](../ktor-client-storyblok/README.md) plugin's caching mechanism. When fetching stories:

1. The client first attempts to retrieve from cache (with `only-if-cached` header)
2. Then makes a network request to get the latest version
3. Emits distinct values, so cached and fresh responses are deduplicated if identical

This provides a "stale-while-revalidate" pattern for optimal user experience.

### Where the cache is stored

The client configures the cache with file-backed storage where the platform supports it, so it survives process restarts. Only the Content Delivery API is cached to disk; the Management API is not.

| Platform         | Storage                                  | Survives process restart      |
|------------------|------------------------------------------|-------------------------------|
| Android          | `${java.io.tmpdir}/storyblok-http-cache` | Yes                           |
| JVM              | `${java.io.tmpdir}/storyblok-http-cache` | Until the OS clears temp files |
| Native, JS, Wasm | In-memory, per client                    | No                            |

The cache directory is derived from the `java.io.tmpdir` system property, on **Android** the platform points `java.io.tmpdir` at the app's cache directory, so this resolves to `context.cacheDir`.

## Error handling

API errors are wrapped in [`StoryblokClientException`](https://storyblok.github.io/storyblok-kotlin/content-api-client/com.storyblok.cdn/-storyblok-client-exception/index.html):

```kotlin
client.story<Page>("non-existent")
    .catch { e: StoryblokClientException ->
        println("Failed to fetch story: ${e.message}")
    }
    .collect { story -> /* handle story */ }
```

> [!NOTE]
> Deserialization failures are **not** wrapped: they surface as `SerializationException`, since a mismatch between your
> component definitions and the content is a modelling error rather than a transient failure worth retrying.

## Closing the client

When you're done using the client, close it to release resources:

```kotlin
client.close()
```

## Other resources

- You can find the full client reference at https://storyblok.github.io/storyblok-kotlin/content-api-client/index.html
- For details on the Ktor plugin, see the [Storyblok Ktor Client Plugin Guide](/../ktor-client-storyblok/README.md#plugin-guide).
