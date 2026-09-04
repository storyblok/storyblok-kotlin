package com.storyblok.cdn

import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * One page of an offset-paginated endpoint. The Content Delivery API pages uniformly — a `page` and a `per_page`
 * parameter, a `Total` response header — so this describes any of them, not only stories.
 *
 * @param T The type of the items on the page.
 */
internal class PagedResponse<T : Any>(
    val items: List<T>,
    val total: Int?,
    val perPage: Int,
    val page: Int,
) {
    /** The total number of pages, or `null` when the response did not say how many items there are. */
    val pages: Int? get() = total?.let { if (perPage <= 0) 0 else (it + perPage - 1) / perPage }

    /**
     * Whether [page] is the last one. When the endpoint said how many items there are this follows from [pages];
     * when it did not, a page short of [perPage] is taken as the last, since no earlier page can be. Guessing the
     * other way — that a full page is the last — would stop paging after the first one.
     */
    val last: Boolean get() = pages?.let { page >= it } ?: (items.size < perPage)
}

/**
 * A [Pager] over the offset-paginated endpoint at [path], reading each page's items out of the response body with
 * [items]. The Content Delivery API pages uniformly, so only those two and the [params] the query states
 * are particular to an endpoint; everything below applies to any of them.
 *
 * Pages are read from Ktor's HTTP cache by a [CachedPagingSource] and refreshed from the network by a
 * [NetworkRemoteMediator], which repopulates the cache and invalidates the source so the fresh copy is re-read —
 * the same cached-then-fresh behaviour [com.storyblok.cdn.StoryblokClient.story] has.
 *
 * @param T The type of the items being paged.
 */
internal fun <T : Any> HttpClient.pager(
    path: String,
    config: PagingConfig,
    params: Map<String, String>,
    items: (body: String) -> List<T>,
): Pager<Int, T> {
    // Completed once the mediator's initial refresh has been through, whether it fetched or failed. The first
    // cached read waits on this rather than publishing the cold cache as an empty page. Relies on the mediator
    // asking for that refresh — see its LAUNCH_INITIAL_REFRESH — since nothing else completes this.
    val primed = CompletableDeferred<Unit>()

    // The factory owns the invalidation, rather than the mediator holding the source in a captured var: that var
    // would be written on one coroutine and read on another, and a second collection of this cold flow would leave
    // the first mediator invalidating a source nobody was reading.
    val sources = InvalidatingPagingSourceFactory {
        CachedPagingSource(
            awaitPriming = { primed.await() },
            fetch = { page -> read(path, page, config.pageSize, cachedOnly = true, params, items) },
        )
    }

    return Pager(
        config = config,
        remoteMediator = NetworkRemoteMediator(
            fetch = { page ->
                try {
                    checkNotNull(read(path, page, config.pageSize, cachedOnly = false, params, items)) {
                        "a read that may go to the network cannot miss the cache"
                    }
                } finally {
                    primed.complete(Unit)
                }
            },
            invalidate = sources::invalidate,
        ),
        // Invoked rather than passed: only the JVM has a Pager overload taking the factory type itself.
        pagingSourceFactory = { sources() },
    )
}

/**
 * Fetches one page of the offset-paginated endpoint at [path], reading its items out of the response body with
 * [items]. The Content Delivery API pages uniformly, so everything but [items] describes any of its endpoints:
 * `page` and `per_page` go out ahead of [params], so that a query stating them replaces them, and the `Total` and
 * `Per-Page` response headers describe what came back, each falling back to what the page itself shows when the
 * header is absent.
 *
 * With [cachedOnly] the read never touches the network and answers `null` when Ktor's cache does not hold the page.
 * The miss is reported rather than rendered as an empty page: an empty page is indistinguishable from the end of
 * the list, and a collector that does not re-drive its own loads would take it for one. A read that may go to the
 * network cannot miss, and so never answers `null`.
 *
 * @param T The type of the items on the page.
 */
