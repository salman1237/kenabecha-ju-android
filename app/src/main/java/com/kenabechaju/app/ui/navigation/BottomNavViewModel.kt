package com.kenabechaju.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.ChatRepository
import com.kenabechaju.app.data.NotificationRepository
import com.kenabechaju.app.data.remote.ChatSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the Inbox and Notifications tabs' unread badges on the bottom nav
 *  bar — scoped to the NavHost itself (one instance for the app's
 *  lifetime, not per screen) so both badges stay correct from any tab, not
 *  just while their own screen is visible. Messages mirror the web app's
 *  own `useUnreadMessageCount` hook: reload the true total on every chat
 *  socket event rather than hand-rolling increment/decrement bookkeeping
 *  that could drift from the server's own count. Notifications have no
 *  equivalent live socket event, so `refreshNotifications()` is instead
 *  called by KenaBechaNavHost whenever the current route changes --
 *  catching the common case (leaving the Notifications screen after
 *  reading some) without a dedicated poll loop. */
@HiltViewModel
class BottomNavViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository,
    chatSocket: ChatSocket,
) : ViewModel() {
    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    init {
        refreshMessages()
        refreshNotifications()
        viewModelScope.launch {
            chatSocket.events.collect { refreshMessages() }
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            when (val result = chatRepository.listConversations()) {
                is AuthResult.Success -> _unreadMessageCount.value = result.value.sumOf { it.unreadCount }
                is AuthResult.Failure -> Unit
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            when (val result = notificationRepository.list()) {
                is AuthResult.Success -> _unreadNotificationCount.value = result.value.unreadCount
                is AuthResult.Failure -> Unit
            }
        }
    }
}
