# Module Content Delivery API Client

A Kotlin Multiplatform client for Storyblok's [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) built on the [Ktor Client Plugin](https://github.com/storyblok/storyblok-kotlin/tree/main/ktor-client-storyblok).

With automatic relation resolution and custom component deserialization.

## Quick start

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:content-api-client:0.5.0")
}
```

#### Then create a client and fetch a story:

```kotlin
val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft
)

// client.story(...) returns a Flow that emits up-to two values:
// - the cached version (if available) 
// - the latest version from the API (if different from the cached version)
val myStory = client.story("home").first()
```

#### Define custom components for deserialization:

- Use `@SerialName` on the class to set the component's technical name. 
- Use `@JsonNames` on a field whose name in Storyblok differs from the Kotlin property

```kotlin
@Serializable
@SerialName("page")
class Page(
    val title: String,
    @JsonNames("hero_image") 
    val heroImage: Asset,
    val body: List<Component>
) : Component()

val client = StoryblokClient(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = Draft,
    serializersModule = SerializersModule {
        polymorphic(Component::class, Page::class, Page.serializer())
    }
)

client.story<Page>("home")
    .collect { story -> println(story.content.title) }
```

#### Fetch multiple stories:

`client.stories(...)` returns a `Pager`, whose `flow` emits pages as they are needed. The `content_type` parameter
comes from the component the query is typed to, and the query DSL narrows, sorts and filters the result:

```kotlin
val articles = client.stories<Page> {
    startsWith = "articles/"
    sortByDescending(Story<*>::publishedAt)
    filter { Page::title like "*space*" }
}
```

## Other resources

You can find the full guide to the Content Delivery API Client inside [README.md](https://github.com/storyblok/storyblok-kotlin/tree/main/content-api-client#client-guide).

