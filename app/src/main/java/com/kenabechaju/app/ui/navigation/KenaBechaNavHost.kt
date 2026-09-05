package com.kenabechaju.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.ui.auth.LoginScreen
import com.kenabechaju.app.ui.auth.OtpVerifyScreen
import com.kenabechaju.app.ui.auth.SignupScreen
import com.kenabechaju.app.ui.home.HomeScreen

@Composable
private fun StartupGate(authRepository: AuthRepository, onResolved: (loggedIn: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        onResolved(authRepository.isLoggedIn())
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun KenaBechaNavHost(authRepository: AuthRepository) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    if (startDestination == null) {
        StartupGate(authRepository) { loggedIn ->
            startDestination = if (loggedIn) Routes.HOME else Routes.LOGIN
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                },
                onGoToSignup = { navController.navigate(Routes.SIGNUP) },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignedUp = { email -> navController.navigate(Routes.otpVerify(email)) },
                onGoToLogin = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.OTP_VERIFY,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) {
            OtpVerifyScreen(
                onVerified = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.SIGNUP) { inclusive = true } }
                },
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            // Phase 1 follow-up — the request-reset screen itself.
            androidx.compose.material3.Text(
                "Forgot password — coming right after this screen set.",
                modifier = Modifier.padding(24.dp),
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
            )
        }
    }
}
