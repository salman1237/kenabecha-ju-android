package com.kenabechaju.app.data.remote

import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.CompleteProfileRequestDto
import com.kenabechaju.app.data.remote.dto.ConversationDto
import com.kenabechaju.app.data.remote.dto.MessageDto
import com.kenabechaju.app.data.remote.dto.NotificationListDto
import com.kenabechaju.app.data.remote.dto.NotificationDto
import com.kenabechaju.app.data.remote.dto.RatingCreateRequestDto
import com.kenabechaju.app.data.remote.dto.RatingDto
import com.kenabechaju.app.data.remote.dto.RatingEligibilityDto
import com.kenabechaju.app.data.remote.dto.ReportCreateRequestDto
import com.kenabechaju.app.data.remote.dto.SendMessageRequestDto
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
import com.kenabechaju.app.data.remote.dto.PostCreateRequestDto
import com.kenabechaju.app.data.remote.dto.PostDto
import com.kenabechaju.app.data.remote.dto.PostImageDto
import com.kenabechaju.app.data.remote.dto.PostUpdateRequestDto
import com.kenabechaju.app.data.remote.dto.FollowToggleDto
import com.kenabechaju.app.data.remote.dto.ResendOtpRequestDto
import com.kenabechaju.app.data.remote.dto.ResetPasswordRequestDto
import com.kenabechaju.app.data.remote.dto.SavedToggleDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto
import com.kenabechaju.app.data.remote.dto.ShopCreateRequestDto
import com.kenabechaju.app.data.remote.dto.ShopDto
import com.kenabechaju.app.data.remote.dto.ShopStatsDto
import com.kenabechaju.app.data.remote.dto.ShopUpdateRequestDto
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

    @POST("shops")
    suspend fun createShop(@Body body: ShopCreateRequestDto): ShopDto

    @GET("shops")
    suspend fun listShops(@Query("skip") skip: Int = 0, @Query("limit") limit: Int = 50): List<ShopDto>

    @GET("shops/mine")
    suspend fun listMyShops(): List<ShopDto>

    @GET("shops/{slug}")
    suspend fun getShop(@Path("slug") slug: String): ShopDto

    @GET("shops/{slug}/stats")
    suspend fun getShopStats(@Path("slug") slug: String): ShopStatsDto

    @POST("shops/{shopId}/follow")
    suspend fun toggleFollowShop(@Path("shopId") shopId: String): FollowToggleDto

    @GET("shops/{slug}/reviews")
    suspend fun getShopReviews(@Path("slug") slug: String): List<RatingDto>

    @retrofit2.http.PATCH("shops/{shopId}")
    suspend fun updateShop(@Path("shopId") shopId: String, @Body body: ShopUpdateRequestDto): ShopDto

    @Multipart
    @POST("shops/{shopId}/logo")
    suspend fun uploadShopLogo(@Path("shopId") shopId: String, @Part file: MultipartBody.Part): ShopDto

    @Multipart
    @POST("shops/{shopId}/cover")
    suspend fun uploadShopCover(@Path("shopId") shopId: String, @Part file: MultipartBody.Part): ShopDto

    @retrofit2.http.DELETE("shops/{shopId}")
    suspend fun deleteShop(@Path("shopId") shopId: String)

    @POST("posts")
    suspend fun createPost(@Body body: PostCreateRequestDto): PostDto

    @GET("posts/feed")
    suspend fun getPostsFeed(@Query("limit") limit: Int = 24, @Query("offset") offset: Int = 0): PageDto<PostDto>

    @GET("posts/shop/{shopId}")
    suspend fun getShopPosts(@Path("shopId") shopId: String): List<PostDto>

    @GET("posts/mine")
    suspend fun getMyPosts(@Query("shop_id") shopId: String): List<PostDto>

    @GET("posts/{slug}")
    suspend fun getPostBySlug(@Path("slug") slug: String): PostDto

    @retrofit2.http.PATCH("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body body: PostUpdateRequestDto): PostDto

    @retrofit2.http.DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String)

    @Multipart
    @POST("posts/{id}/images")
    suspend fun uploadPostImage(@Path("id") id: String, @Part file: MultipartBody.Part): PostImageDto

    @retrofit2.http.DELETE("posts/{id}/images/{imageId}")
    suspend fun deletePostImage(@Path("id") id: String, @Path("imageId") imageId: String)

    @POST("listings/{listingId}/contact")
    suspend fun contactSeller(@Path("listingId") listingId: String): ConversationDto

    @GET("conversations")
    suspend fun listConversations(@Query("shop_id") shopId: String? = null): List<ConversationDto>

    @GET("conversations/{id}")
    suspend fun getConversation(@Path("id") id: String): ConversationDto

    @GET("conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") id: String,
        @Query("before") before: String? = null,
        @Query("limit") limit: Int = 50,
    ): List<MessageDto>

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") id: String, @Body body: SendMessageRequestDto): MessageDto

    @POST("conversations/{id}/read")
    suspend fun markConversationRead(@Path("id") id: String)

    @Multipart
    @POST("conversations/{id}/attachments")
    suspend fun sendAttachment(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("caption") caption: okhttp3.RequestBody,
    ): MessageDto

    @GET("notifications")
    suspend fun getNotifications(): NotificationListDto

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): NotificationDto

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead()

    @GET("listings/{listingId}/rating-eligibility")
    suspend fun getRatingEligibility(@Path("listingId") listingId: String): RatingEligibilityDto

    @POST("listings/{listingId}/ratings")
    suspend fun createRating(@Path("listingId") listingId: String, @Body body: RatingCreateRequestDto): RatingDto

    @POST("reports")
    suspend fun createReport(@Body body: ReportCreateRequestDto)
}
