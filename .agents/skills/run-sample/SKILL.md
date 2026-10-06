---
name: run-sample
description:
  Use when asked to run, build, or try a sample app (JetNews, JetNewsCMP), or to check an SDK change
  in a real app on Android, iOS, or the web
---

# Run a Sample App

Run a sample for: $ARGUMENTS

The samples in `samples/` are **separate Gradle builds**. They depend on the SDK by Maven
coordinates (`com.storyblok:*`, version `storyblok` in each sample's `gradle/libs.versions.toml`)
and resolve it from Maven Central first, then `mavenLocal()`.

| Sample       | Platforms                              | SDK artifacts used                                           |
| ------------ | -------------------------------------- | ------------------------------------------------------------ |
| `JetNews`    | Android                                | `storyblok-compose-android`, `storyblok-material3-android`   |
| `JetNewsCMP` | Android, iOS, web (`wasmJs`, preview)  | `storyblok-compose`, `storyblok-material3`                   |

## Step 1: Choose the SDK source

- **Published SDK** (no local changes to test): skip to step 3.
- **Local SDK changes**: do step 2. Maven Central wins over `mavenLocal()`, so publishing locally
  under a version that's already released does nothing. Use a version that isn't on Central.

## Step 2: Publish the SDK to Maven Local

Use a local-only version in **both** the root and the sample's `gradle/libs.versions.toml`. Both
files must have no uncommitted changes first, because the cleanup below resets them. If either has
changes, stop and ask the user rather than overwriting their work:

```bash
LOCAL=0.0.0-local
if git diff --quiet -- gradle/libs.versions.toml samples/<Sample>/gradle/libs.versions.toml; then
  sed -i '' "s/^storyblok-kotlin = .*/storyblok-kotlin = \"$LOCAL\"/" gradle/libs.versions.toml
  sed -i '' "s/^storyblok = .*/storyblok = \"$LOCAL\"/" samples/<Sample>/gradle/libs.versions.toml
else
  echo "STOP: the version catalogs have uncommitted changes"
fi
```

(On Linux, use `sed -i` without `''`.)

Publish only the publications the sample needs. Publishing everything also compiles every native
target, which is much slower. Signing is skipped automatically for Maven Local.

```bash
MODULES="ktor-client-storyblok content-api-client storyblok-compose storyblok-material3"

# JetNews, or JetNewsCMP on Android (~20s warm)
./gradlew $(for m in $MODULES; do printf ':%s:publishKotlinMultiplatformPublicationToMavenLocal :%s:publishAndroidPublicationToMavenLocal ' $m $m; done)

# JetNewsCMP on iOS: add the iOS slices
./gradlew $(for m in $MODULES; do printf ':%s:publishIosArm64PublicationToMavenLocal :%s:publishIosSimulatorArm64PublicationToMavenLocal ' $m $m; done)

# JetNewsCMP on the web: add wasmJs
./gradlew $(for m in $MODULES; do printf ':%s:publishWasmJsPublicationToMavenLocal ' $m; done)
```

Check that the sample resolves it:

```bash
cd samples/JetNews && ./gradlew -q :app:dependencies --configuration debugRuntimeClasspath | grep com.storyblok
```

**Before committing anything, revert both version edits.** The check above makes this safe: the
only changes in these files are the two version lines.

```bash
git checkout -- gradle/libs.versions.toml samples/<Sample>/gradle/libs.versions.toml
```

## Step 3: Build and run

The samples need an Android SDK. Copy `local.properties` from the repo root if the sample has
none. Run Gradle from the sample's directory.

### JetNews (Android)

```bash
cd samples/JetNews
./gradlew :app:assembleDebug                 # build only
./gradlew :app:installDebug                  # needs a device or emulator (adb devices)
adb shell am start -n com.example.jetnews/.MainActivity
```

### JetNewsCMP

```bash
cd samples/JetNewsCMP

# Android
./gradlew :androidApp:installDebug
adb shell am start -n com.example.jetnews/.MainActivity

# iOS (Apple Silicon only, no iosX64). The Xcode build phase runs embedAndSignAppleFrameworkForXcode.
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' -derivedDataPath build/ios build

# Web (Visual Editor preview). Needs a TLS certificate in samples/JetNewsCMP first:
#   mkcert -install && mkcert -cert-file localhost.pem -key-file localhost-key.pem localhost 127.0.0.1
./gradlew :webApp:wasmJsBrowserDevelopmentRun      # serves https://localhost:8080
```

- Long-running commands (`installDebug` on a slow emulator, the web dev server) go in the
  background. Hand the user the URL or the device to look at.
- `JetNewsCMP` debug builds read **draft** content. Release builds read published content.
- Both samples use a demo space token. To use another space, change `accessToken` in
  `JetNews/app/src/main/java/com/example/jetnews/MainActivity.kt` or
  `JetNewsCMP/shared/src/commonMain/kotlin/com/example/jetnews/JetNewsApp.kt`. Don't commit
  someone else's token.
- `JetNewsCMP` builds with Kotlin 2.4.x while the SDK itself is held at 2.3.x (KT-89275).

## Step 4: Report

Say which sample, platform and SDK source (published version or local build) ran, what you
checked, and anything that looked wrong, with screenshots or logs (`adb logcat`, the browser
console) where useful.
