package com.kenabechaju.app.ui.listings

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ListingFormInput
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ListingImageDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEditListingUiState(
    val listingId: String? = null,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val priceType: String = "fixed",
    val unit: String = "",
    val condition: String = "used_good",
    val tagsText: String = "",
    val fulfillmentType: String = "pickup",
    val pickupAddress: String = "",
    val categories: List<CategoryDto> = emptyList(),
    val categoryId: String? = null,
    val customCategory: String = "",
    val images: List<ListingImageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val isEditing: Boolean get() = listingId != null
}

/** One screen for both create (POST /listings) and edit (PATCH
 *  /listings/{id}) — same field set either way. Photos can only be added
 *  once the listing exists server-side, so on a fresh create the photo
 *  section appears the moment `listingId` is populated by a successful
 *  save, without leaving the screen. */
@HiltViewModel
class CreateEditListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(
        CreateEditListingUiState(listingId = savedStateHandle["listingId"]),
    )
    val state: StateFlow<CreateEditListingUiState> = _state.asStateFlow()

    init {
        loadCategories()
        _state.value.listingId?.let(::loadExisting)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = listingRepository.categories()) {
                is AuthResult.Success -> _state.value = _state.value.copy(categories = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadExisting(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = listingRepository.getListing(id)) {
                is AuthResult.Success -> {
                    val l = result.value
                    _state.value = _state.value.copy(
                        isLoading = false,
                        title = l.title,
                        description = l.description,
                        price = l.price.orEmpty(),
                        priceType = l.priceType,
                        unit = l.unit.orEmpty(),
                        condition = l.condition,
                        tagsText = l.tags.joinToString(", ") { it.name },
                        fulfillmentType = l.fulfillmentType,
                        pickupAddress = l.pickupAddress.orEmpty(),
                        categoryId = l.category?.id,
                        customCategory = l.customCategory.orEmpty(),
                        images = l.images,
                    )
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun update(block: (CreateEditListingUiState) -> CreateEditListingUiState) {
        _state.value = block(_state.value).copy(error = null, saved = false)
    }

    private fun toInput(): ListingFormInput {
        val s = _state.value
        return ListingFormInput(
            title = s.title.trim(),
            description = s.description.trim(),
            price = if (s.priceType == "free") null else s.price.trim().ifBlank { null },
            priceType = s.priceType,
            unit = s.unit.trim().ifBlank { null },
            condition = s.condition,
            tags = s.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(10),
            fulfillmentType = s.fulfillmentType,
            pickupAddress = if (s.fulfillmentType == "delivery") null else s.pickupAddress.trim().ifBlank { null },
            categoryId = if (s.customCategory.isNotBlank()) null else s.categoryId,
            customCategory = s.customCategory.trim().ifBlank { null },
        )
    }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.value = s.copy(error = "Title and description are required")
            return
        }
        if (s.priceType == "fixed" && s.price.isBlank()) {
            _state.value = s.copy(error = "Price is required for a fixed price")
            return
        }
        if (s.fulfillmentType != "delivery" && s.pickupAddress.isBlank()) {
            _state.value = s.copy(error = "Pickup address is required when pickup is offered")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = if (s.listingId != null) {
                listingRepository.updateListing(s.listingId, toInput())
            } else {
                listingRepository.createListing(toInput())
            }
            _state.value = when (result) {
                is AuthResult.Success -> _state.value.copy(
                    isSaving = false,
                    saved = true,
                    listingId = result.value.id,
                    images = result.value.images,
                )
                is AuthResult.Failure -> _state.value.copy(isSaving = false, error = result.error.message)
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        val id = _state.value.listingId ?: return
        _state.value = _state.value.copy(isUploadingPhoto = true, error = null)
        viewModelScope.launch {
            when (val result = listingRepository.uploadListingImage(id, uri)) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    isUploadingPhoto = false,
                    images = _state.value.images + result.value,
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isUploadingPhoto = false, error = result.error.message)
            }
        }
    }

    fun deletePhoto(imageId: String) {
        val id = _state.value.listingId ?: return
        val previous = _state.value.images
        _state.value = _state.value.copy(images = previous.filter { it.id != imageId })
        viewModelScope.launch {
            when (val result = listingRepository.deleteListingImage(id, imageId)) {
                is AuthResult.Failure -> _state.value = _state.value.copy(images = previous, error = result.error.message)
                is AuthResult.Success -> Unit
            }
        }
    }
}
