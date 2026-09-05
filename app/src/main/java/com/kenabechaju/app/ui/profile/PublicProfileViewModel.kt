package com.kenabechaju.app.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.DashboardRepository
import com.kenabechaju.app.data.remote.dto.UserProfileDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileUiState(
    val profile: UserProfileDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Any user's profile — reachable from a seller's name on a listing, not
 *  just the signed-in user's own account (that's Account Settings). */
@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(PublicProfileUiState())
    val state: StateFlow<PublicProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = dashboardRepository.userProfile(userId)) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, profile = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
