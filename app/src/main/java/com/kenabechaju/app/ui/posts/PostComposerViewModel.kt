package com.kenabechaju.app.ui.posts

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.PostRepository
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.PostImageDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostComposerUiState(
    val postId: String? = null,
    val shopId: String = "",
    val title: String = "",
    val bodyText: String = "",
    val existingStatus: String? = null,
    val shopListings: List<ListingDto> = emptyList(),
    val selectedListingIds: Set<String> = emptySet(),
    val images: List<PostImageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val isEditing: Boolean get() = postId != null

    /** Matches web's own warning: editing a live or rejected post silently
     *  flips it back to pending server-side — surfaced here so a seller
     *  isn't surprised their edit temporarily un-published something. */
    val willResubmitToPending: Boolean
        get() = isEditing && (existingStatus == "published" || existingStatus == "rejected")
}

/** The composer's rich-text *input* side (bold/italic/underline/color on
 *  selected text) is intentionally not built yet — Compose has no built-in
 *  rich-text editor and the plan explicitly defers that design decision to
 *  when this phase starts. What ships now is a real, working plain-text
 *  composer: each line becomes its own `<p>` (HTML-escaped), which the
 *  backend's sanitizer accepts as-is. Reading rich content authored on web
 *  (bold/color/etc.) already works everywhere via `parsePostHtml` — only
 *  *writing* styled text from the app is the deferred piece. */
@HiltViewModel
class PostComposerViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val listingRepository: ListingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(
        PostComposerUiState(
            postId = savedStateHandle["postId"],
            shopId = checkNotNull(savedStateHandle["shopId"]),
        ),
    )
    val state: StateFlow<PostComposerUiState> = _state.asStateFlow()

    init {
        loadShopListings()
        _state.value.postId?.let(::loadExisting)
    }

    private fun loadShopListings() {
        viewModelScope.launch {
            when (val result = listingRepository.myListings(_state.value.shopId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(shopListings = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadExisting(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            // No GET /posts/{id} by id — reuse /posts/mine for this shop and find it,
            // same pattern CreateEditShopViewModel already uses for shops.
            when (val result = postRepository.myPosts(_state.value.shopId)) {
                is AuthResult.Success -> {
                    val post = result.value.find { it.id == id }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        title = post?.title.orEmpty(),
                        bodyText = post?.let { com.kenabechaju.app.ui.common.htmlToEditableText(it.descriptionHtml) }.orEmpty(),
                        existingStatus = post?.status,
                        selectedListingIds = post?.listings?.map { it.id }?.toSet().orEmpty(),
                        images = post?.images.orEmpty(),
                    )
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun update(block: (PostComposerUiState) -> PostComposerUiState) {
        _state.value = block(_state.value).copy(error = null, saved = false)
    }

    fun toggleListing(listingId: String) {
        val current = _state.value.selectedListingIds
        _state.value = _state.value.copy(
            selectedListingIds = if (listingId in current) current - listingId else current + listingId,
        )
    }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.bodyText.isBlank()) {
            _state.value = s.copy(error = "Title and description are required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)
        val html = com.kenabechaju.app.ui.common.editableTextToHtml(s.bodyText)
        val listingIds = s.selectedListingIds.toList()
        viewModelScope.launch {
            val result = if (s.postId != null) {
                postRepository.updatePost(s.postId, s.title.trim(), html, listingIds)
            } else {
                postRepository.createPost(s.shopId, s.title.trim(), html, listingIds)
            }
            _state.value = when (result) {
                is AuthResult.Success -> _state.value.copy(
                    isSaving = false,
                    saved = true,
                    postId = result.value.id,
                    existingStatus = result.value.status,
                    images = result.value.images,
                )
                is AuthResult.Failure -> _state.value.copy(isSaving = false, error = result.error.message)
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        val id = _state.value.postId ?: return
        _state.value = _state.value.copy(isUploadingPhoto = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = postRepository.uploadImage(id, uri)) {
                is AuthResult.Success -> _state.value.copy(isUploadingPhoto = false, images = _state.value.images + result.value)
                is AuthResult.Failure -> _state.value.copy(isUploadingPhoto = false, error = result.error.message)
            }
        }
    }

    fun deletePhoto(imageId: String) {
        val id = _state.value.postId ?: return
        val previous = _state.value.images
        _state.value = _state.value.copy(images = previous.filter { it.id != imageId })
        viewModelScope.launch {
            when (val result = postRepository.deleteImage(id, imageId)) {
                is AuthResult.Failure -> _state.value = _state.value.copy(images = previous, error = result.error.message)
                is AuthResult.Success -> Unit
            }
        }
    }
}

