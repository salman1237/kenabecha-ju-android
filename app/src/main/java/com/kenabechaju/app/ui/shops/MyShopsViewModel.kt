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

data class MyShopsUiState(
    val shops: List<ShopDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class MyShopsViewModel @Inject constructor(private val shopRepository: ShopRepository) : ViewModel() {
    private val _state = MutableStateFlow(MyShopsUiState())
    val state: StateFlow<MyShopsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = shopRepository.myShops()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, shops = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
