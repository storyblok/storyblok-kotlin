package ktorplugin.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fixture component for the stories examples that address a typed field of the example space's `post` content type —
 * the "sorting by fields" sort and the filter-query examples (`client.stories<Post> { ... }`).
 */
@Serializable
@SerialName("post")
class Post(
    val title: String? = null,
    val author: String? = null,
    val topics: String? = null,
    val schedule: String? = null,
    val categories: List<String>? = null,
    val description: String? = null,
    val highlighted: Boolean? = null,
) : Component()
