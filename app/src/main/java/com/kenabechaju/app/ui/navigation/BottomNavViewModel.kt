package com.kenabechaju.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ChatRepository
import com.kenabechaju.app.data.remote.ChatSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the Inbox tab's unread badge on the bottom nav bar — scoped to
 *  the NavHost itself (one instance for the app's lifetime, not per
 *  screen) so the badge stays correct from any tab, not just while Inbox
 *  is the visible screen. Mirrors the web app's own `useUnreadMessageCount`
 *  hook: reload the true total on every chat socket event rather than
 *  hand-rolling increment/decrement bookkeeping that could drift from the
 *  server's own count. */
@HiltViewModel
class BottomNavViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    chatSocket: ChatSocket,
) : ViewModel() {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            chatSocket.events.collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = chatRepository.listConversations()) {
                is AuthResult.Success -> _unreadCount.value = result.value.sumOf { it.unreadCount }
                is AuthResult.Failure -> Unit
            }
        }
    }
}
