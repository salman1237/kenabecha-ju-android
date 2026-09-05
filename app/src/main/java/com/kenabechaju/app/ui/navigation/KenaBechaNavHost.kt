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
import com.kenabechaju.app.ui.auth.ForgotPasswordScreen
import com.kenabechaju.app.ui.auth.LoginScreen
import com.kenabechaju.app.ui.auth.OtpVerifyScreen
import com.kenabechaju.app.ui.auth.SignupScreen
import com.kenabechaju.app.ui.listings.BrowseScreen
import com.kenabechaju.app.ui.listings.CreateEditListingScreen
import com.kenabechaju.app.ui.listings.ListingDetailScreen
import com.kenabechaju.app.ui.listings.MyListingsScreen
import com.kenabechaju.app.ui.profile.AccountSettingsScreen
import com.kenabechaju.app.ui.profile.CompleteProfileScreen

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
                onGoogleSignedIn = { navController.navigate(Routes.HOME) { popUpTo(0) } },
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
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            BrowseScreen(
                onListingClick = { id -> navController.navigate(Routes.listingDetail(id)) },
                onAccountClick = { navController.navigate(Routes.ACCOUNT_SETTINGS) },
                onMyListingsClick = { navController.navigate(Routes.MY_LISTINGS) },
            )
        }
        composable(Routes.MY_LISTINGS) {
            MyListingsScreen(
                onBack = { navController.popBackStack() },
                onCreateListing = { navController.navigate(Routes.LISTING_FORM) },
                onEditListing = { id -> navController.navigate(Routes.editListing(id)) },
            )
        }
        composable(
            route = Routes.LISTING_FORM,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            CreateEditListingScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ACCOUNT_SETTINGS) {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() },
                onCompleteProfile = { navController.navigate(Routes.COMPLETE_PROFILE) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
            )
        }
        composable(Routes.COMPLETE_PROFILE) {
            CompleteProfileScreen(onCompleted = { navController.popBackStack() })
        }
        composable(
            route = Routes.LISTING_DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType }),
        ) {
            ListingDetailScreen(
                onBack = { navController.popBackStack() },
                onRelatedClick = { id ->
                    navController.navigate(Routes.listingDetail(id))
                },
            )
        }
    }
}
