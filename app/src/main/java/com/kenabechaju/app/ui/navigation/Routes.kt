package com.kenabechaju.app.ui.navigation

/** Central route constants — every NavHost destination and every
 *  cross-screen navigation call references these, never a raw string, so
 *  a route never drifts between where it's declared and where it's used. */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val OTP_VERIFY = "otp_verify/{email}"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val ACCOUNT_SETTINGS = "account_settings"
    const val COMPLETE_PROFILE = "complete_profile"
    const val LISTING_DETAIL = "listing/{listingId}"
    const val MY_LISTINGS = "my_listings?shopId={shopId}"
    const val LISTING_FORM = "listing_form?listingId={listingId}"
    const val DASHBOARD = "dashboard"
    const val SAVED_LISTINGS = "saved_listings"
    const val PUBLIC_PROFILE = "profile/{userId}"
    const val SHOP_STOREFRONT = "shop/{slug}"
    const val BROWSE_SHOPS = "browse_shops"
    const val MY_SHOPS = "my_shops"
    const val SHOP_FORM = "shop_form?shopId={shopId}"
    const val POSTS_FEED = "posts_feed"
    const val POST_DETAIL = "post/{slug}"
    const val MY_SHOP_POSTS = "my_shop_posts/{shopId}"
    const val POST_COMPOSER = "post_composer/{shopId}?postId={postId}"
    const val INBOX = "inbox"
    const val CHAT_THREAD = "chat/{conversationId}"
    const val NOTIFICATIONS = "notifications"
    const val ASSISTANT = "assistant"

    fun otpVerify(email: String) = "otp_verify/${java.net.URLEncoder.encode(email, "UTF-8")}"
    fun chatThread(conversationId: String) = "chat/$conversationId"
    fun listingDetail(listingId: String) = "listing/$listingId"
    // Bare path, no "?listingId=" suffix at all — matches the create case:
    // navigating with the raw LISTING_FORM constant would send the literal,
    // unsubstituted "{listingId}" placeholder text as the argument value
    // instead of leaving it absent, a real bug caught via a live on-device
    // test (a brand-new listing's screen showed "Edit listing" because
    // listingId resolved to the string "{listingId}", not null).
    const val CREATE_LISTING = "listing_form"
    fun editListing(listingId: String) = "listing_form?listingId=$listingId"
    // Bare path with no shopId -- same reasoning as CREATE_LISTING above:
    // the "all my listings" entry point from Account Settings must never
    // send the literal unsubstituted "{shopId}" text as the argument.
    const val ALL_MY_LISTINGS = "my_listings"
    fun myListingsForShop(shopId: String) = "my_listings?shopId=$shopId"
    fun publicProfile(userId: String) = "profile/$userId"
    fun shopStorefront(slug: String) = "shop/$slug"
    const val CREATE_SHOP = "shop_form"
    fun editShop(shopId: String) = "shop_form?shopId=$shopId"
    fun postDetail(slug: String) = "post/$slug"
    fun myShopPosts(shopId: String) = "my_shop_posts/$shopId"
    fun createPost(shopId: String) = "post_composer/$shopId"
    fun editPost(shopId: String, postId: String) = "post_composer/$shopId?postId=$postId"
}
