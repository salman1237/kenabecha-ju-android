package com.kenabechaju.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.NotificationRepository
import com.kenabechaju.app.data.remote.dto.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val items: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = notificationRepository.list()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, items = result.value.items, unreadCount = result.value.unreadCount)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun markRead(id: String) {
        val previous = _state.value.items
        _state.value = _state.value.copy(
            items = previous.map { if (it.id == id) it.copy(isRead = true) else it },
            unreadCount = (_state.value.unreadCount - 1).coerceAtLeast(0),
        )
        viewModelScope.launch { notificationRepository.markRead(id) }
    }

    fun markAllRead() {
        _state.value = _state.value.copy(items = _state.value.items.map { it.copy(isRead = true) }, unreadCount = 0)
        viewModelScope.launch { notificationRepository.markAllRead() }
    }
}
