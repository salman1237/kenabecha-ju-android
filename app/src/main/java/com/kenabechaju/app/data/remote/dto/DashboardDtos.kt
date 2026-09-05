package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `DashboardStatsOut`. */
@Serializable
data class DashboardStatsDto(
    @SerialName("active_listings") val activeListings: Int,
    @SerialName("sold_listings") val soldListings: Int,
    val shops: Int,
    @SerialName("unread_messages") val unreadMessages: Int,
    val conversations: Int,
    @SerialName("saved_count") val savedCount: Int,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("rating_count") val ratingCount: Int,
)

/** Mirrors `RatingOut`. */
@Serializable
data class RatingDto(
    val id: String,
    @SerialName("listing_id") val listingId: String,
    val stars: Int,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String,
    val rater: ListingSellerDto,
)

/** Mirrors `ShopOut` — enough fields for a profile's shop badge list; the
 *  full shop screen (Phase 3) will want the rest. */
@Serializable
data class ShopSummaryDto(
    val id: String,
    @SerialName("shop_name") val shopName: String,
    val slug: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("listing_count") val listingCount: Int = 0,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
)

/** Mirrors `UserProfileOut` — any user's public-facing profile. */
@Serializable
data class UserProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val email: String,
    val phone: String? = null,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("registration_no") val registrationNo: String? = null,
    val hall: HallDto? = null,
    val session: String? = null,
    val bio: String? = null,
    val department: DepartmentDto? = null,
    val batch: Int? = null,
    @SerialName("profile_complete") val profileComplete: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("average_rating") val averageRating: Double? = null,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("recent_ratings") val recentRatings: List<RatingDto> = emptyList(),
    val shops: List<ShopSummaryDto> = emptyList(),
)
