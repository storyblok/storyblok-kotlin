package com.example.jetnews

import androidx.compose.runtime.Composable
import com.storyblok.ktor.Api.Config.Version
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

/** The web target exists to serve the Visual Editor, so it always reads unpublished content. */
internal actual val contentVersion: Version
    @Composable get() = Version.Draft

/**
 * Derived from the path so that a preview URL such as `/post6` opens that story.
 *
 * In a language other than the default one, the Visual Editor puts the language in front of the
 * path (`/de/post6`), and names it in `_storyblok_lang`. The slug is the path without it: the
 * client already asks for the editor's language.
 *
 * The Visual Editor's **Real path** field can be set to something other than the story's full
 * slug, in which case this lookup will not find it.
 */
internal actual val initialStoryKey: StoryKey =
    window.location.pathname.trim('/').split('/').let { segments ->
        // `default` names the default language, which has no path segment.
        val language = URLSearchParams(window.location.search.toJsString()).get("_storyblok_lang")
            ?.takeUnless { it == "default" }
        val slug = (if (segments.first() == language) segments.drop(1) else segments).joinToString("/")
        if (slug.isEmpty()) HomeKey else StoryKey(slug = slug)
    }
