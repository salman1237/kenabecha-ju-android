package com.kenabechaju.app.ui.shops

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

data class BrowseShopsUiState(
    val allShops: List<ShopDto> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val shops: List<ShopDto>
        get() = if (query.isBlank()) allShops else allShops.filter { it.shopName.contains(query, ignoreCase = true) }
}

/** `GET /shops`; search/sort happen client-side since the endpoint has no
 *  query params beyond pagination and the result set is small enough. */
@HiltViewModel
class BrowseShopsViewModel @Inject constructor(private val shopRepository: ShopRepository) : ViewModel() {
    private val _state = MutableStateFlow(BrowseShopsUiState())
    val state: StateFlow<BrowseShopsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = shopRepository.listShops()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, allShops = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.value = _state.value.copy(query = value)
    }
}
