/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.jetnews.resources.Res
import com.example.jetnews.resources.montserrat_medium
import com.example.jetnews.resources.montserrat_regular
import org.jetbrains.compose.resources.Font

/*
 * Loading a font resource is a composable read, so the family and everything derived from it are
 * `@Composable get()` properties rather than the top-level `val`s the Android sample uses. Call
 * sites are unchanged.
 *
 * The Android-only `PlatformTextStyle(includeFontPadding = false)` is gone with them: font padding
 * is an Android text-layout quirk that the other targets never had.
 */

private val Montserrat: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.montserrat_regular),
        Font(Res.font.montserrat_medium, FontWeight.W500),
    )

val defaultTextStyle: TextStyle
    @Composable get() = TextStyle(
        fontFamily = Montserrat,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )

val JetnewsTypography: Typography
    @Composable get() = Typography(
        displayLarge = defaultTextStyle.copy(
            fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp,
        ),
        displayMedium = defaultTextStyle.copy(
            fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp,
        ),
        displaySmall = defaultTextStyle.copy(
            fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp,
        ),
        headlineLarge = defaultTextStyle.copy(
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp, lineBreak = LineBreak.Heading,
        ),
        headlineMedium = defaultTextStyle.copy(
            fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp, lineBreak = LineBreak.Heading,
        ),
        headlineSmall = defaultTextStyle.copy(
            fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp, lineBreak = LineBreak.Heading,
        ),
        titleLarge = defaultTextStyle.copy(
            fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp, lineBreak = LineBreak.Heading,
        ),
        titleMedium = defaultTextStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
            fontWeight = FontWeight.Medium,
            lineBreak = LineBreak.Heading,
        ),
        titleSmall = defaultTextStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight.Medium,
            lineBreak = LineBreak.Heading,
        ),
        labelLarge = defaultTextStyle.copy(
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, fontWeight = FontWeight.Medium,
        ),
        labelMedium = defaultTextStyle.copy(
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium,
        ),
        labelSmall = defaultTextStyle.copy(
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium,
        ),
        bodyLarge = defaultTextStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
            lineBreak = LineBreak.Paragraph,
        ),
        bodyMedium = defaultTextStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
            lineBreak = LineBreak.Paragraph,
        ),
        bodySmall = defaultTextStyle.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
            lineBreak = LineBreak.Paragraph,
        ),
    )
