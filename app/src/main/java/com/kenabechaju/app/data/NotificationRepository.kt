package com.kenabechaju.app.data

import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.NotificationDto
import com.kenabechaju.app.data.remote.dto.NotificationListDto
import com.kenabechaju.app.data.remote.toApiFailure
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(private val api: ApiService) {
    suspend fun list(): AuthResult<NotificationListDto> = resultOf { api.getNotifications() }

    suspend fun markRead(id: String): AuthResult<NotificationDto> = resultOf { api.markNotificationRead(id) }

    suspend fun markAllRead(): AuthResult<Unit> = resultOf { api.markAllNotificationsRead() }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
