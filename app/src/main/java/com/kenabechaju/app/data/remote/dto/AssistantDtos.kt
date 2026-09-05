package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssistantTurnDto(val role: String, val content: String)

@Serializable
data class AssistantChatRequestDto(
    val message: String,
    val history: List<AssistantTurnDto> = emptyList(),
    val locale: String = "en",
)
