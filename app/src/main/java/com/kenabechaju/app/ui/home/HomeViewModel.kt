package com.kenabechaju.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.remote.dto.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: UserDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val loggedOut: Boolean = false,
)

/** Proves the whole auth loop end to end: a real authenticated `GET
 *  /auth/me` call succeeds using only the stored token (no cookie exists
 *  on this client at all), and logout actually clears the session. */
@HiltViewModel
class HomeViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = authRepository.me()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, user = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(loggedOut = true)
        }
    }
}
