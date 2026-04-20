# Module Content Delivery API Client

A Kotlin Multiplatform client for Storyblok's [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) built on the [Ktor Client Plugin](https://github.com/storyblok/storyblok-kotlin/tree/main/ktor-client-plugin).

With automatic relation resolution and custom component deserialization.

## Quick start

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:content-api-client:0.2.0")
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

```kotlin
@Serializable
@SerialName("page")
class Page(
    val title: String,
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

## Other resources

You can find the full guide to the Content Delivery API Client inside [README.md](https://github.com/storyblok/storyblok-kotlin/tree/main/content-api-client#client-guide).

