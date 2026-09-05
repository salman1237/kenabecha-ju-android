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
    const val MY_LISTINGS = "my_listings"
    const val LISTING_FORM = "listing_form?listingId={listingId}"

    fun otpVerify(email: String) = "otp_verify/${java.net.URLEncoder.encode(email, "UTF-8")}"
    fun listingDetail(listingId: String) = "listing/$listingId"
    fun editListing(listingId: String) = "listing_form?listingId=$listingId"
}
