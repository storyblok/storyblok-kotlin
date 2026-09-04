package ktorplugin.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fixture component for the stories examples that address a typed content field — the "sorting by fields" sort and the
 * filter-query examples (`client.stories<Article> { ... }`)
 */
@Serializable
@SerialName("article")
class Article(
    val headline: String? = null,
    val highlighted: Boolean? = null,
    val topics: List<String>? = null,
    val categories: List<String>? = null,
    val scheduled: String? = null,
) : Component()
