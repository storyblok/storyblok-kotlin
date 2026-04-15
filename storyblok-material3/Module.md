# Module Material 3 Rich Text Provider

A customizable rich text blok provider for the [Compose SDK](https://github.com/storyblok/storyblok-kotlin/tree/main/storyblok-compose) using Material 3 components and theming.

## Quick start

#### First, add the dependency to your project:

```kotlin
dependencies {
    implementation("com.storyblok:storyblok-material3:0.2.0")
}
```

#### Create a blok provider with pre-configured Material 3 rich text renderers:

```kotlin
@Serializable
@SerialName("article")
class Article(val title: String, val content: RichText.Document) : Component()

val myBlokProvider = blokProvider(
    storyLinkListener = { uuid, anchor -> /* Handle story link clicks */ }
) {
    blok<Article> { article, modifier ->
        Column(modifier) {
            Text(article.title, style = MaterialTheme.typography.headlineLarge)
            RichText(article.content, Modifier.fillMaxWidth())
        }
    }
}
```

#### Use with the Storyblok composable inside a MaterialTheme:

```kotlin
@Composable
fun App() {
    MaterialTheme {
        Storyblok(
            accessToken = "YOUR_ACCESS_TOKEN",
            version = Draft,
            blokProvider = myBlokProvider
        ) {
            val story by story<Article>("articles/hello-world").collectAsState(null)
            
            story?.let { Blok(it.content) }
        }
    }
}
```

## Other resources

You can find the full guide to the Material 3 Rich Text Provider inside [README.md](https://github.com/storyblok/storyblok-kotlin/tree/main/storyblok-material3#provider-guide).

