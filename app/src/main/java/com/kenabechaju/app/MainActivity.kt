package com.kenabechaju.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.ui.navigation.KenaBechaNavHost
import com.kenabechaju.app.ui.theme.KenaBechaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            KenaBechaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    // Each screen manages its own padding/scroll — Scaffold
                    // here exists only to paint the theme's background under
                    // system bars, not to reserve content-inset space every
                    // screen would have to duplicate.
                    KenaBechaNavHost(authRepository = authRepository)
                }
            }
        }
    }
}
