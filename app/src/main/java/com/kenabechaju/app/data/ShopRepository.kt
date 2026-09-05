package com.kenabechaju.app.data

import android.content.Context
import android.net.Uri
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.RatingDto
import com.kenabechaju.app.data.remote.dto.ShopCreateRequestDto
import com.kenabechaju.app.data.remote.dto.ShopDto
import com.kenabechaju.app.data.remote.dto.ShopStatsDto
import com.kenabechaju.app.data.remote.dto.ShopUpdateRequestDto
import com.kenabechaju.app.data.remote.toApiFailure
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context,
) {
    suspend fun createShop(name: String, description: String?, shopType: String?): AuthResult<ShopDto> = resultOf {
        api.createShop(ShopCreateRequestDto(name, description, shopType))
    }

    suspend fun listShops(): AuthResult<List<ShopDto>> = resultOf { api.listShops() }

    suspend fun myShops(): AuthResult<List<ShopDto>> = resultOf { api.listMyShops() }

    suspend fun getShop(slug: String): AuthResult<ShopDto> = resultOf { api.getShop(slug) }

    suspend fun getShopStats(slug: String): AuthResult<ShopStatsDto> = resultOf { api.getShopStats(slug) }

    suspend fun toggleFollow(shopId: String): AuthResult<Boolean> = resultOf {
        api.toggleFollowShop(shopId).following
    }

    suspend fun getShopReviews(slug: String): AuthResult<List<RatingDto>> = resultOf { api.getShopReviews(slug) }

    suspend fun updateShop(
        shopId: String,
        name: String?,
        description: String?,
        shopType: String?,
    ): AuthResult<ShopDto> = resultOf {
        api.updateShop(shopId, ShopUpdateRequestDto(name, description, shopType))
    }

    suspend fun deleteShop(shopId: String): AuthResult<Unit> = resultOf { api.deleteShop(shopId) }

    suspend fun uploadLogo(shopId: String, uri: Uri): AuthResult<ShopDto> = resultOf {
        api.uploadShopLogo(shopId, uriToPart(uri))
    }

    suspend fun uploadCover(shopId: String, uri: Uri): AuthResult<ShopDto> = resultOf {
        api.uploadShopCover(shopId, uriToPart(uri))
    }

    private fun uriToPart(uri: Uri): MultipartBody.Part {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected image")
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "shop.jpg", body)
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
