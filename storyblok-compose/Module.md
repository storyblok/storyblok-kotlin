# Module Compose SDK

A Compose Multiplatform SDK for rendering Storyblok content in your app built on the [Content Delivery API Client](https://github.com/storyblok/storyblok-kotlin/tree/main/content-api-client).

With out-of-the-box support for component mapping, rich text rendering, and reactive story updates.

## Quick start

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:storyblok-compose:0.2.0")
}
```

#### Define your components and create a blok provider:

```kotlin
@Serializable
@SerialName("page")
class Page(val title: String, val body: List<Component>) : Component()

val myBlokProvider = blokProviderWithoutRichText {
    blok<Page> { page, modifier ->
        Column(modifier) {
            Text(page.title, style = MaterialTheme.typography.headlineLarge)
            page.body.forEach { Blok(it) }
        }
    }
}
```

#### Wrap your content in the Storyblok composable and fetch stories:

```kotlin
@Composable
fun App() {
    Storyblok(
        accessToken = "YOUR_ACCESS_TOKEN",
        version = Draft,
        blokProvider = myBlokProvider
    ) {
        val story by story<Page>("home").collectAsState(null)
        
        story?.let { Blok(it.content) }
    }
}
```

## Other resources

You can find the full guide to the Storyblok Compose SDK inside [README.md](https://github.com/storyblok/storyblok-kotlin/tree/main/storyblok-compose#sdk-guide).

