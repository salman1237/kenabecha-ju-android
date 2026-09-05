package com.kenabechaju.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ChatRepository
import com.kenabechaju.app.data.remote.ChatSocket
import com.kenabechaju.app.data.remote.WsEvent
import com.kenabechaju.app.data.remote.dto.ConversationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InboxUiState(
    val conversations: List<ConversationDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Conversation list — live-updated from the same global `ChatSocket` a
 *  chat thread screen uses, so a new message bumps the right row's preview
 *  and unread count even while the inbox itself is the visible screen. */
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatSocket: ChatSocket,
) : ViewModel() {
    private val _state = MutableStateFlow(InboxUiState())
    val state: StateFlow<InboxUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            chatSocket.events.collect { event ->
                if (event is WsEvent.NewMessage) {
                    val current = _state.value.conversations
                    val updated = current.map { convo ->
                        if (convo.id == event.conversationId) {
                            convo.copy(
                                lastMessagePreview = event.message.content.take(140),
                                lastMessageAt = event.message.createdAt,
                                unreadCount = convo.unreadCount + 1,
                            )
                        } else convo
                    }
                    _state.value = _state.value.copy(conversations = updated)
                }
            }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = chatRepository.listConversations()) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, conversations = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
