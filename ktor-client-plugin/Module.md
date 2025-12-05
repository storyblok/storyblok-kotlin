# Module Storyblok Ktor Client Plugin

A custom client plugin to simplify calling Storyblok's [Content Delivery](https://www.storyblok.com/docs/api/content-delivery/v2) and [Management](https://www.storyblok.com/docs/api/management) APIs with the Ktor HTTP client.

With out-of-the-box support for authentication, regions, cache invalidation, error and rate limit handling, and more.

## Quick start

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:ktor-client-storyblok:0.1.0")
}
```
#### Then create an HttpClient, install the plugin, and start making requests to the Content Delivery API:

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) { accessToken = "ask9soUkv02QqbZgmZdeDAtt" }
}

val response = client.get("stories/posts/my-third-post")

println(response.body<JsonElement>())
```

#### Or requests to the Management API:

```kotlin
val client = HttpClient {
    install(Storyblok(MAPI)) {
        accessToken = OAuth("YOUR_OAUTH_TOKEN")
    }
}

val response = client.post("spaces/") {
    setBody(buildJsonObject {
        putJsonObject("space") {
            put("name", "Example Space")
        }
    })
}

println(response.body<JsonElement>())
```

## Other resources

You can find the full guide to the Storyblok Ktor Client Plugin inside [README.md](https://github.com/storyblok/storyblok-kotlin/blob/main/ktor-client-plugin/README.md#detailed-guide).
