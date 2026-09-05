package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Generic paginated envelope — mirrors `Page[T]` (backend/app/schemas/common.py). */
@Serializable
data class PageDto<T>(val items: List<T>, val total: Int, val limit: Int, val offset: Int)

@Serializable
data class TagDto(val id: String, val name: String, @SerialName("usage_count") val usageCount: Int)

/** Mirrors `CategoryRef` — the shape embedded on a listing. */
@Serializable
data class CategoryRefDto(val id: String, val name: String, val slug: String, val icon: String? = null)

/** Mirrors `CategoryChildOut`. */
@Serializable
data class CategoryChildDto(
    val id: String,
    val name: String,
    val slug: String,
    val icon: String? = null,
    @SerialName("listing_count") val listingCount: Int,
    @SerialName("is_active") val isActive: Boolean = true,
)

/** Mirrors `CategoryOut` — a top-level category with its children. */
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    val icon: String? = null,
    @SerialName("listing_count") val listingCount: Int,
    @SerialName("is_active") val isActive: Boolean = true,
    val children: List<CategoryChildDto> = emptyList(),
)

@Serializable
data class ListingImageDto(val id: String, @SerialName("image_url") val imageUrl: String, @SerialName("sort_order") val sortOrder: Int)

/** Mirrors `ListingSellerOut`. */
@Serializable
data class ListingSellerDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
)

/** Mirrors `ListingShopOut`. */
@Serializable
data class ListingShopDto(
    val id: String,
    @SerialName("shop_name") val shopName: String,
    val slug: String,
    @SerialName("logo_url") val logoUrl: String? = null,
)

/**
 * Mirrors `ListingOut` (backend/app/schemas/listing.py) field for field.
 * `condition`/`status`/`price_type`/`fulfillment_type` stay raw strings —
 * same "no renamed Kotlin taste" discipline as `UserDto.role` — mapped to
 * display labels only at the UI edge.
 */
@Serializable
data class ListingDto(
    val id: String,
    val title: String,
    val description: String,
    val price: String? = null,
    @SerialName("price_type") val priceType: String,
    val unit: String? = null,
    val condition: String,
    val status: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("fulfillment_type") val fulfillmentType: String,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("is_top") val isTop: Boolean,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("featured_until") val featuredUntil: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    val seller: ListingSellerDto,
    val shop: ListingShopDto? = null,
    val category: CategoryRefDto? = null,
    @SerialName("custom_category") val customCategory: String? = null,
    val images: List<ListingImageDto> = emptyList(),
    val tags: List<TagDto> = emptyList(),
    @SerialName("has_pending_restock_request") val hasPendingRestockRequest: Boolean? = null,
    @SerialName("restock_request_count") val restockRequestCount: Int = 0,
    @SerialName("is_featured") val isFeatured: Boolean = false,
)

@Serializable
data class SavedToggleDto(val saved: Boolean)
