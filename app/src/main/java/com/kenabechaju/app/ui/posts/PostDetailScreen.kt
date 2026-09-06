package com.kenabechaju.app.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.common.parsePostHtml
import com.kenabechaju.app.ui.listings.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    onShopClick: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = "Post", onBack = onBack)
        },
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.post != null -> {
                val post = state.post!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onShopClick(post.shop.slug) },
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (post.shop.logoUrl != null) {
                                AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(post.shop.logoUrl), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                            } else {
                                Icon(Icons.Outlined.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(post.shop.shopName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(start = 8.dp))
                    }

                    Text(
                        post.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp),
                    )

                    if (post.images.isNotEmpty()) {
                        AsyncImage(
                            model = com.kenabechaju.app.ui.common.mediaUrl(post.images.first().imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(top = 12.dp),
                        )
                    }

                    Text(
                        parsePostHtml(post.descriptionHtml),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 14.dp),
                    )

                    if (post.listings.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text("Linked listings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        // A plain Column of Rows, not LazyVerticalGrid: a lazy
                        // grid nested inside a scrollable Column needs a
                        // bounded height, and a hardcoded guess silently
                        // clips whichever row doesn't fit it — confirmed via
                        // a live on-device test where a card's shop-name
                        // line was cut off (same bug class already fixed in
                        // DashboardScreen).
                        post.listings.chunked(2).forEach { rowItems ->
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
            }
        }
    }
}
