package com.kenabechaju.app.data

import android.content.Context
import android.net.Uri
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ListingCreateRequestDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.ListingImageDto
import com.kenabechaju.app.data.remote.dto.ListingUpdateRequestDto
import com.kenabechaju.app.data.remote.dto.PageDto
import com.kenabechaju.app.data.remote.dto.TagDto
import com.kenabechaju.app.data.remote.toApiFailure
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

/** Shared shape for both create (POST) and edit (PATCH) — the same fields
 *  `ListingCreate`/`ListingUpdate` both collect, just optional on the
 *  PATCH side. Kept as one form model since the screen editing them is
 *  the same screen either way. */
data class ListingFormInput(
    val title: String,
    val description: String,
    val price: String?,
    val priceType: String,
    val unit: String?,
    val condition: String?,
    val tags: List<String>,
    val fulfillmentType: String,
    val pickupAddress: String?,
    val categoryId: String?,
    val customCategory: String?,
)

@Singleton
class ListingRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context,
) {
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

    suspend fun myListings(shopId: String? = null): AuthResult<List<ListingDto>> = resultOf {
        api.listMyListings(shopId)
    }

    suspend fun createListing(input: ListingFormInput): AuthResult<ListingDto> = resultOf {
        api.createListing(
            ListingCreateRequestDto(
                title = input.title,
                description = input.description,
                price = input.price,
                priceType = input.priceType,
                unit = input.unit,
                condition = input.condition,
                tags = input.tags,
                fulfillmentType = input.fulfillmentType,
                pickupAddress = input.pickupAddress,
                categoryId = input.categoryId,
                customCategory = input.customCategory,
            )
        )
    }

    suspend fun updateListing(listingId: String, input: ListingFormInput): AuthResult<ListingDto> = resultOf {
        api.updateListing(
            listingId,
            ListingUpdateRequestDto(
                title = input.title,
                description = input.description,
                price = input.price,
                priceType = input.priceType,
                unit = input.unit,
                condition = input.condition,
                tags = input.tags,
                fulfillmentType = input.fulfillmentType,
                pickupAddress = input.pickupAddress,
                categoryId = input.categoryId,
                customCategory = input.customCategory,
            )
        )
    }

    suspend fun deleteListing(listingId: String): AuthResult<Unit> = resultOf { api.deleteListing(listingId) }

    suspend fun markSold(listingId: String): AuthResult<ListingDto> = resultOf { api.markSold(listingId) }

    suspend fun markOutOfStock(listingId: String): AuthResult<ListingDto> = resultOf { api.markOutOfStock(listingId) }

    suspend fun markAvailable(listingId: String): AuthResult<ListingDto> = resultOf { api.markAvailable(listingId) }

    suspend fun pauseListing(listingId: String): AuthResult<ListingDto> = resultOf { api.pauseListing(listingId) }

    suspend fun relistListing(listingId: String): AuthResult<ListingDto> = resultOf { api.relistListing(listingId) }

    suspend fun renewListing(listingId: String): AuthResult<ListingDto> = resultOf { api.renewListing(listingId) }

    suspend fun uploadListingImage(listingId: String, uri: Uri): AuthResult<ListingImageDto> = resultOf {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected image")
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "listing.jpg", body)
        api.uploadListingImage(listingId, part)
    }

    suspend fun deleteListingImage(listingId: String, imageId: String): AuthResult<Unit> = resultOf {
        api.deleteListingImage(listingId, imageId)
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
