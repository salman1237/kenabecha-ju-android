package com.kenabechaju.app.data

import android.content.Context
import android.net.Uri
import com.google.firebase.messaging.FirebaseMessaging
import com.kenabechaju.app.data.remote.ApiFailure
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.CompleteProfileRequestDto
import com.kenabechaju.app.data.remote.dto.DeviceTokenRequestDto
import com.kenabechaju.app.data.remote.dto.ForgotPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.GoogleLoginRequestDto
import com.kenabechaju.app.data.remote.dto.LoginRequestDto
import com.kenabechaju.app.data.remote.dto.ResendOtpRequestDto
import com.kenabechaju.app.data.remote.dto.ResetPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.SignupRequestDto
import com.kenabechaju.app.data.remote.dto.UpdateProfileRequestDto
import com.kenabechaju.app.data.remote.dto.UpdateWhatsAppRequestDto
import com.kenabechaju.app.data.remote.dto.UserDto
import com.kenabechaju.app.data.remote.dto.VerifyOtpRequestDto
import com.kenabechaju.app.data.remote.toApiFailure
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Every method mirrors one call the web app's own `lib/api/auth.ts` makes
 *  against the identical backend endpoint — never a parallel contract. */
sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val error: ApiFailure) : AuthResult<Nothing>
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    @ApplicationContext private val context: Context,
) {
    suspend fun isLoggedIn(): Boolean = tokenStore.accessToken() != null

    suspend fun signup(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        studentId: String,
        registrationNo: String,
        hallId: String,
        departmentId: String,
        session: String,
    ): AuthResult<UserDto> = resultOf {
        api.signup(
            SignupRequestDto(email, password, fullName, phone, studentId, registrationNo, hallId, departmentId, session)
        )
    }

    suspend fun verifyEmail(email: String, otp: String): AuthResult<UserDto> = resultOf {
        api.verifyEmail(VerifyOtpRequestDto(email, otp))
    }

    suspend fun resendOtp(email: String): AuthResult<Unit> = resultOf {
        api.resendOtp(ResendOtpRequestDto(email))
    }

    suspend fun login(email: String, password: String): AuthResult<UserDto> = resultOf {
        val response = api.login(LoginRequestDto(email, password))
        tokenStore.save(response.accessToken, response.refreshToken)
        syncDeviceToken()
        response.toUser()
    }

    suspend fun googleLogin(idToken: String): AuthResult<UserDto> = resultOf {
        val response = api.googleLogin(GoogleLoginRequestDto(idToken))
        tokenStore.save(response.accessToken, response.refreshToken)
        syncDeviceToken()
        response.toUser()
    }

    suspend fun logout() {
        // Un-registering still needs to authenticate against the backend,
        // so this must happen *before* the local token is cleared below —
        // getting this ordering backwards was a real bug the reference app
        // shipped once (the un-register call had nothing left to sign in
        // with by the time it ran).
        unsyncDeviceToken()
        val refreshToken = tokenStore.refreshToken()
        if (refreshToken != null) {
            runCatching { api.logout("Bearer $refreshToken") }
        }
        tokenStore.clear()
    }

    /** Best-effort: a failed sync just means this device won't get pushes
     *  until the next login, never a user-facing login failure. */
    private suspend fun syncDeviceToken() {
        val token = currentFcmToken() ?: return
        runCatching { api.registerDeviceToken(DeviceTokenRequestDto(token)) }
    }

    private suspend fun unsyncDeviceToken() {
        val token = currentFcmToken() ?: return
        runCatching { api.unregisterDeviceToken(token) }
    }

    private suspend fun currentFcmToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    suspend fun me(): AuthResult<UserDto> = resultOf { api.me() }

    suspend fun completeProfile(
        phone: String,
        studentId: String,
        registrationNo: String,
        hallId: String,
        departmentId: String,
        session: String,
    ): AuthResult<UserDto> = resultOf {
        api.completeProfile(CompleteProfileRequestDto(phone, studentId, registrationNo, hallId, departmentId, session))
    }

    suspend fun updateWhatsApp(number: String?): AuthResult<UserDto> = resultOf {
        api.updateWhatsApp(UpdateWhatsAppRequestDto(number))
    }

    suspend fun updateProfile(fullName: String, bio: String?): AuthResult<UserDto> = resultOf {
        api.updateProfile(UpdateProfileRequestDto(fullName, bio))
    }

    suspend fun forgotPassword(email: String): AuthResult<Unit> = resultOf {
        api.forgotPassword(ForgotPasswordRequestDto(email))
    }

    suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> = resultOf {
        api.resetPassword(ResetPasswordRequestDto(token, newPassword))
    }

    suspend fun updateAvatar(uri: Uri): AuthResult<UserDto> = resultOf {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected image")
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
        api.updateAvatar(part)
    }
}
