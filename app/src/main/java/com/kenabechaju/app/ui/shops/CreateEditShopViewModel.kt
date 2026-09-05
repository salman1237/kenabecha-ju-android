package com.kenabechaju.app.ui.shops

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ShopRepository
import com.kenabechaju.app.data.remote.dto.ShopDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEditShopUiState(
    val shop: ShopDto? = null,
    val shopName: String = "",
    val description: String = "",
    val shopType: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingLogo: Boolean = false,
    val isUploadingCover: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
) {
    val isEditing: Boolean get() = shop != null
}

/** One screen for create (POST /shops) and edit (PATCH /shops/{id}) — logo
 *  and cover upload only make sense once the shop exists, same reasoning
 *  as listing photos in CreateEditListingViewModel. */
@HiltViewModel
class CreateEditShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val shopId: String? = savedStateHandle["shopId"]

    private val _state = MutableStateFlow(CreateEditShopUiState())
    val state: StateFlow<CreateEditShopUiState> = _state.asStateFlow()

    init {
        shopId?.let(::loadExisting)
    }

    private fun loadExisting(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = shopRepository.myShops()) {
                is AuthResult.Success -> {
                    val shop = result.value.find { it.id == id }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        shop = shop,
                        shopName = shop?.shopName.orEmpty(),
                        description = shop?.description.orEmpty(),
                        shopType = shop?.shopType.orEmpty(),
                    )
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun update(block: (CreateEditShopUiState) -> CreateEditShopUiState) {
        _state.value = block(_state.value).copy(error = null, saved = false)
    }

    fun submit() {
        val s = _state.value
        if (s.shopName.isBlank()) {
            _state.value = s.copy(error = "Shop name is required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = if (s.shop != null) {
                shopRepository.updateShop(s.shop.id, s.shopName.trim(), s.description.trim().ifBlank { null }, s.shopType.trim().ifBlank { null })
            } else {
                shopRepository.createShop(s.shopName.trim(), s.description.trim().ifBlank { null }, s.shopType.trim().ifBlank { null })
            }
            _state.value = when (result) {
                is AuthResult.Success -> _state.value.copy(isSaving = false, saved = true, shop = result.value)
                is AuthResult.Failure -> _state.value.copy(isSaving = false, error = result.error.message)
            }
        }
    }

    fun uploadLogo(uri: Uri) {
        val id = _state.value.shop?.id ?: return
        _state.value = _state.value.copy(isUploadingLogo = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = shopRepository.uploadLogo(id, uri)) {
                is AuthResult.Success -> _state.value.copy(isUploadingLogo = false, shop = result.value)
                is AuthResult.Failure -> _state.value.copy(isUploadingLogo = false, error = result.error.message)
            }
        }
    }

    fun uploadCover(uri: Uri) {
        val id = _state.value.shop?.id ?: return
        _state.value = _state.value.copy(isUploadingCover = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = shopRepository.uploadCover(id, uri)) {
                is AuthResult.Success -> _state.value.copy(isUploadingCover = false, shop = result.value)
                is AuthResult.Failure -> _state.value.copy(isUploadingCover = false, error = result.error.message)
            }
        }
    }

    fun delete() {
        val id = _state.value.shop?.id ?: return
        viewModelScope.launch {
            when (val result = shopRepository.deleteShop(id)) {
                is AuthResult.Success -> _state.value = _state.value.copy(deleted = true)
                is AuthResult.Failure -> _state.value = _state.value.copy(error = result.error.message)
            }
        }
    }
}
