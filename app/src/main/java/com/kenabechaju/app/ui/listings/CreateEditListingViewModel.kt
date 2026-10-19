package com.kenabechaju.app.ui.listings

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ListingFormInput
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.ShopRepository
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ListingImageDto
import com.kenabechaju.app.data.remote.dto.ListingVariantRequestDto
import com.kenabechaju.app.data.remote.dto.ShopDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One editable option row. Prices stay strings while being typed, same as
 *  the listing's own price field. */
data class VariantRow(
    val name: String = "",
    val price: String = "",
    val unit: String = "",
    val isAvailable: Boolean = true,
)

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
    val myShops: List<ShopDto> = emptyList(),
    val shopId: String? = null,
    val images: List<ListingImageDto> = emptyList(),
    val hasVariants: Boolean = false,
    val variants: List<VariantRow> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val isEditing: Boolean get() = listingId != null

    /** Options are a shop-inventory concept — the server rejects a variants
     *  payload on a personal listing outright. */
    val isShopListing: Boolean get() = shopId != null
}

/** One screen for both create (POST /listings) and edit (PATCH
 *  /listings/{id}) — same field set either way. Photos can only be added
 *  once the listing exists server-side, so on a fresh create the photo
 *  section appears the moment `listingId` is populated by a successful
 *  save, without leaving the screen. */
@HiltViewModel
class CreateEditListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val shopRepository: ShopRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(
        CreateEditListingUiState(listingId = savedStateHandle["listingId"]),
    )
    val state: StateFlow<CreateEditListingUiState> = _state.asStateFlow()

    init {
        loadCategories()
        val id = _state.value.listingId
        if (id != null) {
            loadExisting(id)
        } else {
            loadMyShops()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = listingRepository.categories()) {
                is AuthResult.Success -> _state.value = _state.value.copy(categories = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadMyShops() {
        viewModelScope.launch {
            when (val result = shopRepository.myShops()) {
                is AuthResult.Success -> _state.value = _state.value.copy(myShops = result.value)
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
                        shopId = l.shop?.id,
                        hasVariants = l.variants.isNotEmpty(),
                        variants = l.variants.map {
                            VariantRow(
                                name = it.name,
                                price = it.price,
                                unit = it.unit.orEmpty(),
                                isAvailable = it.isAvailable,
                            )
                        },
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
            shopId = s.shopId,
            // Only shop listings may send this at all. [] on an edit is the
            // explicit "remove every option"; null leaves them untouched.
            variants = when {
                !s.isShopListing -> null
                s.hasVariants -> s.variants.map {
                    ListingVariantRequestDto(
                        name = it.name.trim(),
                        price = it.price.trim(),
                        unit = it.unit.trim().ifBlank { null },
                        isAvailable = it.isAvailable,
                    )
                }
                s.isEditing -> emptyList()
                else -> null
            },
        )
    }

    fun addVariant() {
        update { it.copy(variants = it.variants + VariantRow()) }
    }

    fun updateVariant(index: Int, block: (VariantRow) -> VariantRow) {
        update { s ->
            s.copy(variants = s.variants.mapIndexed { i, row -> if (i == index) block(row) else row })
        }
    }

    fun removeVariant(index: Int) {
        update { it.copy(variants = it.variants.filterIndexed { i, _ -> i != index }) }
    }

    fun submit() {
        val s = _state.value
        if (s.title.isBlank() || s.description.isBlank()) {
            _state.value = s.copy(error = "Title and description are required")
            return
        }
        val usingVariants = s.isShopListing && s.hasVariants
        if (s.priceType == "fixed" && s.price.isBlank() && !usingVariants) {
            _state.value = s.copy(error = "Price is required for a fixed price")
            return
        }
        if (usingVariants) {
            if (s.variants.isEmpty()) {
                _state.value = s.copy(error = "Add at least one option, or turn options off")
                return
            }
            if (s.variants.any { it.name.isBlank() || it.price.isBlank() }) {
                _state.value = s.copy(error = "Every option needs a name and a price")
                return
            }
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

    fun uploadPhoto(uri: Uri) = uploadPhotos(listOf(uri))

    /** Uploads sequentially, not in parallel -- each one appends to
     *  `images` as it finishes, so the gallery fills in one at a time
     *  rather than racing several uploads against the same mutable list. */
    fun uploadPhotos(uris: List<Uri>) {
        val id = _state.value.listingId ?: return
        if (uris.isEmpty()) return
        _state.value = _state.value.copy(isUploadingPhoto = true, error = null)
        viewModelScope.launch {
            for (uri in uris) {
                when (val result = listingRepository.uploadListingImage(id, uri)) {
                    is AuthResult.Success -> _state.value = _state.value.copy(images = _state.value.images + result.value)
                    is AuthResult.Failure -> {
                        _state.value = _state.value.copy(isUploadingPhoto = false, error = result.error.message)
                        return@launch
                    }
                }
            }
            _state.value = _state.value.copy(isUploadingPhoto = false)
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

    /** Swaps a photo with its neighbor in [direction] (-1 left/earlier, +1
     *  right/later) and persists the new order. Optimistic like delete
     *  above, reverted if the server call fails. */
    fun movePhoto(imageId: String, direction: Int) {
        val id = _state.value.listingId ?: return
        val previous = _state.value.images
        val index = previous.indexOfFirst { it.id == imageId }
        val target = index + direction
        if (index < 0 || target !in previous.indices) return
        val reordered = previous.toMutableList().apply { add(target, removeAt(index)) }
        _state.value = _state.value.copy(images = reordered)
        viewModelScope.launch {
            when (val result = listingRepository.reorderListingImages(id, reordered.map { it.id })) {
                is AuthResult.Success -> _state.value = _state.value.copy(images = result.value.images)
                is AuthResult.Failure -> _state.value = _state.value.copy(images = previous, error = result.error.message)
            }
        }
    }

    /** Moves a photo to the front -- the cover is simply whichever image
     *  sorts first, so "make cover" is reorder's one-step special case
     *  rather than a separate backend concept. */
    fun makeCoverPhoto(imageId: String) {
        val id = _state.value.listingId ?: return
        val previous = _state.value.images
        val image = previous.find { it.id == imageId } ?: return
        val reordered = listOf(image) + previous.filter { it.id != imageId }
        _state.value = _state.value.copy(images = reordered)
        viewModelScope.launch {
            when (val result = listingRepository.reorderListingImages(id, reordered.map { it.id })) {
                is AuthResult.Success -> _state.value = _state.value.copy(images = result.value.images)
                is AuthResult.Failure -> _state.value = _state.value.copy(images = previous, error = result.error.message)
            }
        }
    }
}
