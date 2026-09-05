package com.kenabechaju.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Every ColorScheme role set explicitly, deliberately — a scheme built with
 * only the "obviously used" roles filled in silently falls back to
 * Material3's own default purple for everything else (surfaceContainer*,
 * outline, etc.), which only shows up once some later screen happens to use
 * a role this pass didn't anticipate. Cheaper to set all of them once, here.
 */
private val KbLightColors = lightColorScheme(
    primary = KbLightPrimary,
    onPrimary = KbLightOnPrimary,
    primaryContainer = KbLightPrimaryContainer,
    onPrimaryContainer = KbLightOnPrimaryContainer,
    inversePrimary = KbDarkPrimary,
    secondary = KbLightSecondary,
    onSecondary = KbLightOnSecondary,
    secondaryContainer = KbLightSecondary,
    onSecondaryContainer = KbLightOnSecondary,
    tertiary = KbLightTertiary,
    onTertiary = KbLightOnTertiary,
    tertiaryContainer = KbLightTertiaryContainer,
    onTertiaryContainer = KbLightOnTertiaryContainer,
    background = KbLightBackground,
    onBackground = KbLightForeground,
    surface = KbLightBackground,
    onSurface = KbLightForeground,
    surfaceVariant = KbLightMuted,
    onSurfaceVariant = KbLightMutedForeground,
    surfaceTint = KbLightPrimary,
    inverseSurface = KbDarkBackground,
    inverseOnSurface = KbDarkForeground,
    error = KbLightError,
    onError = KbLightOnError,
    errorContainer = KbLightErrorContainer,
    onErrorContainer = KbLightOnErrorContainer,
    outline = KbLightMutedForeground,
    outlineVariant = KbLightBorder,
    scrim = KbLightForeground,
    surfaceContainer = KbLightCard,
    surfaceContainerLow = KbLightBackground,
    surfaceContainerLowest = KbLightBackground,
    surfaceContainerHigh = KbLightMuted,
    surfaceContainerHighest = KbLightMuted,
    surfaceDim = KbLightMuted,
    surfaceBright = KbLightBackground,
)

private val KbDarkColors = darkColorScheme(
    primary = KbDarkPrimary,
    onPrimary = KbDarkOnPrimary,
    primaryContainer = KbDarkPrimaryContainer,
    onPrimaryContainer = KbDarkOnPrimaryContainer,
    inversePrimary = KbLightPrimary,
    secondary = KbDarkSecondary,
    onSecondary = KbDarkOnSecondary,
    secondaryContainer = KbDarkSecondary,
    onSecondaryContainer = KbDarkOnSecondary,
    tertiary = KbDarkTertiary,
    onTertiary = KbDarkOnTertiary,
    tertiaryContainer = KbDarkTertiaryContainer,
    onTertiaryContainer = KbDarkOnTertiaryContainer,
    background = KbDarkBackground,
    onBackground = KbDarkForeground,
    surface = KbDarkBackground,
    onSurface = KbDarkForeground,
    surfaceVariant = KbDarkMuted,
    onSurfaceVariant = KbDarkMutedForeground,
    surfaceTint = KbDarkPrimary,
    inverseSurface = KbLightBackground,
    inverseOnSurface = KbLightForeground,
    error = KbDarkError,
    onError = KbDarkOnError,
    errorContainer = KbDarkErrorContainer,
    onErrorContainer = KbDarkOnErrorContainer,
    outline = KbDarkMutedForeground,
    outlineVariant = KbDarkBorder,
    scrim = KbDarkForeground,
    surfaceContainer = KbDarkCard,
    surfaceContainerLow = KbDarkBackground,
    surfaceContainerLowest = KbDarkBackground,
    surfaceContainerHigh = KbDarkMuted,
    surfaceContainerHighest = KbDarkMuted,
    surfaceDim = KbDarkBackground,
    surfaceBright = KbDarkMuted,
)

@Composable
fun KenaBechaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // dynamicColor deliberately never used: Android 12+'s Material You would
    // otherwise silently override this whole scheme with wallpaper-derived
    // colors, which is exactly how the reference app's first build ended up
    // looking like generic Material Design instead of the actual brand.
    val colorScheme = if (darkTheme) KbDarkColors else KbLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KenaBechaTypography,
        shapes = KenaBechaShapes,
        content = content,
    )
}
