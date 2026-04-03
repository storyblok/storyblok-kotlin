# <ins>Storyblok Ktor Client Plugin</ins>

A custom client plugin to simplify calling Storyblok's Content Delivery and Management APIs with the Ktor HTTP client.

With out-of-the-box support for authentication, regions, cache invalidation, error and rate limit handling, and more.

# Getting Started
## Add plugin dependency

The Storyblok plugin requires adding the `ktor-client-storyblok` artifact in your `build.gradle.kts`:


```kotlin
dependencies {
    implementation("com.storyblok:ktor-client-storyblok:0.2.0")
}
```

> [!NOTE]
> The plugin ships with the Ktor CIO engine (`io.ktor:ktor-client-cio`) as a dependency so it works out of the box with JVM and Android. If you want to use a different engine, you can exclude CIO in your `build.gradle.kts`:
> ```kotlin
> dependencies {
>     implementation("com.storyblok:ktor-client-storyblok:0.2.0") {
>         exclude(group = "io.ktor", module = "ktor-client-cio")
>     }
> }
> ```

## Install the plugin

To install the plugin, you need to pass it to the [`install`](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client-config/install.html) function inside your [client configuration block](https://ktor.io/docs/client-create-and-configure.html#configure-client), like this:

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) {
        accessToken = "YOUR_ACCESS_TOKEN"
    }
}
```

API requests must be authenticated by providing an API access token. Learn more in the [Access Tokens concept](https://www.storyblok.com/docs/concepts/access-tokens).

## Make a request

The plugin configures the client's base URL so you can just pass the relative path of the endpoint you want to call:

```kotlin
val response = client.get("stories") {
    url {
        parameters.append("starts_with", "articles")
        parameters.append("search_term", "mars")
    }
}
```

# Plugin Guide
## Installation

To install the plugin invoke [`Storyblok(api: Api)`](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-storyblok.html) and pass it to the [`install`](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client-config/install.html) function inside your [client configuration block](https://ktor.io/docs/client-create-and-configure.html#configure-client).
The [`Api`](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/index.html) argument [configures](#client-configuration) the `HttpClient` for calling either the [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) ([`Api.CDN`](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-c-d-n/index.html)) or the [Management API](https://www.storyblok.com/docs/api/management) ([`Api.MAPI`](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-m-a-p-i/index.html)). 
> [!NOTE]
> Only a single Storyblok plugin can be installed in any one `HttpClient` instance.

## Plugin configuration

The [`install`](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client-config/install.html) function also takes the [plugin configuration block](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/index.html) as the second argument, in the block you can customize the plugin configuration further:

### Authentication

As API requests must be authenticated, you'll need to provide an access token in the configuration block. The Content Delivery API requires a read-only access token, whilst the Management API requires either an [OAuth](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-management/-access-token/-o-auth/index.html) or [personal](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-management/-access-token/-personal/index.html) access token. You can learn more about authentication in the [Access Tokens concept](https://www.storyblok.com/docs/concepts/access-tokens).

#### Content Delivery API

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) {
        accessToken = "YOUR_ACCESS_TOKEN"
    }
}
```
#### Management API

```kotlin
val client = HttpClient {
    install(Storyblok(MAPI)) {
        accessToken = OAuth("YOUR_OAUTH_ACCESS_TOKEN")
        //...or...
        accessToken = Personal("YOUR_PERSONAL_ACESSS_TOKEN")
    }
}
```
### Specifying a region

By default, the plugin uses the [EU](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-region/-e-u/index.html) region, if your space is located in a [different region](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-region/index.html) you can set it in the configuration block:

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) {
        region = USA
    }
}

```
#### Custom region

You can also specify a [custom region](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-region/-custom/index.html) by providing a custom base URL:

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) {
        region = Custom("https://app.storyblokchina.cn/cdn")
    }
}
```

### Rate limit handling

