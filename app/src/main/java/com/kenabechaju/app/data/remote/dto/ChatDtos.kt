package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `MessageOut`. */
@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("receiver_id") val receiverId: String,
    val content: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("read_at") val readAt: String? = null,
)

/** Mirrors `ConversationListingOut` — the product-card context shown at
 *  the top of a thread, not a real chat message. */
@Serializable
data class ConversationListingDto(
    val id: String,
    val title: String,
    val price: String? = null,
    @SerialName("price_type") val priceType: String,
    val unit: String? = null,
    val status: String,
    @SerialName("image_url") val imageUrl: String? = null,
)

/** Mirrors `ConversationOut`. */
@Serializable
data class ConversationDto(
    val id: String,
    val listing: ConversationListingDto,
    val shop: ListingShopDto? = null,
    val counterparty: ListingSellerDto,
    @SerialName("is_seller") val isSeller: Boolean,
    @SerialName("last_message_preview") val lastMessagePreview: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SendMessageRequestDto(val content: String)
