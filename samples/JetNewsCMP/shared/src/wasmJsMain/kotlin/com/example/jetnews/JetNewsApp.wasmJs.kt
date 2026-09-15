package com.example.jetnews

import com.storyblok.ktor.Api.Config.Version
import kotlinx.browser.window

/** The web target exists to serve the Visual Editor, so it reads unpublished content. */
internal actual val contentVersion: Version = Version.Draft

/**
 * Derived from the path so that a preview URL such as `/post6` opens that story.
 *
 * The Visual Editor's **Real path** field can be set to something other than the story's full
 * slug, in which case this lookup will not find it.
 */
internal actual val initialStoryKey: StoryKey =
    window.location.pathname.trim('/').let { slug ->
        if (slug.isEmpty()) HomeKey else StoryKey(slug = slug)
    }
