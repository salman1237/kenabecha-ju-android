package com.kenabechaju.app.ui.listings

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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ShopDto
import com.kenabechaju.app.ui.common.KenaBechaTopBar
import com.kenabechaju.app.ui.common.LabeledDropdown

private val PRICE_TYPES = listOf("fixed" to "Fixed", "negotiable" to "Negotiable", "free" to "Free")
private val CONDITIONS = listOf(
    "new" to "New",
    "used_like_new" to "Used - Like New",
    "used_good" to "Used - Good",
    "used_fair" to "Used - Fair",
)
private val FULFILLMENT_TYPES = listOf("pickup" to "Pickup", "delivery" to "Delivery", "both" to "Pickup or delivery")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateEditListingScreen(
    onBack: () -> Unit,
    viewModel: CreateEditListingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::uploadPhoto) }

    Scaffold(
        topBar = {
            KenaBechaTopBar(title = if (state.isEditing) "Edit listing" else "New listing", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            if (!state.isEditing && state.myShops.isNotEmpty()) {
                LabeledDropdown(
                    label = "Sell as",
                    options = listOf<ShopDto?>(null) + state.myShops,
                    selected = state.myShops.find { it.id == state.shopId },
                    optionLabel = { it?.shopName ?: "Personal listing" },
                    onSelect = { shop -> viewModel.update { it.copy(shopId = shop?.id) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
            }

            // Options are shop inventory only; a personal listing keeps the
            // single price it has always had.
            if (state.isShopListing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.hasVariants,
                        onCheckedChange = { on ->
                            viewModel.update {
                                it.copy(
                                    hasVariants = on,
                                    variants = if (on && it.variants.isEmpty()) listOf(VariantRow()) else it.variants,
                                )
                            }
                        },
                    )
                    Text("This item has multiple options")
                }
            }

            if (state.isShopListing && state.hasVariants) {
                state.variants.forEachIndexed { index, row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        OutlinedTextField(
                            value = row.name,
                            onValueChange = { v -> viewModel.updateVariant(index) { it.copy(name = v) } },
                            label = { Text("Option") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                        )
                        OutlinedTextField(
                            value = row.price,
                            onValueChange = { v -> viewModel.updateVariant(index) { it.copy(price = v) } },
                            label = { Text("৳") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = row.unit,
                            onValueChange = { v -> viewModel.updateVariant(index) { it.copy(unit = v) } },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(0.9f),
                        )
                        IconButton(onClick = { viewModel.removeVariant(index) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove option",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = row.isAvailable,
                            onCheckedChange = { v -> viewModel.updateVariant(index) { it.copy(isAvailable = v) } },
                        )
                        Text("Available", style = MaterialTheme.typography.bodySmall)
                    }
                }
                TextButton(onClick = viewModel::addVariant, enabled = state.variants.size < 20) {
                    Text("+ Add option")
                }
                Spacer(Modifier.height(10.dp))
            } else {
                LabeledDropdown(
                    label = "Price type",
                    options = PRICE_TYPES,
                    selected = PRICE_TYPES.find { it.first == state.priceType },
                    optionLabel = { it.second },
                    onSelect = { v -> viewModel.update { it.copy(priceType = v.first) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.priceType != "free") {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.price,
                            onValueChange = { v -> viewModel.update { it.copy(price = v) } },
                            label = { Text("Price (৳)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = state.unit,
                            onValueChange = { v -> viewModel.update { it.copy(unit = v) } },
                            label = { Text("Per unit (optional)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            LabeledDropdown(
                label = "Condition",
                options = CONDITIONS,
                selected = CONDITIONS.find { it.first == state.condition },
                optionLabel = { it.second },
                onSelect = { v -> viewModel.update { it.copy(condition = v.first) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            LabeledDropdown(
                label = "Category",
                options = state.categories,
                selected = state.categories.find { it.id == state.categoryId },
                optionLabel = CategoryDto::name,
                onSelect = { c -> viewModel.update { it.copy(categoryId = c.id, customCategory = "") } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.customCategory,
                onValueChange = { v -> viewModel.update { it.copy(customCategory = v, categoryId = null) } },
                label = { Text("Or a custom category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = state.tagsText,
                onValueChange = { v -> viewModel.update { it.copy(tagsText = v) } },
                label = { Text("Tags (comma separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            LabeledDropdown(
                label = "Fulfillment",
                options = FULFILLMENT_TYPES,
                selected = FULFILLMENT_TYPES.find { it.first == state.fulfillmentType },
                optionLabel = { it.second },
                onSelect = { v -> viewModel.update { it.copy(fulfillmentType = v.first) } },
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.fulfillmentType != "delivery") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.pickupAddress,
                    onValueChange = { v -> viewModel.update { it.copy(pickupAddress = v) } },
                    label = { Text("Pickup address") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }
            if (state.saved) {
                Text(
                    if (state.isEditing) "Saved" else "Listing created — add some photos below",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(if (state.isSaving) "Saving…" else if (state.isEditing) "Save changes" else "Create listing")
            }

            if (state.listingId != null) {
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
