package com.kenabechaju.app.data

import android.content.Context
import android.net.Uri
import com.kenabechaju.app.data.remote.ApiService
import com.kenabechaju.app.data.remote.dto.PageDto
import com.kenabechaju.app.data.remote.dto.PostCreateRequestDto
import com.kenabechaju.app.data.remote.dto.PostDto
import com.kenabechaju.app.data.remote.dto.PostImageDto
import com.kenabechaju.app.data.remote.dto.PostUpdateRequestDto
import com.kenabechaju.app.data.remote.toApiFailure
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context,
) {
    suspend fun createPost(shopId: String, title: String, descriptionHtml: String, listingIds: List<String>): AuthResult<PostDto> =
        resultOf { api.createPost(PostCreateRequestDto(shopId, title, descriptionHtml, listingIds)) }

    suspend fun feed(limit: Int = 24, offset: Int = 0): AuthResult<PageDto<PostDto>> = resultOf {
        api.getPostsFeed(limit, offset)
    }

    suspend fun shopPosts(shopId: String): AuthResult<List<PostDto>> = resultOf { api.getShopPosts(shopId) }

    suspend fun myPosts(shopId: String): AuthResult<List<PostDto>> = resultOf { api.getMyPosts(shopId) }

    suspend fun getPost(slug: String): AuthResult<PostDto> = resultOf { api.getPostBySlug(slug) }

    suspend fun updatePost(
        postId: String,
        title: String?,
        descriptionHtml: String?,
        listingIds: List<String>?,
    ): AuthResult<PostDto> = resultOf {
        api.updatePost(postId, PostUpdateRequestDto(title, descriptionHtml, listingIds))
    }

    suspend fun deletePost(postId: String): AuthResult<Unit> = resultOf { api.deletePost(postId) }

    suspend fun uploadImage(postId: String, uri: Uri): AuthResult<PostImageDto> = resultOf {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not read the selected image")
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "post.jpg", body)
        api.uploadPostImage(postId, part)
    }

    suspend fun deleteImage(postId: String, imageId: String): AuthResult<Unit> = resultOf {
        api.deletePostImage(postId, imageId)
    }
}

private suspend fun <T> resultOf(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (e: Exception) {
    AuthResult.Failure(e.toApiFailure())
}
