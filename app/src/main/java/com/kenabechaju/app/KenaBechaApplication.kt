package com.kenabechaju.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kenabechaju.app.data.remote.ChatSocket
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KenaBechaApplication : Application() {

    @Inject lateinit var chatSocket: ChatSocket

    override fun onCreate() {
        super.onCreate()
        // Registers a ProcessLifecycleOwner observer that connects/disconnects
        // the single global chat socket as the app foregrounds/backgrounds —
        // started once here rather than per-screen, since the socket must
        // stay alive across chat and non-chat screens alike while foregrounded.
        chatSocket.start()

        // Created eagerly here, not lazily inside the messaging service's
        // onMessageReceived() — that callback never runs at all for the
        // backgrounded/killed-app path FCM auto-displays through, so a
        // lazy creation would mean the very first real push (delivered
        // while the app isn't running) lands under a missing/generic
        // channel instead of this one. A real bug class, not hypothetical.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                "General",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
