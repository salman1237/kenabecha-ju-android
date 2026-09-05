package com.kenabechaju.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.data.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sent: Boolean = false,
)

/** Requests the reset email only — same as web's `/forgot-password` page.
 *  Completing the reset happens on the emailed link, which opens the web
 *  app's `/reset-password?token=...` page; the backend issues one token
 *  type for both clients, so there is no separate mobile reset endpoint
 *  to build against. */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun submit() {
        val current = _state.value
        if (current.email.isBlank()) {
            _state.value = current.copy(error = "Enter your email")
            return
        }
        _state.value = current.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(current.email.trim())) {
                is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, sent = true)
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
