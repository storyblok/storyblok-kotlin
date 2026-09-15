package com.storyblok.cdn

import kotlinx.serialization.json.Json

/** Only the web target can be embedded in the Visual Editor. */
internal actual fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge = NoVisualEditor
