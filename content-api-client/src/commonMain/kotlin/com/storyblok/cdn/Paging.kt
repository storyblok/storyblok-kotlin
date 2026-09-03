package com.storyblok.cdn

import androidx.paging.LoadType
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
import kotlinx.coroutines.CancellationException

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
 * Fetches one page of the offset-paginated endpoint at [path], reading its items out of the response body with
 * [items]. The Content Delivery API pages uniformly, so everything but [items] describes any of its endpoints:
 * `page` and `per_page` go out ahead of [params], so that a query stating them replaces them, and the `Total` and
 * `Per-Page` response headers describe what came back, each falling back to what the page itself shows when the
 * header is absent.
 *
 * When [cachedOnly] is `true` the request only reads Ktor's HTTP cache; a cache miss (a `504 Gateway Timeout`)
 * yields an empty page.
 *
 * @param T The type of the items on the page.
 */
internal suspend fun <T : Any> HttpClient.fetchPage(
    path: String,
    page: Int,
    perPage: Int,
    cachedOnly: Boolean,
    params: Map<String, String>,
    items: (body: String) -> List<T>,
): PagedResponse<T> {
    val response = try {
        get(path) {
            if (cachedOnly) onlyIfCached()
            (mapOf("page" to page.toString(), "per_page" to perPage.toString()) + params)
                .forEach { (name, value) -> parameter(name, value) }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: ServerResponseException) {
        if (cachedOnly && e.response.status == HttpStatusCode.GatewayTimeout) {
            return PagedResponse(emptyList(), total = 0, perPage = perPage, page = page)
        }
        throw StoryblokClientException(e.response.bodyAsText(), e)
    } catch (e: Throwable) {
        throw StoryblokClientException(e.message, e)
    }

    return PagedResponse(
        items = items(response.body<String>()),
        total = response.headers["Total"]?.toIntOrNull(),
        perPage = response.headers["Per-Page"]?.toIntOrNull() ?: perPage,
        page = page,
    )
}

/**
 * [PagingSource] that reads pages from Ktor's HTTP cache, via a [fetch] that only reads the cache. Fresh data is
 * pulled from the network by [NetworkRemoteMediator], which refreshes the cache and invalidates this source.
 *
 * @param T The type of the items being paged.
 */
internal class CachedPagingSource<T : Any>(
    private val fetch: suspend (page: Int) -> PagedResponse<T>,
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val response = fetch(page)
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
        state.anchorPosition?.let { anchor ->
            val closest = state.closestPageToPosition(anchor)
            closest?.prevKey?.plus(1) ?: closest?.nextKey?.minus(1)
        }
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

    private var lastPage: Int = 0

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, T>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> lastPage + 1
        }
        return try {
            val response = fetch(page)
            lastPage = page
            invalidate()
            MediatorResult.Success(endOfPaginationReached = response.last)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            MediatorResult.Error(e)
        }
    }
}
