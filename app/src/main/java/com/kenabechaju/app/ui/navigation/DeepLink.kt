package com.kenabechaju.app.ui.navigation

/**
 * Maps a backend-issued `link_url` (a *web* path, e.g. "/listings/abc" --
 * notification_service.py's callers always build one of these, web just
 * routes it directly) to the equivalent Android route, so tapping a
 * notification -- the in-app list or a system push -- lands on the actual
 * relevant screen instead of just opening the app wherever it happened to
 * already be.
 *
 * Returns null for a link with no Android screen to land on yet (shop
 * collaborator invites have no dedicated screen on Android at all) --
 * callers fall back to just opening the app rather than guessing.
 */
fun resolveDeepLink(linkUrl: String): String? {
    val segments = linkUrl.trim().trim('/').split("/").filter { it.isNotEmpty() }
    return when {
        segments.size == 2 && segments[0] == "inbox" -> Routes.chatThread(segments[1])
        segments.size == 2 && segments[0] == "listings" -> Routes.listingDetail(segments[1])
        segments.size == 1 && segments[0] == "listings" -> Routes.HOME
        // Checked before the generic two-segment "shops/{slug}" case below.
        segments.size == 2 && segments[0] == "shops" && segments[1] == "dashboard" -> Routes.MY_SHOPS
        segments.size == 2 && segments[0] == "shops" && segments[1] == "invites" -> Routes.MY_SHOPS
        segments.size == 2 && segments[0] == "shops" -> Routes.shopStorefront(segments[1])
        segments.size == 2 && segments[0] == "profile" -> Routes.publicProfile(segments[1])
        segments.size == 2 && segments[0] == "posts" -> Routes.postDetail(segments[1])
        else -> null
    }
}
