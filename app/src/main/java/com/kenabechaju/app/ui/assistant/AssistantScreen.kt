package com.kenabechaju.app.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenabechaju.app.ui.theme.KbColors
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.listings.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: AssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = "Ask KenaBecha", onBack = onBack)
        },
        bottomBar = {
            // Edge-to-edge means this row draws behind the system
            // gesture-nav bar unless it explicitly claims that inset back
            // — confirmed live: without this, the row could sit at/under
            // the gesture zone and was fully hidden behind the keyboard.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp),
            ) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = viewModel::onDraftChange,
                    placeholder = { Text("Ask about anything on KenaBecha JU") },
                    enabled = !state.isStreaming,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = viewModel::send, enabled = !state.isStreaming && state.draft.isNotBlank()) {
                    if (state.isStreaming) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Ask me to find listings, compare prices, or recommend something to buy or sell on campus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().weight(1f),
                ) {
                    items(state.messages) { message ->
                        AssistantBubble(message, onListingClick)
                    }
                    state.error?.let { error ->
                        item { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantBubble(message: AssistantMessage, onListingClick: (String) -> Unit) {
    val isUser = message.role == "user"
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
            val bubbleModifier = if (isUser) {
                Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                        ),
                    )
                    .padding(10.dp)
            } else {
                Modifier
                    .widthIn(max = 280.dp)
                    .shadow(elevation = 2.dp, shape = shape, ambientColor = KbColors.shadowPrimary, spotColor = KbColors.shadowPrimary)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp)
            }
            Box(modifier = bubbleModifier) {
                Text(
                    message.content.ifBlank { "…" },
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (message.listings.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                items(message.listings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        isSaved = false,
                        onToggleSaved = {},
                        onClick = { onListingClick(listing.id) },
                        modifier = Modifier.widthIn(max = 160.dp),
                    )
                }
            }
        }
    }
}
