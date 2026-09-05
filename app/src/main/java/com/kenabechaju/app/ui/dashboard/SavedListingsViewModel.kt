package com.kenabechaju.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.DashboardRepository
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class SavedListingsViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val listingRepository: ListingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SavedListingsUiState())
    val state: StateFlow<SavedListingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = dashboardRepository.savedListings()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, listings = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    /** Unsaving here should drop the item from this exact list — reusing
     *  the generic toggle rather than assuming the result is always "now
     *  unsaved" (defensive: this list only ever shows saved items, so a
     *  toggle from here always means "remove"). */
    fun unsave(listingId: String) {
        _state.value = _state.value.copy(listings = _state.value.listings.filter { it.id != listingId })
        viewModelScope.launch { listingRepository.toggleSaved(listingId) }
    }
}
