# Examples

This module contains example Kotlin code snippets from [Storyblok's docs site](https://www.storyblok.com/docs) as executable unit tests for both the [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2) and the [Management API](https://www.storyblok.com/docs/api/management).

> [!TIP]
> You can run individual tests classes and functions via the play icon in the gutter in IntelliJ or Android Studio.

## 📦 Packages

This module consists of the the following packages:

| Package                                                  | Description                                                                         | Run all tests (JVM platform)                            |
|----------------------------------------------------------|-------------------------------------------------------------------------------------|---------------------------------------------------------|
| [ktorplugin.cdn](src/commonTest/kotlin/ktorplugin/cdn)   | Examples calling the Content Delivery API via Ktor Client with the Storyblok plugin | `./gradlew :examples:jvmTest --tests ktorplugin.cdn.*`  |
| [ktorplugin.mapi](src/commonTest/kotlin/ktorplugin/mapi) | Examples calling the Management API via Ktor Client with the Storyblok plugin       | `./gradlew :examples:jvmTest --tests ktorplugin.mapi.*` |


