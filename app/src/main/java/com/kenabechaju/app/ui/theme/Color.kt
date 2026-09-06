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

// --- semantic extras (matches web's --success/--warning/--info, absent
// from Material3's own ColorScheme roles — see ExtendedColors.kt) ---
val KbLightSuccess = Color(0xFF16A34A) // green-600
val KbLightOnSuccess = Color(0xFFFFFFFF)
val KbLightSuccessContainer = Color(0xFFDCFCE7) // green-100
val KbLightOnSuccessContainer = Color(0xFF14532D) // green-900
val KbLightWarning = Color(0xFFD97706) // amber-600
val KbLightOnWarning = Color(0xFFFFFFFF)
val KbLightWarningContainer = Color(0xFFFEF3C7) // amber-100
val KbLightOnWarningContainer = Color(0xFF78350F) // amber-900
val KbLightInfo = Color(0xFF2563EB) // blue-600
val KbLightOnInfo = Color(0xFFFFFFFF)
val KbLightInfoContainer = Color(0xFFDBEAFE) // blue-100
val KbLightOnInfoContainer = Color(0xFF1E3A8A) // blue-900

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

// --- semantic extras (matches web's --success/--warning/--info) ---
val KbDarkSuccess = Color(0xFF22C55E) // green-500, matches web's dark --success
val KbDarkOnSuccess = Color(0xFF09090B)
val KbDarkSuccessContainer = Color(0xFF166534) // green-800
val KbDarkOnSuccessContainer = Color(0xFFDCFCE7)
val KbDarkWarning = Color(0xFFFBBF24) // amber-400, matches web's dark --warning
val KbDarkOnWarning = Color(0xFF09090B)
val KbDarkWarningContainer = Color(0xFF92400E) // amber-800
val KbDarkOnWarningContainer = Color(0xFFFEF3C7)
val KbDarkInfo = Color(0xFF60A5FA) // blue-400, matches web's dark --info
val KbDarkOnInfo = Color(0xFF09090B)
val KbDarkInfoContainer = Color(0xFF1E40AF) // blue-800
val KbDarkOnInfoContainer = Color(0xFFDBEAFE)

// Brand-tinted soft shadow, approximating web's `--shadow-soft-primary`
// (rgb(16 185 129 / 0.12)) — used by KbCard for the "glow" elevated look.
val KbShadowPrimaryLight = Color(0x1F059669)
val KbShadowPrimaryDark = Color(0x2610B981)
