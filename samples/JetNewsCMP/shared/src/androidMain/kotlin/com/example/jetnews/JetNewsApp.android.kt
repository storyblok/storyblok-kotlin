package com.example.jetnews

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.storyblok.ktor.Api.Config.Version

/**
 * Let debug builds of the Android app fetch draft content from Storyblok,
 * while release builds fetch published content.
 */
internal actual val contentVersion: Version
    @Composable get() = when (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
        ApplicationInfo.FLAG_DEBUGGABLE -> Version.Draft
        else -> Version.Published
    }
