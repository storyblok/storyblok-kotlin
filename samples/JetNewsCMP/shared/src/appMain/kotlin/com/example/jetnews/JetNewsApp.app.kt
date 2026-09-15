package com.example.jetnews

import com.storyblok.ktor.Api.Config.Version

/** Android and iOS are shipped apps, so they read published content. */
internal actual val contentVersion: Version = Version.Published

/** The app targets have no URL to read, so they start at the home story. */
internal actual val initialStoryKey: StoryKey = HomeKey
