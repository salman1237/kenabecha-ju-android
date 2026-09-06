package com.kenabechaju.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material3's [androidx.compose.material3.ColorScheme] has no success/
 * warning/info roles, but the web app's design tokens (and this app's own
 * status semantics — active/published, pending, sold/out-of-stock) need
 * them. Kept as a small parallel CompositionLocal rather than abusing
 * primary/tertiary/error for meanings they weren't meant to carry.
 */
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val shadowPrimary: Color,
)

val LightExtendedColors = ExtendedColors(
    success = KbLightSuccess,
    onSuccess = KbLightOnSuccess,
    successContainer = KbLightSuccessContainer,
    onSuccessContainer = KbLightOnSuccessContainer,
    warning = KbLightWarning,
    onWarning = KbLightOnWarning,
    warningContainer = KbLightWarningContainer,
    onWarningContainer = KbLightOnWarningContainer,
    info = KbLightInfo,
    onInfo = KbLightOnInfo,
    infoContainer = KbLightInfoContainer,
    onInfoContainer = KbLightOnInfoContainer,
    shadowPrimary = KbShadowPrimaryLight,
)

val DarkExtendedColors = ExtendedColors(
    success = KbDarkSuccess,
    onSuccess = KbDarkOnSuccess,
    successContainer = KbDarkSuccessContainer,
    onSuccessContainer = KbDarkOnSuccessContainer,
    warning = KbDarkWarning,
    onWarning = KbDarkOnWarning,
    warningContainer = KbDarkWarningContainer,
    onWarningContainer = KbDarkOnWarningContainer,
    info = KbDarkInfo,
    onInfo = KbDarkOnInfo,
    infoContainer = KbDarkInfoContainer,
    onInfoContainer = KbDarkOnInfoContainer,
    shadowPrimary = KbShadowPrimaryDark,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Mirrors `MaterialTheme.colorScheme` for the roles Material3 doesn't have: `KbColors.success`, etc. */
object KbColors {
    val success: Color @Composable get() = LocalExtendedColors.current.success
    val onSuccess: Color @Composable get() = LocalExtendedColors.current.onSuccess
    val successContainer: Color @Composable get() = LocalExtendedColors.current.successContainer
    val onSuccessContainer: Color @Composable get() = LocalExtendedColors.current.onSuccessContainer
    val warning: Color @Composable get() = LocalExtendedColors.current.warning
    val onWarning: Color @Composable get() = LocalExtendedColors.current.onWarning
    val warningContainer: Color @Composable get() = LocalExtendedColors.current.warningContainer
    val onWarningContainer: Color @Composable get() = LocalExtendedColors.current.onWarningContainer
    val info: Color @Composable get() = LocalExtendedColors.current.info
    val onInfo: Color @Composable get() = LocalExtendedColors.current.onInfo
    val infoContainer: Color @Composable get() = LocalExtendedColors.current.infoContainer
    val onInfoContainer: Color @Composable get() = LocalExtendedColors.current.onInfoContainer
    val shadowPrimary: Color @Composable get() = LocalExtendedColors.current.shadowPrimary
}
