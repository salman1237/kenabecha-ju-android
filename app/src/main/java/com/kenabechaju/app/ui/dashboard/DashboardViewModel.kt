package com.kenabechaju.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.DashboardRepository
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.DashboardStatsDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val stats: DashboardStatsDto? = null,
    val recentListings: List<ListingDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Web's `/dashboard`: stat tiles + a recent-listings rail — the seller's
 *  overview, distinct from the full inventory-management screen
 *  (`MyListingsScreen`, Phase 2c). */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val listingRepository: ListingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val statsResult = dashboardRepository.stats()
            val listingsResult = listingRepository.myListings()
            _state.value = when {
                statsResult is AuthResult.Failure -> _state.value.copy(isLoading = false, error = statsResult.error.message)
                listingsResult is AuthResult.Failure -> _state.value.copy(isLoading = false, error = listingsResult.error.message)
                statsResult is AuthResult.Success && listingsResult is AuthResult.Success -> _state.value.copy(
                    isLoading = false,
                    stats = statsResult.value,
                    recentListings = listingsResult.value.take(5),
                )
                else -> _state.value.copy(isLoading = false)
            }
        }
    }
}
