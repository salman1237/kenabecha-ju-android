package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.remote.dto.CompleteProfileRequestDto
import com.kenabechaju.app.data.remote.dto.DepartmentDto
import com.kenabechaju.app.data.remote.dto.ForgotPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.GoogleLoginRequestDto
import com.kenabechaju.app.data.remote.dto.HallDto
import com.kenabechaju.app.data.remote.dto.LoginRequestDto
import com.kenabechaju.app.data.remote.dto.ResendOtpRequestDto
import com.kenabechaju.app.data.remote.dto.ResetPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto
import com.kenabechaju.app.data.remote.dto.SignupRequestDto
import com.kenabechaju.app.data.remote.dto.TokenResponseDto
import com.kenabechaju.app.data.remote.dto.UpdateProfileRequestDto
import com.kenabechaju.app.data.remote.dto.UpdateWhatsAppRequestDto
import com.kenabechaju.app.data.remote.dto.UserDto
import com.kenabechaju.app.data.remote.dto.VerifyOtpRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Mirrors app/routers/auth.py and app/routers/reference.py 1:1 — same
 * paths, same request/response shapes, so there is never a second source
 * of truth to keep in sync with the backend by hand.
 */
interface ApiService {
    @GET("reference/halls")
    suspend fun getHalls(): List<HallDto>

    @GET("reference/departments")
    suspend fun getDepartments(): List<DepartmentDto>

    @GET("reference/sessions")
    suspend fun getSessions(): List<SessionOptionDto>

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequestDto): UserDto

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyOtpRequestDto): UserDto

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequestDto)

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenResponseDto

    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequestDto): TokenResponseDto

    // Explicit header, not the generic per-request AuthInterceptor: this
    // call authenticates with the *refresh* token, never the access token
    // the interceptor would otherwise attach to every other request.
    @POST("auth/refresh")
    suspend fun refresh(@Header("Authorization") refreshBearer: String): TokenResponseDto

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") refreshBearer: String)

    @GET("auth/me")
    suspend fun me(): UserDto

    @retrofit2.http.PATCH("auth/complete-profile")
    suspend fun completeProfile(@Body body: CompleteProfileRequestDto): UserDto

    @retrofit2.http.PATCH("auth/whatsapp")
    suspend fun updateWhatsApp(@Body body: UpdateWhatsAppRequestDto): UserDto

    @retrofit2.http.PATCH("auth/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): UserDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto)

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto)
}
