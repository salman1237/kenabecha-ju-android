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

    fun otpVerify(email: String) = "otp_verify/${java.net.URLEncoder.encode(email, "UTF-8")}"
}
