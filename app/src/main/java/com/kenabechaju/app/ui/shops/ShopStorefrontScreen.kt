package com.kenabechaju.app.ui.shops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.RatingDto
import com.kenabechaju.app.ui.common.KbCard
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.common.ReportDialog
import com.kenabechaju.app.ui.listings.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopStorefrontScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    viewModel: ShopStorefrontViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.reportSubmitted) {
        if (state.reportSubmitted) {
            showReportDialog = false
            viewModel.dismissReportDialog()
        }
    }
    // Same stale-on-return bug as AccountSettingsScreen / MyShopPostsScreen:
    // the ViewModel survives in its NavBackStackEntry, so a newly created
    // post/listing for this shop won't show here without an explicit refresh.
    LaunchedEffect(Unit) { viewModel.refresh() }
    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = viewModel::submitReport,
            isSubmitting = state.isSubmittingReport,
            error = state.reportError,
        )
    }

    Scaffold(
        topBar = {
            KenaBechaTopBar(
                title = state.shop?.shopName ?: "Shop",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report this shop")
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
            state.shop != null -> {
                val shop = state.shop!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        shop.coverUrl?.let {
                            AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(it), contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (shop.logoUrl != null) {
                                    AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(shop.logoUrl), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                                } else {
                                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(shop.shopName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                shop.shopType?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            val following = state.stats?.isFollowing
                            if (following != null) {
                                if (following) {
                                    OutlinedButton(onClick = viewModel::toggleFollow, enabled = !state.isFollowLoading) { Text("Following") }
                                } else {
                                    Button(onClick = viewModel::toggleFollow, enabled = !state.isFollowLoading) { Text("Follow") }
                                }
                            }
                        }

                        state.followError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                        }

                        shop.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                        }

                        state.stats?.let { stats ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            ) {
                                StatItem("Active", stats.activeListings.toString())
                                StatItem("Sold", stats.soldCount.toString())
                                StatItem("Followers", stats.followers.toString())
                                if (stats.reviewCount > 0) {
                                    StatItem("Rating", "★ ${"%.1f".format(stats.averageRating ?: 0.0)}")
                                }
                            }
                            stats.rateableListing?.let {
                                Text(
                                    "You can rate this shop after your purchase of \"${it.title}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Text(
                        "Listings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp),
                    )
                    if (state.listings.isEmpty()) {
                        Text(
                            "No listings yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    } else {
                        // A plain Column of Rows, not LazyVerticalGrid: a
                        // lazy grid nested inside a scrollable Column needs
                        // a bounded height, and a hardcoded guess silently
                        // clips whichever row doesn't fit it — confirmed via
                        // a live on-device test of the identical pattern in
                        // PostDetailScreen (a card's shop-name line was cut
                        // off), and already fixed the same way in
                        // DashboardScreen.
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            state.listings.chunked(2).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
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
                                    if (rowItems.size == 1) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (state.posts.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                        Text(
                            "Posts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(16.dp),
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            state.posts.forEach { post ->
                                com.kenabechaju.app.ui.posts.PostCard(post = post, onClick = { onPostClick(post.slug) })
                            }
                        }
                    }

                    if (state.reviews.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                        Text(
                            "Reviews",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(16.dp),
                        )
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            state.reviews.forEach { review ->
                                ReviewRow(review, modifier = Modifier.padding(bottom = 10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReviewRow(review: RatingDto, modifier: Modifier = Modifier) {
    KbCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                repeat(5) { i -> Text(if (i < review.stars) "★" else "☆", color = MaterialTheme.colorScheme.primary) }
            }
            review.reviewText?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
            Text(
                "— ${review.rater.fullName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
