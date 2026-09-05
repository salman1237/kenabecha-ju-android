package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostImageDto(val id: String, @SerialName("image_url") val imageUrl: String, @SerialName("sort_order") val sortOrder: Int)

/** Mirrors `PostShopOut`. */
@Serializable
data class PostShopDto(
    val id: String,
    @SerialName("shop_name") val shopName: String,
    val slug: String,
    @SerialName("logo_url") val logoUrl: String? = null,
)

/** Mirrors `PostOut`. `status` stays a raw string (`pending`/`published`/
 *  `rejected`) — same "no renamed Kotlin taste" discipline as everywhere
 *  else in this app. */
@Serializable
data class PostDto(
    val id: String,
    val shop: PostShopDto,
    val title: String,
    @SerialName("description_html") val descriptionHtml: String,
    val slug: String,
    val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val images: List<PostImageDto> = emptyList(),
    val listings: List<ListingDto> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable
data class PostCreateRequestDto(
    @SerialName("shop_id") val shopId: String,
    val title: String,
    @SerialName("description_html") val descriptionHtml: String,
    @SerialName("listing_ids") val listingIds: List<String> = emptyList(),
)

@Serializable
data class PostUpdateRequestDto(
    val title: String? = null,
    @SerialName("description_html") val descriptionHtml: String? = null,
    @SerialName("listing_ids") val listingIds: List<String>? = null,
)
