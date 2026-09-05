package com.kenabechaju.app.ui.listings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.ui.common.conditionLabel
import com.kenabechaju.app.ui.common.formatPrice
import com.kenabechaju.app.ui.common.fulfillmentLabel
import com.kenabechaju.app.ui.common.statusLabel

@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    onRelatedClick: (String) -> Unit,
    onSellerClick: (String) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 12.dp)) { Text("Retry") }
            }
            state.listing != null -> {
                val listing = state.listing!!
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box {
                        ImageGallery(listing)
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        IconButton(
                            onClick = viewModel::toggleSaved,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        ) {
                            Icon(
                                if (state.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (state.isSaved) "Unsave" else "Save",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                listing.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                            )
                            if (listing.status != "active") {
                                Badge { Text(statusLabel(listing.status)) }
                            }
                        }

                        Text(
                            formatPrice(listing.price, listing.priceType, listing.unit),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (listing.priceType == "negotiable") {
                            Text(
                                "Price is negotiable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(conditionLabel(listing.condition), style = MaterialTheme.typography.bodyMedium)
                        if (listing.fulfillmentType != "delivery") {
                            Text(
                                listing.pickupAddress ?: "Ask seller for pickup location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (listing.fulfillmentType != "pickup") {
                            Text(
                                "Delivery available — ask seller for details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        SellerRow(
                            listing = listing,
                            onClick = if (listing.shop == null) ({ onSellerClick(listing.seller.id) }) else null,
                        )

                        if (listing.seller.phone != null || listing.seller.whatsappNumber != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            ) {
                                listing.seller.phone?.let { phone ->
                                    OutlinedButton(
                                        onClick = {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(" Call", modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                                listing.seller.whatsappNumber?.let { number ->
                                    OutlinedButton(
                                        onClick = {
                                            val digits = number.filter { it.isDigit() }
                                            val message = Uri.encode("Hi, I'm interested in \"${listing.title}\" on KenaBecha JU.")
                                            val uri = Uri.parse("https://wa.me/$digits?text=$message")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text("WhatsApp")
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            listing.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )

                        if (listing.tags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 10.dp),
                            ) {
                                listing.tags.forEach { tag ->
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(tag.name, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }

                        Text(
                            "${listing.viewCount} views",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    if (state.related.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            "Related listings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(16.dp),
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        ) {
                            items(state.related, key = { it.id }) { related ->
                                ListingCard(
                                    listing = related,
                                    isSaved = false,
                                    onToggleSaved = {},
                                    onClick = { onRelatedClick(related.id) },
                                    modifier = Modifier.size(160.dp),
                                )
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageGallery(listing: ListingDto) {
    if (listing.images.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { listing.images.size })
    Column {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = listing.images[page].imageUrl,
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
        }
        if (listing.images.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            ) {
                repeat(listing.images.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerRow(listing: ListingDto, onClick: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        val imageUrl = listing.shop?.logoUrl ?: listing.seller.avatarUrl
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                Text(
                    (listing.shop?.shopName ?: listing.seller.fullName).take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(listing.shop?.shopName ?: listing.seller.fullName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(
                if (listing.shop != null) "Campus shop" else "Student seller",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
