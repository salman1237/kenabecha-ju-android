package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `RatingEligibility`. */
@Serializable
data class RatingEligibilityDto(@SerialName("can_rate") val canRate: Boolean, val reason: String? = null)

@Serializable
data class RatingCreateRequestDto(val stars: Int, @SerialName("review_text") val reviewText: String? = null)

/** Mirrors `ReportCreate` — `targetType`/`reasonCode` stay raw strings, same
 *  discipline as every other backend enum in this app. */
@Serializable
data class ReportCreateRequestDto(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("reason_code") val reasonCode: String,
    val note: String? = null,
)
