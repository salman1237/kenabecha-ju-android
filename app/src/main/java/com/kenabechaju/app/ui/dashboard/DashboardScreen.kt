package com.kenabechaju.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.kenabechaju.app.ui.listings.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onSavedListings: () -> Unit,
    onListingClick: (String) -> Unit,
    onMyShops: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Same stale-on-return bug as AccountSettingsScreen / MyShopPostsScreen:
    // the ViewModel survives in its NavBackStackEntry, so stats/recent
    // listings won't reflect a sale, new listing, etc. without a refresh.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.stats != null -> {
                val stats = state.stats!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    // A plain Column of Rows, not LazyVerticalGrid: a lazy
                    // grid nested inside a scrollable Column needs a bounded
                    // height, and a hardcoded one silently clips whichever
                    // row doesn't fit it — confirmed via a live on-device
                    // test where the last row's labels were cut off. Six
                    // fixed items never need lazy/virtualized rendering.
                    listOf(
                        "Active listings" to stats.activeListings.toString(),
                        "Sold" to stats.soldListings.toString(),
                        "Shops" to stats.shops.toString(),
                        "Unread messages" to stats.unreadMessages.toString(),
                        "Conversations" to stats.conversations.toString(),
                        "Saved" to stats.savedCount.toString(),
                    ).chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        ) {
                            rowItems.forEach { (label, value) ->
                                StatTile(label = label, value = value, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (stats.ratingCount > 0) {
                        StatTile(
                            label = "Rating",
                            value = "★ ${"%.1f".format(stats.averageRating ?: 0.0)} (${stats.ratingCount})",
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        )
                    }

                    TextButton(onClick = onSavedListings, modifier = Modifier.padding(top = 16.dp)) {
                        Text("View saved listings (${stats.savedCount})")
                    }
                    TextButton(onClick = onMyShops) {
                        Text("Manage my shops (${stats.shops})")
                    }

                    if (state.recentListings.isNotEmpty()) {
                        Text(
                            "Recent listings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        )
                        state.recentListings.chunked(2).forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            ) {
                                rowItems.forEach { listing ->
                                    ListingCard(
                                        listing = listing,
                                        isSaved = false,
                                        onToggleSaved = {},
                                        onClick = { onListingClick(listing.id) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                // An odd final row's single card should not
                                // stretch to fill the row — a matching
                                // invisible spacer keeps its width identical
                                // to every other card instead.
                                if (rowItems.size == 1) {
                                    Box(modifier = Modifier.weight(1f))
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
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
