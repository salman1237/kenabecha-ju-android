package com.kenabechaju.app.ui.posts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.ui.common.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposerScreen(
    onBack: () -> Unit,
    viewModel: PostComposerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::uploadPhoto) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit post" else "New post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (state.willResubmitToPending) {
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(
                        "This post is currently ${state.existingStatus}. Saving your edit will resubmit it for moderation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.bodyText,
                onValueChange = { v -> viewModel.update { it.copy(bodyText = v) } },
                label = { Text("What's new?") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.shopListings.isNotEmpty()) {
                Text(
                    "Link listings (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                Column {
                    state.shopListings.forEach { listing ->
                        ListingPickerRow(
                            listing = listing,
                            checked = listing.id in state.selectedListingIds,
                            onToggle = { viewModel.toggleListing(listing.id) },
                        )
                    }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
            if (state.saved) {
                Text(
                    if (state.isEditing) "Saved" else "Post submitted for moderation",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(if (state.isSaving) "Saving…" else if (state.isEditing) "Save changes" else "Submit post")
            }

            if (state.postId != null) {
                Text(
                    "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.images, key = { it.id }) { image ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = com.kenabechaju.app.ui.common.mediaUrl(image.imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            )
                            IconButton(
                                onClick = { viewModel.deletePhoto(image.id) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape),
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove photo", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.isUploadingPhoto) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                IconButton(onClick = {
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) {
                                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Add photo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingPickerRow(listing: ListingDto, checked: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(listing.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            Text(
                formatPrice(listing.price, listing.priceType, listing.unit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
