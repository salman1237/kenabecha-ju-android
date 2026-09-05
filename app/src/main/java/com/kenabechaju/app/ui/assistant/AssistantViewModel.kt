package com.kenabechaju.app.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.remote.AssistantEvent
import com.kenabechaju.app.data.remote.AssistantSocket
import com.kenabechaju.app.data.remote.dto.AssistantTurnDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantMessage(
    val role: String,
    val content: String,
    val listings: List<ListingDto> = emptyList(),
)

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val draft: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
)

/** Session-only history, no account tie — matches the web app's own
 *  explicit privacy stance for the assistant; genuinely not persisted
 *  anywhere, including across process death. */
@HiltViewModel
class AssistantViewModel @Inject constructor(private val assistantSocket: AssistantSocket) : ViewModel() {
    private val _state = MutableStateFlow(AssistantUiState())
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()

    fun onDraftChange(value: String) {
        _state.value = _state.value.copy(draft = value)
    }

    fun send() {
        val text = _state.value.draft.trim()
        if (text.isEmpty() || _state.value.isStreaming) return

        val history = _state.value.messages.takeLast(10).map { AssistantTurnDto(it.role, it.content) }
        _state.value = _state.value.copy(
            draft = "",
            error = null,
            isStreaming = true,
            messages = _state.value.messages + AssistantMessage("user", text) + AssistantMessage("assistant", ""),
        )

        viewModelScope.launch {
            assistantSocket.streamChat(text, history, "en").collect { event ->
                when (event) {
                    is AssistantEvent.Delta -> appendToLastAssistantMessage(event.text)
                    is AssistantEvent.Listings -> setLastAssistantListings(event.listings)
                    is AssistantEvent.Error -> _state.value = _state.value.copy(isStreaming = false, error = event.message)
                    is AssistantEvent.Done -> _state.value = _state.value.copy(isStreaming = false)
                }
            }
        }
    }

    private fun appendToLastAssistantMessage(text: String) {
        val messages = _state.value.messages.toMutableList()
        val lastIndex = messages.indexOfLast { it.role == "assistant" }
        if (lastIndex >= 0) {
            messages[lastIndex] = messages[lastIndex].copy(content = messages[lastIndex].content + text)
            _state.value = _state.value.copy(messages = messages)
        }
    }

    private fun setLastAssistantListings(listings: List<ListingDto>) {
        val messages = _state.value.messages.toMutableList()
        val lastIndex = messages.indexOfLast { it.role == "assistant" }
        if (lastIndex >= 0) {
            messages[lastIndex] = messages[lastIndex].copy(listings = listings)
            _state.value = _state.value.copy(messages = messages)
        }
    }
}
