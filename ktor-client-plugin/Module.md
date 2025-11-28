# Module Storyblok Ktor Client Plugin

A custom client plugin to simplify calling Storyblok's Content Delivery and Management APIs with the Ktor HTTP client.

With out-of-the-box support for authentication, regions, cache invalidation, error and rate limit handling, and more.

## Getting started

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:ktor-client-storyblok:0.1.0")
}
```
#### Then create an HttpClient, install the plugin, and start making requests to the [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2):

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) { accessToken = "ask9soUkv02QqbZgmZdeDAtt" }
}

val response = client.get("stories/posts/my-third-post")

println(response.body<JsonElement>())
```

#### Or requests to the [Management API](https://www.storyblok.com/docs/api/management):

```kotlin
val client = HttpClient {
    install(Storyblok(MAPI)) {
        accessToken = OAuth("YOUR_OAUTH_TOKEN")
    }
}

val response = client.post("spaces/") {
    contentType(ContentType.Application.Json)
    setBody("""{
      "space": {
        "name": "Example Space"
      }
    }""")
}

println(response.body<JsonElement>())
```

