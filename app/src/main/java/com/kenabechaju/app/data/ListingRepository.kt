package com.kenabechaju.app.data

import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.PageDto
import com.kenabechaju.app.data.remote.dto.TagDto
import com.kenabechaju.app.data.remote.toApiFailure
import javax.inject.Inject
import javax.inject.Singleton

/** Same filter shape as web's `BrowseFilters` (lib/api/listings.ts) — kept
 *  as plain nullable fields rather than a query-string builder, since
 *  Retrofit's `@QueryMap` already drops absent keys the same way web's
 *  `for...continue` loop does. */
data class BrowseFilters(
    val q: String? = null,
    val tags: List<String> = emptyList(),
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val condition: String? = null,
    val category: String? = null,
    val sort: String = "newest",
    val limit: Int = 24,
    val offset: Int = 0,
) {
    fun toQueryMap(): Map<String, String> = buildMap {
        q?.takeIf { it.isNotBlank() }?.let { put("q", it) }
        minPrice?.let { put("min_price", it.toString()) }
        maxPrice?.let { put("max_price", it.toString()) }
        condition?.let { put("condition", it) }
        category?.let { put("category", it) }
        put("sort", sort)
        put("limit", limit.toString())
        put("offset", offset.toString())
    }
}

@Singleton
class ListingRepository @Inject constructor(private val api: ApiService) {
    suspend fun browse(filters: BrowseFilters): AuthResult<PageDto<ListingDto>> = resultOf {
        api.browseListings(filters.toQueryMap(), filters.tags)
    }

    suspend fun categories(): AuthResult<List<CategoryDto>> = resultOf { api.getCategories() }

    suspend fun trendingTags(): AuthResult<List<TagDto>> = resultOf { api.getTrendingTags() }

    suspend fun searchSuggestions(q: String): AuthResult<List<String>> = resultOf {
        api.getSearchSuggestions(q)
    }

    suspend fun getListing(id: String): AuthResult<ListingDto> = resultOf { api.getListing(id) }

    suspend fun getRelated(id: String): AuthResult<List<ListingDto>> = resultOf {
        api.getRelatedListings(id)
    }

    suspend fun savedIds(): AuthResult<List<String>> = resultOf { api.getSavedIds() }

    suspend fun toggleSaved(listingId: String): AuthResult<Boolean> = resultOf {
        api.toggleSaved(listingId).saved
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
