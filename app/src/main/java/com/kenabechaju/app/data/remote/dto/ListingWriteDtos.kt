package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `ListingCreate` (backend/app/schemas/listing.py) field for field. */
@Serializable
data class ListingCreateRequestDto(
    val title: String,
    val description: String,
    val price: String? = null,
    @SerialName("price_type") val priceType: String,
    val unit: String? = null,
    val condition: String? = null,
    @SerialName("shop_id") val shopId: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("fulfillment_type") val fulfillmentType: String,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("custom_category") val customCategory: String? = null,
)

/** Mirrors `ListingUpdate` — every field optional, only what's sent changes. */
@Serializable
data class ListingUpdateRequestDto(
    val title: String? = null,
    val description: String? = null,
    val price: String? = null,
    @SerialName("price_type") val priceType: String? = null,
    val unit: String? = null,
    val condition: String? = null,
    val tags: List<String>? = null,
    @SerialName("fulfillment_type") val fulfillmentType: String? = null,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("custom_category") val customCategory: String? = null,
)
