package com.kenabechaju.app.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionError: String? = null,
    val busyListingId: String? = null,
)

/** The seller's own inventory — every status action the web dashboard has,
 *  status-gated the same way the backend itself enforces the transitions
 *  (active -> sold/out-of-stock/paused, out-of-stock -> available,
 *  sold/paused -> relist, active/expired -> renew). */
@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MyListingsUiState())
    val state: StateFlow<MyListingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = listingRepository.myListings()) {
                is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, listings = result.value)
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    private fun runAction(listingId: String, block: suspend () -> AuthResult<ListingDto>) {
        _state.value = _state.value.copy(busyListingId = listingId, actionError = null)
        viewModelScope.launch {
            when (val result = block()) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    busyListingId = null,
                    listings = _state.value.listings.map { if (it.id == listingId) result.value else it },
                )
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(busyListingId = null, actionError = result.error.message)
            }
        }
    }

    fun markSold(id: String) = runAction(id) { listingRepository.markSold(id) }
    fun markOutOfStock(id: String) = runAction(id) { listingRepository.markOutOfStock(id) }
    fun markAvailable(id: String) = runAction(id) { listingRepository.markAvailable(id) }
    fun pause(id: String) = runAction(id) { listingRepository.pauseListing(id) }
    fun relist(id: String) = runAction(id) { listingRepository.relistListing(id) }
    fun renew(id: String) = runAction(id) { listingRepository.renewListing(id) }

    fun delete(id: String) {
        _state.value = _state.value.copy(busyListingId = id, actionError = null)
        viewModelScope.launch {
            when (val result = listingRepository.deleteListing(id)) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    busyListingId = null,
                    listings = _state.value.listings.filter { it.id != id },
                )
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(busyListingId = null, actionError = result.error.message)
            }
        }
    }
}
