package com.kenabechaju.app.ui.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthRepository
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ChatRepository
import com.kenabechaju.app.data.remote.ChatSocket
import com.kenabechaju.app.data.remote.WsEvent
import com.kenabechaju.app.data.remote.dto.ConversationDto
import com.kenabechaju.app.data.remote.dto.MessageDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatThreadUiState(
    val conversationId: String = "",
    val conversation: ConversationDto? = null,
    val messages: List<MessageDto> = emptyList(),
    val currentUserId: String? = null,
    val draft: String = "",
    val otherIsTyping: Boolean = false,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val isSendingAttachment: Boolean = false,
    val error: String? = null,
    // Deliberately separate from `error` above: that field gates the
    // initial load (blank screen vs. message list), so a failed *send*
    // must not reuse it — doing so would replace the whole already-loaded
    // conversation with just an error message, a real bug found via a
    // live on-device test, not a hypothetical.
    val sendError: String? = null,
)

@HiltViewModel
class ChatThreadViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatSocket: ChatSocket,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _state = MutableStateFlow(ChatThreadUiState(conversationId = conversationId))
    val state: StateFlow<ChatThreadUiState> = _state.asStateFlow()

    private var typingClearJob: Job? = null
    private var typingStopJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            when (val result = authRepository.me()) {
                is AuthResult.Success -> _state.value = _state.value.copy(currentUserId = result.value.id)
                is AuthResult.Failure -> Unit
            }
        }
        viewModelScope.launch {
            chatSocket.events.collect { event ->
                when (event) {
                    is WsEvent.NewMessage -> if (event.conversationId == conversationId) {
                        val current = _state.value.messages
                        if (current.none { it.id == event.message.id }) {
                            _state.value = _state.value.copy(messages = current + event.message)
                            viewModelScope.launch { chatRepository.markRead(conversationId) }
                        }
                    }
                    is WsEvent.Read -> if (event.conversationId == conversationId) {
                        val ids = event.messageIds.toSet()
                        _state.value = _state.value.copy(
                            messages = _state.value.messages.map {
                                if (it.id in ids) it.copy(readAt = it.readAt ?: "now") else it
                            },
                        )
                    }
                    is WsEvent.Typing -> if (event.conversationId == conversationId) {
                        _state.value = _state.value.copy(otherIsTyping = event.isTyping)
                        typingClearJob?.cancel()
                        if (event.isTyping) {
                            typingClearJob = viewModelScope.launch {
                                delay(5000)
                                _state.value = _state.value.copy(otherIsTyping = false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val conversationResult = chatRepository.getConversation(conversationId)
            val messagesResult = chatRepository.getMessages(conversationId)
            _state.value = when {
                conversationResult is AuthResult.Failure -> _state.value.copy(isLoading = false, error = conversationResult.error.message)
                messagesResult is AuthResult.Failure -> _state.value.copy(isLoading = false, error = messagesResult.error.message)
                conversationResult is AuthResult.Success && messagesResult is AuthResult.Success -> _state.value.copy(
                    isLoading = false,
                    conversation = conversationResult.value,
                    messages = messagesResult.value.sortedBy { it.createdAt },
                )
                else -> _state.value.copy(isLoading = false)
            }
            chatRepository.markRead(conversationId)
        }
    }

    fun onDraftChange(value: String) {
        _state.value = _state.value.copy(draft = value)
        chatSocket.sendTyping(conversationId, true)
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2000)
            chatSocket.sendTyping(conversationId, false)
        }
    }

    fun send() {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        _state.value = _state.value.copy(isSending = true, draft = "", sendError = null)
        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(conversationId, text)) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    isSending = false,
                    messages = _state.value.messages + result.value,
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isSending = false, sendError = result.error.message, draft = text)
            }
        }
    }

    fun sendAttachment(uri: Uri) {
        _state.value = _state.value.copy(isSendingAttachment = true, sendError = null)
        viewModelScope.launch {
            when (val result = chatRepository.sendAttachment(conversationId, uri, "")) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    isSendingAttachment = false,
                    messages = _state.value.messages + result.value,
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isSendingAttachment = false, sendError = result.error.message)
            }
        }
    }
}
