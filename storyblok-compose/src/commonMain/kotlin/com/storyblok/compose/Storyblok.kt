package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.storyblok.cdn.StoryblokClient
import com.storyblok.compose.provider.BlockProvider
import com.storyblok.ktor.Api.Config.Region
import com.storyblok.ktor.Api.Config.Region.EU
import com.storyblok.ktor.Api.Config.Version
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val clientKeys = mutableSetOf<List<Any?>>()
private val clients = mutableMapOf<List<Any?>, StoryblokClient>()

/**
 * Entry point composable for rendering Storyblok content.
 *
 * Creates a [StoryblokClient] and provides a [StoryblokScope] with access to both
 * the client and registered block composables.
 *
 * @param accessToken The API access token for authentication.
 * @param version The content [version][Version] to retrieve (draft or published).
 * @param region The [region][Region] depending on the server location of your space. Defaults to [EU].
 * @param language Optional language code for localized content.
 * @param fallbackLanguage Optional fallback language for untranslated fields.
 * @param cv Optional cache version timestamp.
 * @param blockProvider The [BlockProvider] containing registered component composables and serializers.
 * @param content The composable content rendered within the [StoryblokScope].
 */
@Composable
public fun Storyblok(
    accessToken: String,
    version: Version,
    region: Region = EU,
    language: String? = null,
    fallbackLanguage: String? = null,
    cv: String? = null,
    blockProvider: BlockProvider,
    content: @Composable StoryblokScope.() -> Unit,
) {

    val clientKey = listOf(accessToken, version, region, language, fallbackLanguage, cv)
    clientKeys.add(clientKey)

    DisposableEffect(clientKey) {
        onDispose {
            clientKeys.remove(clientKey)
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                delay(5.seconds)
                if(clientKey in clientKeys) return@launch
                clients.remove(clientKey)?.close()
            }
        }
    }

    val client = clients.getOrPut(clientKey) {
        StoryblokClient(accessToken, version, region, language, fallbackLanguage, cv, blockProvider.serializersModule)
    }

    content(StoryblokScope(client, blockProvider.blockScope))
}

/**
 * Scope combining [StoryblokClient] and [BlockScope] capabilities.
 *
 * Provides access to story fetching via [StoryblokClient] and component rendering via [BlockScope].
 */
public class StoryblokScope(
    /** The underlying [StoryblokClient] used to fetch stories. */
    public val client: StoryblokClient,
    blockScope: BlockScope
) : BlockScope by blockScope, StoryblokClient by client