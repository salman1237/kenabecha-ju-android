package com.kenabechaju.app.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
            TopAppBar(
                title = { Text("Post") },
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
                                AsyncImage(model = post.shop.logoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
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
                            model = post.images.first().imageUrl,
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
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().height(((post.listings.size + 1) / 2 * 220).dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            userScrollEnabled = false,
                        ) {
                            items(post.listings, key = { it.id }) { listing ->
                                ListingCard(
                                    listing = listing,
                                    isSaved = false,
                                    onToggleSaved = {},
                                    onClick = { onListingClick(listing.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
