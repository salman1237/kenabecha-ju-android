package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `ShopOut`. */
@Serializable
data class ShopDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("shop_name") val shopName: String,
    val slug: String,
    val description: String? = null,
    @SerialName("shop_type") val shopType: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("listing_count") val listingCount: Int = 0,
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
)

@Serializable
data class RateableListingDto(val id: String, val title: String)

/** Mirrors `ShopStatsOut`. */
@Serializable
data class ShopStatsDto(
    @SerialName("active_listings") val activeListings: Int,
    @SerialName("sold_count") val soldCount: Int,
    val followers: Int,
    @SerialName("review_count") val reviewCount: Int,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("is_following") val isFollowing: Boolean? = null,
    @SerialName("rateable_listing") val rateableListing: RateableListingDto? = null,
)

@Serializable
data class ShopCreateRequestDto(
    @SerialName("shop_name") val shopName: String,
    val description: String? = null,
    @SerialName("shop_type") val shopType: String? = null,
)

@Serializable
data class ShopUpdateRequestDto(
    @SerialName("shop_name") val shopName: String? = null,
    val description: String? = null,
    @SerialName("shop_type") val shopType: String? = null,
)

@Serializable
data class FollowToggleDto(val following: Boolean)
