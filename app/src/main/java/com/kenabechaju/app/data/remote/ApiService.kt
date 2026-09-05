package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.CompleteProfileRequestDto
import com.kenabechaju.app.data.remote.dto.DashboardStatsDto
import com.kenabechaju.app.data.remote.dto.DepartmentDto
import com.kenabechaju.app.data.remote.dto.ForgotPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.GoogleLoginRequestDto
import com.kenabechaju.app.data.remote.dto.HallDto
import com.kenabechaju.app.data.remote.dto.ListingCreateRequestDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.ListingImageDto
import com.kenabechaju.app.data.remote.dto.ListingUpdateRequestDto
import com.kenabechaju.app.data.remote.dto.LoginRequestDto
import com.kenabechaju.app.data.remote.dto.PageDto
import com.kenabechaju.app.data.remote.dto.ResendOtpRequestDto
import com.kenabechaju.app.data.remote.dto.ResetPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.SavedToggleDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto
import com.kenabechaju.app.data.remote.dto.SignupRequestDto
import com.kenabechaju.app.data.remote.dto.TagDto
import com.kenabechaju.app.data.remote.dto.TokenResponseDto
import com.kenabechaju.app.data.remote.dto.UpdateProfileRequestDto
import com.kenabechaju.app.data.remote.dto.UpdateWhatsAppRequestDto
import com.kenabechaju.app.data.remote.dto.UserDto
import com.kenabechaju.app.data.remote.dto.UserProfileDto
import com.kenabechaju.app.data.remote.dto.VerifyOtpRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

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

    @Multipart
    @POST("auth/avatar")
    suspend fun updateAvatar(@Part file: MultipartBody.Part): UserDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto)

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto)

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("tags/trending")
    suspend fun getTrendingTags(): List<TagDto>

    @GET("listings")
    suspend fun browseListings(@QueryMap filters: Map<String, String>, @Query("tags") tags: List<String>): PageDto<ListingDto>

    @GET("listings/suggestions")
    suspend fun getSearchSuggestions(@Query("q") q: String, @Query("limit") limit: Int = 5): List<String>

    @GET("listings/{id}")
    suspend fun getListing(@Path("id") id: String): ListingDto

    @GET("listings/{id}/related")
    suspend fun getRelatedListings(@Path("id") id: String, @Query("limit") limit: Int = 8): List<ListingDto>

    @GET("dashboard/saved/ids")
    suspend fun getSavedIds(): List<String>

    @POST("dashboard/saved/{listingId}")
    suspend fun toggleSaved(@Path("listingId") listingId: String): SavedToggleDto

    @GET("listings/mine")
    suspend fun listMyListings(@Query("shop_id") shopId: String? = null): List<ListingDto>

    @POST("listings")
    suspend fun createListing(@Body body: ListingCreateRequestDto): ListingDto

    @retrofit2.http.PATCH("listings/{id}")
    suspend fun updateListing(@Path("id") id: String, @Body body: ListingUpdateRequestDto): ListingDto

    @retrofit2.http.DELETE("listings/{id}")
    suspend fun deleteListing(@Path("id") id: String)

    @POST("listings/{id}/mark-sold")
    suspend fun markSold(@Path("id") id: String): ListingDto

    @POST("listings/{id}/mark-out-of-stock")
    suspend fun markOutOfStock(@Path("id") id: String): ListingDto

    @POST("listings/{id}/mark-available")
    suspend fun markAvailable(@Path("id") id: String): ListingDto

    @POST("listings/{id}/pause")
    suspend fun pauseListing(@Path("id") id: String): ListingDto

    @POST("listings/{id}/relist")
    suspend fun relistListing(@Path("id") id: String): ListingDto

    @POST("listings/{id}/renew")
    suspend fun renewListing(@Path("id") id: String): ListingDto

    @Multipart
    @POST("listings/{id}/images")
    suspend fun uploadListingImage(@Path("id") id: String, @Part file: MultipartBody.Part): ListingImageDto

    @retrofit2.http.DELETE("listings/{id}/images/{imageId}")
    suspend fun deleteListingImage(@Path("id") id: String, @Path("imageId") imageId: String)

    @GET("dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsDto

    @GET("dashboard/saved")
    suspend fun getSavedListings(): List<ListingDto>

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): UserProfileDto
}
