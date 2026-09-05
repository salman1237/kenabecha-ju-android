package com.kenabechaju.app.ui.profile

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

data class CompleteProfileUiState(
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
    val completed: Boolean = false,
) {
    val batch: Int? get() = sessions.find { it.session == session }?.batch
}

/** Fills in the JU-verification fields a Google-lite buyer skipped at
 *  signup — same fields SignupViewModel collects, against the same
 *  reference data, just via PATCH instead of POST /auth/signup. */
@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val referenceRepository: ReferenceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CompleteProfileUiState())
    val state: StateFlow<CompleteProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val halls = referenceRepository.halls()
                val departments = referenceRepository.departments()
                val sessions = referenceRepository.sessions()
                _state.value = _state.value.copy(halls = halls, departments = departments, sessions = sessions)
            }
        }
    }

    fun update(block: (CompleteProfileUiState) -> CompleteProfileUiState) {
        _state.value = block(_state.value).copy(error = null)
    }

    fun submit() {
        val s = _state.value
        if (s.phone.isBlank() || s.studentId.isBlank() || s.registrationNo.isBlank() ||
            s.hallId.isBlank() || s.departmentId.isBlank() || s.session.isBlank()
        ) {
            _state.value = s.copy(error = "Fill in every field")
            return
        }
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.completeProfile(
                phone = s.phone.trim(),
                studentId = s.studentId.trim(),
                registrationNo = s.registrationNo.trim(),
                hallId = s.hallId,
                departmentId = s.departmentId,
                session = s.session,
            )
            _state.value = when (result) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, completed = true)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
