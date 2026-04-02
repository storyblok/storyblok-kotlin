package com.storyblok.material3.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.withCompositionLocal
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType.Companion.Em
import androidx.compose.ui.unit.TextUnitType.Companion.Sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val LocalBulletScope = compositionLocalOf<ListScope> { error("No bullet scope provided") }

@Composable
internal inline fun buildAnnotatedString(builder: AnnotatedString.Builder.() -> Unit): AnnotatedString {
    val listScope = ListScope()
    return withCompositionLocal(LocalBulletScope provides listScope) {
        buildAnnotatedString { builder(this) }
    }
}

internal class ListScope {
    val listSettingStack = mutableListOf<Pair<TextUnit, Bullet>>()
}

internal val Bullet.Companion.None: Bullet
    get() = Bullet(object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
            Outline.Generic(Path())
    }, 0.em, 0.em, -Bullet.DefaultIndentation)

@Composable
internal inline fun <R : Any> AnnotatedString.Builder.withList(
    indentation: TextUnit = Bullet.DefaultIndentation,
    bullet: Bullet = Bullet.None,
    block: () -> R,
): R {
    val bulletScope = LocalBulletScope.current
    val adjustedIndentation =
        bulletScope.listSettingStack.lastOrNull()?.first?.let {
            require(it.type == indentation.type) {
                "Indentation unit types of nested bullet lists must match. Current $it and previous is $indentation"
            }
            when (indentation.type) {
                Sp -> (indentation.value + it.value).sp
                Em -> (indentation.value + it.value).em
                else -> indentation
            }
        } ?: indentation

    val parIndex =
        pushStyle(
            ParagraphStyle(
                textIndent = TextIndent(adjustedIndentation, adjustedIndentation)
            )
        )
    bulletScope.listSettingStack.add(Pair(adjustedIndentation, bullet))
    return try {
        block()
    } finally {
        if (bulletScope.listSettingStack.isNotEmpty()) {
            bulletScope.listSettingStack.removeAt(
                bulletScope.listSettingStack.lastIndex
            )
        }
        pop(parIndex)
    }
}

@Composable
internal inline fun <R : Any> AnnotatedString.Builder.withListItem(
    bullet: Bullet? = null,
    block: AnnotatedString.Builder.() -> R,
): R {
    val bulletScope = LocalBulletScope.current
    val lastItemInStack = bulletScope.listSettingStack.lastOrNull()
    val itemIndentation = lastItemInStack?.first ?: Bullet.DefaultIndentation
    val itemBullet = bullet ?: (lastItemInStack?.second ?: Bullet.Default)
    val parIndex =
        pushStyle(
            ParagraphStyle(textIndent = TextIndent(itemIndentation, itemIndentation))
        )
    val bulletIndex = pushBullet(itemBullet)
    return try {
        block(this)
    } finally {
        pop(bulletIndex)
        pop(parIndex)
    }
}
