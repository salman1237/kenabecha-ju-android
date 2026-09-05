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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun login() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "Enter your email and password")
            return
        }
        _state.value = current.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.login(current.email.trim(), current.password)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, loggedIn = true)
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun onGoogleResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Cancelled -> Unit
            is GoogleSignInResult.Failure -> _state.value = _state.value.copy(error = result.message)
            is GoogleSignInResult.Success -> {
                _state.value = _state.value.copy(isLoading = true, error = null)
                viewModelScope.launch {
                    when (val authResult = authRepository.googleLogin(result.idToken)) {
                        is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, loggedIn = true)
                        is AuthResult.Failure -> _state.value =
                            _state.value.copy(isLoading = false, error = authResult.error.message)
                    }
                }
            }
        }
    }
}
