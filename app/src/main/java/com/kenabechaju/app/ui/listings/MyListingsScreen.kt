package com.kenabechaju.app.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.ui.common.KbCard
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.common.StatusBadge
import com.kenabechaju.app.ui.common.formatPrice

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    onBack: () -> Unit,
    onCreateListing: () -> Unit,
    onEditListing: (String) -> Unit,
    viewModel: MyListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Same stale-on-return bug as AccountSettingsScreen / MyShopPostsScreen:
    // the ViewModel survives in its NavBackStackEntry, so a newly created
    // or edited listing won't show here without an explicit refresh.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = "My Listings", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateListing) {
                Icon(Icons.Filled.Add, contentDescription = "Create listing")
            }
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
                state.listings.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't listed anything yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    items(state.listings, key = { it.id }) { listing ->
                        MyListingRow(
                            listing = listing,
                            busy = state.busyListingId == listing.id,
                            onEdit = { onEditListing(listing.id) },
                            onMarkSold = { viewModel.markSold(listing.id) },
                            onMarkOutOfStock = { viewModel.markOutOfStock(listing.id) },
                            onMarkAvailable = { viewModel.markAvailable(listing.id) },
                            onPause = { viewModel.pause(listing.id) },
                            onRelist = { viewModel.relist(listing.id) },
                            onRenew = { viewModel.renew(listing.id) },
                            onDelete = { viewModel.delete(listing.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyListingRow(
    listing: ListingDto,
    busy: Boolean,
    onEdit: () -> Unit,
    onMarkSold: () -> Unit,
    onMarkOutOfStock: () -> Unit,
    onMarkAvailable: () -> Unit,
    onPause: () -> Unit,
    onRelist: () -> Unit,
    onRenew: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    KbCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                val image = listing.images.firstOrNull()
                if (image != null) {
                    AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(image.imageUrl), contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(listing.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    formatPrice(listing.price, listing.priceType, listing.unit),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    StatusBadge(listing.status)
                    if (listing.status == "out_of_stock" && listing.restockRequestCount > 0) {
                        Text(
                            "🔔 ${listing.restockRequestCount} waiting for restock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }

            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEdit() })
                        when (listing.status) {
                            "active" -> {
                                DropdownMenuItem(text = { Text("Mark sold") }, onClick = { menuExpanded = false; onMarkSold() })
                                DropdownMenuItem(text = { Text("Mark out of stock") }, onClick = { menuExpanded = false; onMarkOutOfStock() })
                                DropdownMenuItem(text = { Text("Pause") }, onClick = { menuExpanded = false; onPause() })
                            }
                            "out_of_stock" -> DropdownMenuItem(text = { Text("Mark available") }, onClick = { menuExpanded = false; onMarkAvailable() })
                            "sold", "paused" -> DropdownMenuItem(text = { Text("Relist") }, onClick = { menuExpanded = false; onRelist() })
                            "expired" -> DropdownMenuItem(text = { Text("Renew") }, onClick = { menuExpanded = false; onRenew() })
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDelete() },
                        )
                    }
                }
            }
        }
    }
}
