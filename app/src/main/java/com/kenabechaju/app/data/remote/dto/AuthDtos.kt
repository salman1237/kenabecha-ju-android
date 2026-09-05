package com.kenabechaju.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors `HallOut` (backend/app/schemas/reference.py) field for field. */
@Serializable
data class HallDto(val id: String, val name: String)

/** Mirrors `DepartmentOut`. */
@Serializable
data class DepartmentDto(val id: String, val name: String, val faculty: String)

/** Mirrors `SessionOption`. */
@Serializable
data class SessionOptionDto(val session: String, val batch: Int)

/**
 * Mirrors `UserPublic` (backend/app/schemas/user.py) exactly — same field
 * names as the JSON the API actually returns, not renamed for Kotlin taste,
 * so there's never a silent mismatch to debug later.
 */
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
    val bio: String? = null,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("registration_no") val registrationNo: String? = null,
    val hall: HallDto? = null,
    val department: DepartmentDto? = null,
    val session: String? = null,
    val batch: Int? = null,
    val role: String,
    @SerialName("auth_provider") val authProvider: String,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("profile_complete") val profileComplete: Boolean,
    @SerialName("created_at") val createdAt: String,
)

/**
 * `TokenResponse` — a `UserDto` plus the two token fields the web app never
 * reads from the body (it relies on the cookies the same response also
 * sets) but this app depends on entirely, having no cookie jar of its own.
 */
@Serializable
data class TokenResponseDto(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
    val bio: String? = null,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("registration_no") val registrationNo: String? = null,
    val hall: HallDto? = null,
    val department: DepartmentDto? = null,
    val session: String? = null,
    val batch: Int? = null,
    val role: String,
    @SerialName("auth_provider") val authProvider: String,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("profile_complete") val profileComplete: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
) {
    fun toUser() = UserDto(
        id, email, fullName, avatarUrl, phone, whatsappNumber, bio, studentId, registrationNo,
        hall, department, session, batch, role, authProvider, isVerified, profileComplete, createdAt,
    )
}

@Serializable
data class SignupRequestDto(
    val email: String,
    val password: String,
    @SerialName("full_name") val fullName: String,
    val phone: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("registration_no") val registrationNo: String,
    @SerialName("hall_id") val hallId: String,
    @SerialName("department_id") val departmentId: String,
    val session: String,
)

@Serializable
data class VerifyOtpRequestDto(val email: String, val otp: String)

@Serializable
data class ResendOtpRequestDto(val email: String)

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class GoogleLoginRequestDto(val credential: String)

@Serializable
data class CompleteProfileRequestDto(
    val phone: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("registration_no") val registrationNo: String,
    @SerialName("hall_id") val hallId: String,
    @SerialName("department_id") val departmentId: String,
    val session: String,
)

@Serializable
data class ForgotPasswordRequestDto(val email: String)

@Serializable
data class ResetPasswordRequestDto(val token: String, @SerialName("new_password") val newPassword: String)

@Serializable
data class UpdateWhatsAppRequestDto(@SerialName("whatsapp_number") val whatsappNumber: String?)

@Serializable
data class UpdateProfileRequestDto(@SerialName("full_name") val fullName: String, val bio: String?)

/** Mirrors the API's `{detail, code}` error shape (app/core/errors.py). */
@Serializable
data class ApiErrorBody(val detail: String? = null, val code: String? = null)
