package com.kenabechaju.app.ui.common

import com.kenabechaju.app.BuildConfig

/**
 * Backend media paths (`image_url`/`avatar_url`/`logo_url`/`cover_url`)
 * come back relative (e.g. `/media/listings/xyz.png`) — matching web's own
 * `lib/utils.ts` `mediaUrl()`, which leaves them relative because the
 * browser resolves them against the page's own origin (Next.js rewrites
 * paths under `/media/` to the backend same-origin). A native client has no page
 * origin to resolve against, so Coil is handed a bare path with no host
 * and silently fails to load it — confirmed via a live on-device test
 * (an uploaded listing photo rendered as nothing, not an error). This
 * prefixes exactly the same way web's function is a no-op for: absolute
 * URLs (Google avatars, etc.) pass through untouched.
 */
fun mediaUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val suffix = if (path.startsWith("/")) path else "/$path"
    return base + suffix
}