private suspend fun <T : Any> HttpClient.read(
    path: String,
    page: Int,
    perPage: Int,
    cachedOnly: Boolean,
    params: Map<String, String>,
    items: (body: String) -> List<T>,
): PagedResponse<T>? {
    val response = try {
        get(path) {
            if (cachedOnly) onlyIfCached()
            (mapOf("page" to page.toString(), "per_page" to perPage.toString()) + params)
                .forEach { (name, value) -> parameter(name, value) }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: ServerResponseException) {
        // Ktor answers a cached-only read it cannot serve with a 504, which is a miss rather than a failure.
        if (cachedOnly && e.response.status == HttpStatusCode.GatewayTimeout) return null
        throw StoryblokClientException(e.response.bodyAsText(), e)
    } catch (e: Throwable) {
        throw StoryblokClientException(e.message, e)
    }

    val fetched = try {
        items(response.body<String>())
    } catch (e: CancellationException) {
        throw e
    } catch (e: SerializationException) {
        // As StoryblokClient.story does: a deserialization failure is a modelling error rather than a transient API
        // one, so it propagates unwrapped instead of arriving as a (potentially retryable) client exception.
        throw e
    } catch (e: Throwable) {
        // A body that is well-formed JSON but not the shape this endpoint answers with does not reach the
        // deserializer at all — it fails on the way there, and would otherwise surface to the caller as whatever
        // the traversal happened to throw.
        throw StoryblokClientException(e.message, e)
    }

    return PagedResponse(
        items = fetched,
        total = response.headers["Total"]?.toIntOrNull(),
        perPage = response.headers["Per-Page"]?.toIntOrNull() ?: perPage,
        page = page,
    )
}

/**
 * The page a loaded result came from. A page states its predecessor as [prevKey][PagingSource.LoadResult.Page.prevKey]
 * and the first page has none, so the number is recoverable from the [PagingState] without tracking it separately.
 */
private val PagingSource.LoadResult.Page<Int, *>.page: Int get() = prevKey?.plus(1) ?: 1

/**
 * [PagingSource] that reads pages from Ktor's HTTP cache, via a [fetch] that only reads the cache and answers `null`
 * when it does not hold the page. Fresh data is pulled from the network by [NetworkRemoteMediator], which refreshes
 * the cache and invalidates this source.
 *
 * A miss is published as an empty page, since an exhausted source is what asks the mediator for the next page — but
 * not on the very first load, where an empty page would be the whole of what a collector sees and is
 * indistinguishable from an endpoint with no stories. That one waits on [awaitPriming] instead: the mediator's
 * initial refresh is already on its way to filling the cache, so waiting for it publishes the loaded page rather
 * than an empty one, and costs no request that was not being made anyway.
 *
 * @param T The type of the items being paged.
 */
internal class CachedPagingSource<T : Any>(
    private val awaitPriming: suspend () -> Unit,
    private val fetch: suspend (page: Int) -> PagedResponse<T>?,
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val cached = fetch(page)
                ?: if (params is LoadParams.Refresh) awaitPriming().let { fetch(page) } else null

            val response = cached ?: return LoadResult.Page(
                data = emptyList(),
                prevKey = if (page <= 1) null else page - 1,
                nextKey = null,
            )

            LoadResult.Page(
                data = response.items,
                prevKey = if (page <= 1) null else page - 1,
                nextKey = if (response.items.isEmpty() || response.last) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { anchor -> state.closestPageToPosition(anchor)?.page }
}

/**
 * [RemoteMediator] that fetches fresh pages from the network, populating Ktor's HTTP cache, and then invalidates the
 * [CachedPagingSource] so the refreshed data is re-read. This mirrors the cached-then-fresh behavior of
 * [StoryblokClient.story].
 *
 * @param T The type of the items being paged.
 */
internal class NetworkRemoteMediator<T : Any>(
    private val fetch: suspend (page: Int) -> PagedResponse<T>,
    private val invalidate: () -> Unit,
) : RemoteMediator<Int, T>() {

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, T>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> state.pages.lastOrNull()
                ?.let { if (it.data.isEmpty()) it.page else it.page + 1 }
                ?: 1
        }
        return try {
            val response = fetch(page)
            invalidate()
            MediatorResult.Success(endOfPaginationReached = response.last)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            MediatorResult.Error(e)
        }
    }
}
