package com.kenabechaju.app.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.data.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

data class OtpUiState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    val verified: Boolean = false,
)

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val email: String =
        URLDecoder.decode(checkNotNull(savedStateHandle["email"]) as String, "UTF-8")

    private val _state = MutableStateFlow(OtpUiState(email = email))
    val state: StateFlow<OtpUiState> = _state.asStateFlow()

    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _state.value = _state.value.copy(otp = value, error = null)
        }
    }

    fun verify() {
        val s = _state.value
        if (s.otp.length != 6) {
            _state.value = s.copy(error = "Enter the 6-digit code")
            return
        }
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = authRepository.verifyEmail(email, s.otp)) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, verified = true)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun resend() {
        _state.value = _state.value.copy(isResending = true, error = null, infoMessage = null)
        viewModelScope.launch {
            _state.value = when (val result = authRepository.resendOtp(email)) {
                is AuthResult.Success -> _state.value.copy(isResending = false, infoMessage = "A new code is on its way.")
                is AuthResult.Failure -> _state.value.copy(isResending = false, error = result.error.message)
            }
        }
    }
}
