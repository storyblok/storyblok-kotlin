# JetNews Sample App

A sample Android app demonstrating how to integrate Storyblok with Jetpack Compose using the Storyblok Kotlin SDK.

This sample is based on Google's [JetNews](https://github.com/android/compose-samples/tree/main/JetNews) sample app from the official Compose Samples, reimagined to pull content from Storyblok instead of using hardcoded data.

<p align="center">
  <img src="https://github.com/storyblok/storyblok-kotlin/raw/main/samples/JetNews/screenshots/feed.png" width="300" alt="JetNews Feed Screen"/>
  <img src="https://github.com/storyblok/storyblok-kotlin/raw/main/samples/JetNews/screenshots/article.png" width="300" alt="JetNews Article Screen"/>
</p>

## Features

- **Feed screen** - Displays a dynamic feed with highlighted, popular, recent, and recommended posts
- **Article screen** - Full article view with rich text content, images, and author information
- **Pull-to-refresh** - Refresh content from Storyblok
- **Navigation** - Navigate between the feed and articles using Navigation 3
- **Draft/Published** - Automatically switches between draft and published content based on build type

## Getting Started

### Prerequisites

- Android Studio Ladybug or later
- Android SDK 36
- A Storyblok space (or use the pre-configured demo space)

### Running the app

1. Open the `samples/JetNews` folder in Android Studio
2. Sync the Gradle project
3. Run the app on an emulator or device

The app is pre-configured with a demo Storyblok space. To use your own content, update the `accessToken` in `MainActivity.kt`.

## Project Structure

```
app/src/main/java/com/example/jetnews/
├── MainActivity.kt          # Main activity with Storyblok integration
├── NavKey.kt                 # Navigation key definitions
├── model/
│   ├── Feed.kt               # Feed-related component models
│   └── Post.kt               # Post and author component models
└── ui/
    ├── PostCardTop.kt        # Featured post card
    ├── PostCardSimple.kt     # Simple post card with favorite toggle
    ├── PostCardPopular.kt    # Horizontal scrolling popular post card
    ├── PostCardHistory.kt    # History/recommended post card
    ├── PostHeaderImage.kt    # Article header image
    ├── PostMetadata.kt       # Author and read time display
    └── theme/                # Material 3 theme configuration
```

## Storyblok Integration

### Setting up the Storyblok composable

The app wraps its content in the `Storyblok` composable with a `blokProvider`:

```kotlin
Storyblok(
    accessToken = "YOUR_ACCESS_TOKEN",
    version = if (BuildConfig.DEBUG) Draft else Published,
    blokProvider = blokProvider(
        storyLinkListener = { uuid, _ -> backStack.add(StoryKey(uuid = uuid)) }
    ) {
        // Component registrations
    }
) {
    // App content
}
```

### Defining components

Components are defined as Kotlin data classes extending `Component`:

```kotlin
@Serializable
@SerialName("post")
data class Post(
    val title: String,
    val subtitle: String? = null,
    val image: Asset,
    val body: RichText,
    val author: Story<Author>,
    val readTimeMinutes: Int
) : Component()
```

### Registering bloks

Each component is registered with a composable renderer:

```kotlin
blok<Post> { post, modifier ->
    CompositionLocalProvider(LocalPost provides post) {
        RichText(post.body, modifier.padding(16.dp))
    }
}

blok<Feed> { page, modifier ->
    LazyColumn(modifier) {
        items(page.body, key = { it.uid }) { 
            Blok(it, Modifier.fillMaxWidth()) 
        }
    }
}
```

### Fetching stories

Stories are fetched reactively using the `story()` function:

```kotlin
val story by story(slug = "home").collectAsStateWithLifecycle(null)

story?.let { 
    Blok(it.content)
}
```

## Content Structure in Storyblok

The demo space uses the following content types:

| Content Type | Description |
|--------------|-------------|
| `page` (Feed) | The home page with nested feed sections |
| `post` | Individual article with title, body, author, and metadata |
| `author` | Author information |

And the following nestable components:

| Component | Description |
|-----------|-------------|
| `highlighted` | Featured post at the top of the feed |
| `popular` | Horizontally scrolling popular posts section |
| `recent` | List of recent posts with favorite toggle |
| `recommended` | Recommended posts based on reading history |
| `header` | Optional custom header for articles |
| `metadata` | Author and read time display |

## Dependencies

The sample uses the following Storyblok dependencies:

```kotlin
implementation("com.storyblok:storyblok-compose-android:0.2.0")
implementation("com.storyblok:storyblok-material3-android:0.2.0")
```

## Learn More

- [Storyblok Kotlin SDK Documentation](https://storyblok.github.io/storyblok-kotlin/)
- [Storyblok Compose SDK](../../storyblok-compose)
- [Storyblok Material 3 Provider](../../storyblok-material3)
- [Storyblok Documentation](https://www.storyblok.com/docs)

