package com.kenabechaju.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenabechaju.app.data.remote.dto.NotificationDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Same stale-on-return bug as AccountSettingsScreen / MyShopPostsScreen:
    // the ViewModel survives in its NavBackStackEntry, so new notifications
    // and read-state changes won't show without an explicit refresh here.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllRead) { Text("Mark all read") }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
                state.items.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(state.items, key = { it.id }) { notification ->
                        NotificationRow(notification, onClick = { viewModel.markRead(notification.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationDto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 8.dp))
            }
            Column {
                Text(
                    notification.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    ),
                )
                notification.body?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
