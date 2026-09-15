# JetNews Compose Multiplatform Sample

The [JetNews sample](../JetNews) rebuilt on Compose Multiplatform: one shared UI that runs on
**Android** and **iOS**, still backed by Storyblok through the Storyblok Kotlin SDK. A **web**
target is added for one purpose — previewing draft content in Storyblok's Visual Editor.

## Module layout

```
JetNewsCMP/
├── shared/       # KMP library: every screen, model and block in the app
│   └── src/
│       ├── commonMain/kotlin/          # all of the UI
│       ├── commonMain/composeResources/ # strings, icons and fonts for every platform
│       └── iosMain/kotlin/             # MainViewController(), called from Swift
├── androidApp/   # Android entry point: MainActivity, manifest, launcher icons
├── webApp/       # Wasm entry point: main(), index.html
└── iosApp/       # Xcode project that hosts the shared UI in a SwiftUI app
```

The split follows the Android Gradle Plugin 9 rule that a module cannot apply both the Kotlin
Multiplatform plugin and `com.android.application`: `shared` is a
`com.android.kotlin.multiplatform.library`, and each platform gets a thin entry-point module of
its own.

## Running

### Android

```bash
./gradlew :androidApp:installDebug
```

Or open the folder in Android Studio and run the `androidApp` configuration.

### iOS

```bash
open iosApp/iosApp.xcodeproj
```

Run the `iosApp` scheme. The project's *Compile Kotlin Framework* build phase calls
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`, so Xcode always links a fresh framework.

Set `TEAM_ID` in [`iosApp/Configuration/Config.xcconfig`](iosApp/Configuration/Config.xcconfig) to
run on a device; the simulator needs nothing.

Apple Silicon only. `storyblok-compose` publishes `iosArm64` and `iosSimulatorArm64` slices but no
`iosX64`, so the project excludes `x86_64` from simulator builds.

### Web

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Then open <http://localhost:8080>. `./gradlew :webApp:wasmJsBrowserDistribution` writes a
deployable bundle to `webApp/build/dist/wasmJs/productionExecutable`.

## What changed from the Android sample

The models ([`model/`](shared/src/commonMain/kotlin/com/example/jetnews/model)), the block library
([`BlockLibrary.kt`](shared/src/commonMain/kotlin/com/example/jetnews/BlockLibrary.kt)) and nearly
all of the UI are unchanged — they were already platform-agnostic Compose. The rest:

| Android sample | Compose Multiplatform |
|---|---|
| `MainActivity` holds the UI | [`JetNewsApp()`](shared/src/commonMain/kotlin/com/example/jetnews/JetNewsApp.kt) in `commonMain`; all three platform entry points just call it |
| `BuildConfig.DEBUG` picks draft vs published | an `expect val contentVersion` in `JetNewsApp.kt`. The web target's actual is `Draft`, because it is the one embedded in Storyblok's Visual Editor; Android and iOS share an `appMain` source set whose actual is `Published` |
| — | an `expect val initialStoryKey`, also in `JetNewsApp.kt`. The web target derives it from `window.location.pathname`, so a Visual Editor preview URL such as `/post6` opens that story; the app targets start at the home story |
| — | nothing: on the web target `story()` keeps emitting while the page is inside the Visual Editor, so the preview re-renders as an author types. The flow therefore never completes there, which is why `state` stays `Loading` in the editor — harmless, since content renders as soon as the story arrives |
| Navigation 3 | unchanged, on JetBrains' `org.jetbrains.androidx.navigation3:navigation3-ui` — but the back stack now needs a [`SavedStateConfiguration`](shared/src/commonMain/kotlin/com/example/jetnews/NavKey.kt) (see below) |
| `R.string` / `R.drawable` / `R.font` | Compose Multiplatform resources under `commonMain/composeResources`, reached through the generated `Res` class |
| Dynamic color on Android 12+ | the JetNews palette everywhere — dynamic color has no counterpart off Android |
| `PlatformTextStyle(includeFontPadding = false)` | dropped; font padding is an Android text-layout quirk |
| Fonts as top-level `val`s | `JetnewsTypography` is a `@Composable get()` property, because loading a font resource is a composable read |

Coil also needs its network fetcher registered by hand off the JVM, which `JetNewsApp` does; the
Storyblok client already supplies a Ktor engine for every target.

### Navigation 3 off Android

`androidx.navigation3:navigation3-runtime` is already multiplatform; only the UI half needs
JetBrains' build, `org.jetbrains.androidx.navigation3:navigation3-ui`. `NavDisplay`,
`entryProvider` and the `NavKey`/`NavBackStack` types are then used exactly as in the Android
sample.

The one difference is saving the back stack. Navigation 3 finds a key's serializer by reflection on
Android, which iOS and web don't have, so every destination has to be registered as a polymorphic
subclass of `NavKey` and handed to `rememberNavBackStack` as a `SavedStateConfiguration`. JetNews
has one destination, so that is
[`NavConfiguration`](shared/src/commonMain/kotlin/com/example/jetnews/NavKey.kt).

## Learn more

- [Storyblok Kotlin SDK documentation](https://storyblok.github.io/storyblok-kotlin/)
- [The original Android JetNews sample](../JetNews) — content model, component registration and
  Storyblok setup are documented there
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