The Content Delivery and Management APIs have different rate limits depending on the [type of request](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/rate-limit) and your [pricing plan](https://www.storyblok.com/pricing/technical-limits), these are expressed in requests per second.

The plugin implements *API throttling* to slow down the API requests by introducing intermediate delays. You can specify the [maximum number of requests per second allowed](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/requests-per-second.html) in the configuration block:

```kotlin
val client = HttpClient {
    install(Storyblok(MAPI)) {
        requestsPerSecond = 3
    }
}
```

> [!NOTE] 
> The value of [`requestsPerSecond`](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/requests-per-second.html) defaults to `1000` for the Content Delivery API and `6` for the Management API.

> [!TIP]
> As the rate limit can differ on the type of request, and you can only configure `requestsPerSecond` per `HttpClient` instance, use [HttpClient.config](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client/config.html) to clone your client when you need to make requests with a different rate limit: 
> ```kotlin
> // create a new client with a lower rate limit for listings
> val listingsClient = client.config {
>     install(Storyblok(CDN)) {
>         requestsPerSecond = 15
>     }
> }
> ```
> Be careful when using multiple clients concurrently as the requests sent to the API from their combined usage may still exceed the rate limit.

The plugin also implements a [retry mechanism](#retrying-failed-requests) along with an exponential backoff if the HTTP status `429 Too Many Requests` is received.

### Configuring default parameters for all requests

> [!IMPORTANT]
> Default parameters are only available for the Content Delivery API.

You can optionally [configure](https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/com.storyblok.ktor/-api/-config/-content/index.html) default parameters that are applied to all requests:

```kotlin
val client = HttpClient {
    install(Storyblok(CDN)) {
        cv = "1706094649" //cached version Unix timestamp
        language = "en" //language to retrieve resources
        fallbackLanguage = "de" //language for untranslated fields
        version = Draft //the version of resources to retrieve
    }
}
```

#### CV parameter handling

By specifying a default value for the `cv` parameter you can retrieve a specific [cached version](ttps://www.storyblok.com/docs/api/content-delivery/v2/getting-started/cache-invalidation) of a published resource.

Otherwise, the plugin will automatically set the `cv` parameter to the latest version of the space after the first request to the Content Delivery API.

## Client configuration

Installing the plugin applies the following configuration to the `HttpClient` to simplify calling Storyblok's Content Delivery and Management APIs:
> [!TIP]
> You can override any part of this configuration inside your [client configuration block](https://ktor.io/docs/client-create-and-configure.html#configure-client) if needed.

### Response validation

The plugin sets the [`expectSuccess`](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client-config/expect-success.html?query=var%20expectSuccess:%20Boolean) property to `true` to throw exceptions for non-2xx responses.

### Retrying failed requests

The plugin installs the [`HttpRequestRetry`](https://ktor.io/docs/client-request-retry.html) plugin and configures 5 [retries](https://api.ktor.io/ktor-client-core/io.ktor.client.plugins/-http-request-retry-config/max-retries.html) with an [exponential delay](https://api.ktor.io/ktor-client-core/io.ktor.client.plugins/-http-request-retry-config/exponential-delay.html) for `Too Many Requests (429)` and `Server Errors (5xx)` responses.

> [!NOTE]
> When a request is retried the plugin applies the delay to all pending requests made with the same `HttpClient` instance. 

### Caching

> [!IMPORTANT]
> Caching is only implemented for the Content Delivery API.

Responses from the Content Delivery API contain the `Cache-Control` and `ETag` headers, to respect these, the plugin installs the [`HttpCache`](https://ktor.io/docs/client-caching.html) plugin and configures a shared in-memory cache.

This means subsequent requests for the same resource made with the same `HttpClient` instance will be served from the cache instead of performing a network request.

### Content negotiation and serialization

The plugin installs the [`ContentNegotiation`](https://ktor.io/docs/client-serialization.html) plugin and configures the [JSON serializer](https://ktor.io/docs/client-serialization.html#register_json). The JSON serializer is configured to [ignore unknown keys](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json-builder/ignore-unknown-keys.html) and the content type of request bodies is automatically set to `application/json`. 

This allows you to use a [`JsonElement`](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json-element/) or a custom class as the request or response body:

#### JsonElement serialization

```kotlin
val response = client.post("spaces/288868932106293/stories") {
    setBody(buildJsonObject {
        put("publish", 1)
        putJsonObject("story") {
            putJsonObject("content") {
                putJsonArray("body") {}
                put("component", "page")
            }
            put("name", "Story Name")
            put("slug", "story-name")
        }
    })
}

println("Story ${response.body<JsonObject>().getValue("story").jsonObject["name"]} created")
```

#### Custom class serialization

```kotlin
@Serializable
data class Content(val component: String, val body: List<String>)
@Serializable
data class Story(val name: String, val slug: String, val content: Content)
@Serializable
data class Body(val story: Story, val publish: Int = 0)

val response = client.post("spaces/288868932106293/stories") {
    setBody(Body(
        publish = 1,
        story = Story(
            content = Content(
                component = "page",
                body = emptyList()
            ),
            name = "Story Name", 
            slug = "story-name"
        )
    ))
}

println("Story ${response.body<Body>().story.name} created")
```

## Other resources

You can find the full plugin reference at https://storyblok.github.io/storyblok-kotlin/ktor-client-plugin/index.html
