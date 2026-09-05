package com.kenabechaju.app.ui.profile

import android.net.Uri
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

data class AccountSettingsUiState(
    val user: UserDto? = null,
    val fullName: String = "",
    val bio: String = "",
    val whatsapp: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

/** `dashboard/settings` on web, ported field for field: profile photo,
 *  display name, bio, WhatsApp number — phone itself stays read-only, set
 *  once during JU verification, same as web. */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountSettingsUiState())
    val state: StateFlow<AccountSettingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = authRepository.me()) {
                is AuthResult.Success -> _state.value.copy(
                    isLoading = false,
                    user = result.value,
                    fullName = result.value.fullName,
                    bio = result.value.bio.orEmpty(),
                    whatsapp = result.value.whatsappNumber.orEmpty(),
                )
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun onFullNameChange(value: String) {
        _state.value = _state.value.copy(fullName = value, saved = false, error = null)
    }

    fun onBioChange(value: String) {
        _state.value = _state.value.copy(bio = value, saved = false, error = null)
    }

    fun onWhatsAppChange(value: String) {
        _state.value = _state.value.copy(whatsapp = value, saved = false, error = null)
    }

    fun saveProfile() {
        val s = _state.value
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val profileResult = authRepository.updateProfile(s.fullName.trim(), s.bio.trim().ifBlank { null })
            when (profileResult) {
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSaving = false, error = profileResult.error.message)
                    return@launch
                }
                is AuthResult.Success -> Unit
            }
            val whatsappResult = authRepository.updateWhatsApp(s.whatsapp.trim().ifBlank { null })
            _state.value = when (whatsappResult) {
                is AuthResult.Success -> _state.value.copy(isSaving = false, saved = true, user = whatsappResult.value)
                is AuthResult.Failure -> _state.value.copy(isSaving = false, error = whatsappResult.error.message)
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        _state.value = _state.value.copy(isUploadingPhoto = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = authRepository.updateAvatar(uri)) {
                is AuthResult.Success -> _state.value.copy(isUploadingPhoto = false, user = result.value)
                is AuthResult.Failure -> _state.value.copy(isUploadingPhoto = false, error = result.error.message)
            }
        }
    }
}
