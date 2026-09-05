package com.kenabechaju.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.ui.navigation.KenaBechaNavHost
import com.kenabechaju.app.ui.theme.KenaBechaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
