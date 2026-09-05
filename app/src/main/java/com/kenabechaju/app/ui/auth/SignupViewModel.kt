package com.kenabechaju.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ReferenceRepository
import com.kenabechaju.app.data.remote.dto.DepartmentDto
import com.kenabechaju.app.data.remote.dto.HallDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val showFullForm: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val studentId: String = "",
    val registrationNo: String = "",
    val hallId: String = "",
    val departmentId: String = "",
    val session: String = "",
    val halls: List<HallDto> = emptyList(),
    val departments: List<DepartmentDto> = emptyList(),
    val sessions: List<SessionOptionDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val signedUpEmail: String? = null,
    val googleSignedIn: Boolean = false,
) {
    val batch: Int? get() = sessions.find { it.session == session }?.batch
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val referenceRepository: ReferenceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SignupUiState())
    val state: StateFlow<SignupUiState> = _state.asStateFlow()

    fun showFullForm() {
        _state.value = _state.value.copy(showFullForm = true)
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            runCatching {
                val halls = referenceRepository.halls()
                val departments = referenceRepository.departments()
                val sessions = referenceRepository.sessions()
                _state.value = _state.value.copy(halls = halls, departments = departments, sessions = sessions)
            }
        }
    }

    fun update(block: (SignupUiState) -> SignupUiState) {
        _state.value = block(_state.value).copy(error = null)
    }

    fun submit() {
        val s = _state.value
        if (s.password != s.confirmPassword) {
            _state.value = s.copy(error = "Passwords don't match")
            return
        }
        if (s.hallId.isBlank() || s.departmentId.isBlank() || s.session.isBlank()) {
            _state.value = s.copy(error = "Fill in every field")
            return
        }
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.signup(
                email = s.email.trim(),
                password = s.password,
                fullName = s.fullName.trim(),
                phone = s.phone.trim(),
                studentId = s.studentId.trim(),
                registrationNo = s.registrationNo.trim(),
                hallId = s.hallId,
                departmentId = s.departmentId,
                session = s.session,
            )
            _state.value = when (result) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, signedUpEmail = result.value.email)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
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
                        is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, googleSignedIn = true)
                        is AuthResult.Failure -> _state.value =
                            _state.value.copy(isLoading = false, error = authResult.error.message)
                    }
                }
            }
        }
    }
}
