package com.kenabechaju.app.data.remote

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kenabechaju.app.MainActivity
import com.kenabechaju.app.R
import com.kenabechaju.app.data.TokenStore
import com.kenabechaju.app.data.remote.dto.DeviceTokenRequestDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `onMessageReceived` only fires for a foregrounded app — FCM auto-displays
 * the system notification itself (via the channel declared in
 * AndroidManifest.xml's `default_notification_channel_id` meta-data)
 * whenever the app is backgrounded or killed, bypassing this method
 * entirely. That auto-display path was verified working; this one wasn't:
 * the original assumption was that a foregrounded app doesn't need a
 * system notification since the same event already arrives live over the
 * chat WebSocket — but "foregrounded" only means *some* screen of the app
 * is open, not that the user is looking at the relevant one, so every push
 * that arrived while the app was simply open (on any screen) was silently
 * dropped with nothing shown at all. Built to match the auto-display path
 * as closely as possible: same channel, same launcher icon, and the same
 * deep-link behavior -- FCM's own auto-display already attaches every
 * `data` key (including `link_url`) as a plain string extra on the Intent
 * it launches with, so using the identical extra key here
 * (MainActivity.EXTRA_LINK_URL) makes both paths resolvable the same way
 * once the Intent reaches MainActivity.
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

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data["link_url"]?.takeIf { it.isNotBlank() }?.let {
                putExtra(MainActivity.EXTRA_LINK_URL, it)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val notificationId = (message.messageId ?: System.currentTimeMillis().toString()).hashCode()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
