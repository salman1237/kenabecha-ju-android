package com.kenabechaju.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenabechaju.app.ui.common.GradientText
import com.kenabechaju.app.ui.common.KbCard
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.listings.ListingCard
import com.kenabechaju.app.ui.theme.KbColors

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
            KenaBechaTopBar(title = "Dashboard", onBack = onBack)
        },
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
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
                    GradientText(
                        "Your overview",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )

                    // A plain Column of Rows, not LazyVerticalGrid: a lazy
                    // grid nested inside a scrollable Column needs a bounded
                    // height, and a hardcoded one silently clips whichever
                    // row doesn't fit it — confirmed via a live on-device
                    // test where the last row's labels were cut off. Six
                    // fixed items never need lazy/virtualized rendering.
                    // Each stat also gets its own accent color/icon instead
                    // of six identical gray tiles.
                    listOf(
                        StatSpec("Active listings", stats.activeListings.toString(), Icons.Filled.Inventory2) { KbColors.success },
                        StatSpec("Sold", stats.soldListings.toString(), Icons.Filled.CheckCircle) { KbColors.info },
                        StatSpec("Shops", stats.shops.toString(), Icons.Outlined.Storefront) { MaterialTheme.colorScheme.tertiary },
                        StatSpec("Unread messages", stats.unreadMessages.toString(), Icons.Filled.MarkChatUnread) { KbColors.warning },
                        StatSpec("Conversations", stats.conversations.toString(), Icons.AutoMirrored.Filled.Chat) { MaterialTheme.colorScheme.primary },
                        // Not `secondary`: in this palette that role is a
                        // near-white neutral (zinc-100), meant as a light
                        // background, not a vivid accent — confirmed live,
                        // it rendered the icon almost invisible.
                        StatSpec("Saved", stats.savedCount.toString(), Icons.Filled.Bookmark) { MaterialTheme.colorScheme.tertiary },
                    ).chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        ) {
                            rowItems.forEach { spec ->
                                StatTile(spec = spec, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (stats.ratingCount > 0) {
                        StatTile(
                            spec = StatSpec(
                                "Rating",
                                "★ ${"%.1f".format(stats.averageRating ?: 0.0)} (${stats.ratingCount})",
                                Icons.Filled.Star,
                            ) { KbColors.warning },
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

private class StatSpec(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accent: @Composable () -> Color,
)

@Composable
private fun StatTile(spec: StatSpec, modifier: Modifier = Modifier) {
    val accent = spec.accent()
    KbCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(spec.icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(
                spec.value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(spec.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
