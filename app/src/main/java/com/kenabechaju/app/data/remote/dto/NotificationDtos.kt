package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `NotificationOut`. `type` stays a raw string, same discipline
 *  as every other backend enum in this app. */
@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String? = null,
    @SerialName("link_url") val linkUrl: String? = null,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class NotificationListDto(val items: List<NotificationDto>, @SerialName("unread_count") val unreadCount: Int)
