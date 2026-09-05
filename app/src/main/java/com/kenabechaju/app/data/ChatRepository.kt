package com.kenabechaju.app.data

import android.content.Context
import android.net.Uri
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.ConversationDto
import com.kenabechaju.app.data.remote.dto.MessageDto
import com.kenabechaju.app.data.remote.dto.SendMessageRequestDto
import com.kenabechaju.app.data.remote.toApiFailure
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context,
) {
    suspend fun contactSeller(listingId: String): AuthResult<ConversationDto> = resultOf {
        api.contactSeller(listingId)
    }

    suspend fun listConversations(shopId: String? = null): AuthResult<List<ConversationDto>> = resultOf {
        api.listConversations(shopId)
    }

    suspend fun getConversation(id: String): AuthResult<ConversationDto> = resultOf { api.getConversation(id) }

    suspend fun getMessages(id: String, before: String? = null): AuthResult<List<MessageDto>> = resultOf {
        api.getMessages(id, before)
    }

    suspend fun sendMessage(conversationId: String, content: String): AuthResult<MessageDto> = resultOf {
        api.sendMessage(conversationId, SendMessageRequestDto(content))
    }

    suspend fun markRead(conversationId: String): AuthResult<Unit> = resultOf {
        api.markConversationRead(conversationId)
    }

    suspend fun sendAttachment(conversationId: String, uri: Uri, caption: String): AuthResult<MessageDto> = resultOf {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected image")
        val filePart = MultipartBody.Part.createFormData(
            "file", "attachment.jpg", bytes.toRequestBody(mimeType.toMediaTypeOrNull()),
        )
        val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())
        api.sendAttachment(conversationId, filePart, captionPart)
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
