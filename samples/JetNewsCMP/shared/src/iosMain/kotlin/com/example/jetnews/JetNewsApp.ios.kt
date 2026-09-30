package com.example.jetnews

import androidx.compose.runtime.Composable
import com.storyblok.ktor.Api.Config.Version
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * Let debug builds of the iOS app fetch draft content from Storyblok,
 * while release builds fetch published content.
 */
@OptIn(ExperimentalNativeApi::class)
internal actual val contentVersion: Version
    @Composable get() = if (Platform.isDebugBinary) Version.Draft else Version.Published
