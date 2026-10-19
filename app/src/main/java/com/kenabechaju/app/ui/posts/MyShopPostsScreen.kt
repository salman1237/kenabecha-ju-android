package com.kenabechaju.app.ui.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenabechaju.app.data.remote.dto.PostDto
import com.kenabechaju.app.ui.common.KbCard
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.common.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyShopPostsScreen(
    onBack: () -> Unit,
    onCreatePost: () -> Unit,
    onEditPost: (String) -> Unit,
    viewModel: MyShopPostsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // ViewModel survives in its NavBackStackEntry's ViewModelStore, so
    // init { refresh() } doesn't re-run on return from create/edit — same
    // stale-list bug already found and fixed for AccountSettingsScreen.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = "Shop Posts", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Icon(Icons.Filled.Add, contentDescription = "New post")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
                }
                state.posts.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posts yet — tap + to post an update", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(state.posts, key = { it.id }) { post ->
                        MyPostRow(
                            post = post,
                            onClick = { onEditPost(post.id) },
                            onDelete = { viewModel.delete(post.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPostRow(post: PostDto, onClick: () -> Unit, onDelete: () -> Unit) {
    KbCard(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(post.status)
            }
            if (post.status == "rejected" && post.rejectionReason != null) {
                Text(
                    "Rejected: ${post.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            androidx.compose.material3.TextButton(onClick = onDelete, modifier = Modifier.padding(top = 4.dp)) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
