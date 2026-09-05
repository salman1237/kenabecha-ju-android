package com.kenabechaju.app.ui.listings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ChatRepository
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingDetailUiState(
    val listing: ListingDto? = null,
    val related: List<ListingDto> = emptyList(),
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
    val isContactingSeller: Boolean = false,
    val openConversationId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val listingId: String = checkNotNull(savedStateHandle["listingId"])

    private val _state = MutableStateFlow(ListingDetailUiState())
    val state: StateFlow<ListingDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = listingRepository.getListing(listingId)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, listing = result.value)
                    loadSavedStatus()
                    loadRelated()
                }
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    private fun loadSavedStatus() {
        viewModelScope.launch {
            when (val result = listingRepository.savedIds()) {
                is AuthResult.Success -> _state.value = _state.value.copy(isSaved = listingId in result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadRelated() {
        viewModelScope.launch {
            when (val result = listingRepository.getRelated(listingId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(related = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    fun toggleSaved() {
        val previous = _state.value.isSaved
        _state.value = _state.value.copy(isSaved = !previous)
        viewModelScope.launch {
            when (val result = listingRepository.toggleSaved(listingId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isSaved = result.value)
                is AuthResult.Failure -> _state.value = _state.value.copy(isSaved = previous)
            }
        }
    }

    fun contactSeller() {
        _state.value = _state.value.copy(isContactingSeller = true, error = null)
        viewModelScope.launch {
            when (val result = chatRepository.contactSeller(listingId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isContactingSeller = false, openConversationId = result.value.id)
                is AuthResult.Failure -> _state.value = _state.value.copy(isContactingSeller = false, error = result.error.message)
            }
        }
    }
}
