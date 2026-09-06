package com.kenabechaju.app.data.remote

import com.google.firebase.messaging.FirebaseMessagingService
import com.kenabechaju.app.data.TokenStore
import com.kenabechaju.app.data.remote.dto.DeviceTokenRequestDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Covers only the background/killed-app push path: FCM auto-displays the
 * system notification for a backgrounded app using the channel declared in
 * AndroidManifest.xml's `default_notification_channel_id` meta-data, so
 * `onMessageReceived` is never even invoked for that path — a foregrounded
 * app already gets the same event live over the existing chat/notification
 * WebSocket, so there's nothing for this service to additionally display.
 * It only exists to keep the backend's `device_tokens` row current when FCM
 * rotates the token independently of any login/logout event.
 */
@AndroidEntryPoint
class KenaBechaMessagingService : FirebaseMessagingService() {

    @Inject lateinit var api: ApiService
    @Inject lateinit var tokenStore: TokenStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            if (tokenStore.accessToken() != null) {
                runCatching { api.registerDeviceToken(DeviceTokenRequestDto(token)) }
            }
        }
    }
}
