package com.kenabechaju.app

import android.app.Application
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
    }
}
