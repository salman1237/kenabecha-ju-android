package com.kenabechaju.app.ui.shops

import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.ShopDto
import com.kenabechaju.app.ui.common.KbCard
import com.kenabechaju.app.ui.common.KenaBechaTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyShopsScreen(
    onBack: () -> Unit,
    onCreateShop: () -> Unit,
    onOpenStorefront: (String) -> Unit,
    onEditShop: (String) -> Unit,
    onManageListings: (String) -> Unit,
    onManagePosts: (String) -> Unit,
    viewModel: MyShopsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Same stale-on-return bug as AccountSettingsScreen / MyShopPostsScreen:
    // the ViewModel survives in its NavBackStackEntry, so a newly created
    // shop won't show here without an explicit refresh — confirmed live
    // for the identical bug in MyShopPostsScreen this same audit round.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = "My Shops", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateShop) {
                Icon(Icons.Filled.Add, contentDescription = "Create shop")
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
                state.shops.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't opened a shop yet", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(state.shops, key = { it.id }) { shop ->
                        ShopRow(
                            shop,
                            onClick = { onOpenStorefront(shop.slug) },
                            onEdit = { onEditShop(shop.id) },
                            onManageListings = { onManageListings(shop.id) },
                            onManagePosts = { onManagePosts(shop.id) },
                        )
                    }
                }
            }
        }
    }
}

/** Tapping the row itself opens the public storefront -- the same "what a
 *  buyer sees" view a shop owner actually wants to check most often -- not
 *  the edit form, which now needs its own explicit icon button since it no
 *  longer doubles as the row's default action. Listings/Posts are two
 *  separate, equally-weighted management shortcuts rather than Posts alone:
 *  a shop's day-to-day work is split between its inventory and its
 *  announcements, and only one of those was reachable from here before. */
@Composable
private fun ShopRow(
    shop: ShopDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onManageListings: () -> Unit,
    onManagePosts: () -> Unit,
) {
    KbCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (shop.logoUrl != null) {
                    AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(shop.logoUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(shop.shopName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    "${shop.listingCount} listings · ${shop.followerCount} followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit shop", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
            androidx.compose.material3.TextButton(onClick = onManageListings) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                Text("Listings")
            }
            androidx.compose.material3.TextButton(onClick = onManagePosts) {
                Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                Text("Posts")
            }
        }
        }
    }
}
