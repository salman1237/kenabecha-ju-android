package com.kenabechaju.app.ui.shops

import androidx.compose.foundation.background
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
    onEditShop: (String) -> Unit,
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
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
                state.shops.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't opened a shop yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(state.shops, key = { it.id }) { shop ->
                        ShopRow(shop, onClick = { onEditShop(shop.id) }, onManagePosts = { onManagePosts(shop.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopRow(shop: ShopDto, onClick: () -> Unit, onManagePosts: () -> Unit) {
    KbCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (shop.logoUrl != null) {
                    AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(shop.logoUrl), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
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
            androidx.compose.material3.TextButton(onClick = onManagePosts) {
                Text("Posts")
            }
        }
    }
}
