package com.kenabechaju.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Every value here is lifted directly from the web app's own design tokens
 * (frontend/app/globals.css :root / .dark blocks) — this app's brand is
 * "whatever the web app already is," not a fresh palette invented for
 * Android. Container/variant tones not present in web's flatter token set
 * are derived (lighter/darker steps of the nearest real token), never new
 * hues.
 */

// --- light ---
val KbLightBackground = Color(0xFFFFFFFF)
val KbLightForeground = Color(0xFF09090B)
val KbLightCard = Color(0xFFFFFFFF)
val KbLightPrimary = Color(0xFF059669) // emerald-600
val KbLightOnPrimary = Color(0xFFFFFFFF)
val KbLightPrimaryContainer = Color(0xFFD1FAE5) // emerald-100
val KbLightOnPrimaryContainer = Color(0xFF064E3B) // emerald-900
val KbLightSecondary = Color(0xFFF4F4F5) // zinc-100
val KbLightOnSecondary = Color(0xFF18181B) // zinc-900
val KbLightTertiary = Color(0xFF0D9488) // teal-600, the brand's secondary accent
val KbLightOnTertiary = Color(0xFFFFFFFF)
val KbLightTertiaryContainer = Color(0xFFCCFBF1) // teal-100
val KbLightOnTertiaryContainer = Color(0xFF134E4A) // teal-900
val KbLightMuted = Color(0xFFF4F4F5)
val KbLightMutedForeground = Color(0xFF71717A) // zinc-500
val KbLightBorder = Color(0xFFE4E4E7) // zinc-200
val KbLightError = Color(0xFFDC2626)
val KbLightOnError = Color(0xFFFFFFFF)
val KbLightErrorContainer = Color(0xFFFEE2E2)
val KbLightOnErrorContainer = Color(0xFF7F1D1D)

// --- dark ---
val KbDarkBackground = Color(0xFF09090B)
val KbDarkForeground = Color(0xFFFAFAFA)
val KbDarkCard = Color(0xFF18181B)
val KbDarkPrimary = Color(0xFF10B981) // emerald-500
val KbDarkOnPrimary = Color(0xFF09090B)
val KbDarkPrimaryContainer = Color(0xFF065F46) // emerald-800
val KbDarkOnPrimaryContainer = Color(0xFFD1FAE5)
val KbDarkSecondary = Color(0xFF27272A) // zinc-800
val KbDarkOnSecondary = Color(0xFFFAFAFA)
val KbDarkTertiary = Color(0xFF2DD4BF) // teal-400
val KbDarkOnTertiary = Color(0xFF09090B)
val KbDarkTertiaryContainer = Color(0xFF115E59) // teal-800
val KbDarkOnTertiaryContainer = Color(0xFFCCFBF1)
val KbDarkMuted = Color(0xFF27272A)
val KbDarkMutedForeground = Color(0xFFA1A1AA) // zinc-400
val KbDarkBorder = Color(0xFF27272A)
val KbDarkError = Color(0xFFEF4444)
val KbDarkOnError = Color(0xFF09090B)
val KbDarkErrorContainer = Color(0xFF7F1D1D)
val KbDarkOnErrorContainer = Color(0xFFFEE2E2)
