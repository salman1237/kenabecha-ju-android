package com.kenabechaju.app.ui.profile

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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.reportSubmitted) {
        if (state.reportSubmitted) {
            showReportDialog = false
            viewModel.dismissReportDialog()
        }
    }
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
                title = "Profile",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report this user")
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
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center)
            }
            state.profile != null -> {
                val profile = state.profile!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (profile.avatarUrl != null) {
                                AsyncImage(model = com.kenabechaju.app.ui.common.mediaUrl(profile.avatarUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                            } else {
                                Text(profile.fullName.take(1).uppercase(), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(profile.fullName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            if (profile.ratingCount > 0) {
                                Text(
                                    "★ ${"%.1f".format(profile.averageRating ?: 0.0)} (${profile.ratingCount})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text("No ratings yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    profile.bio?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 14.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    profile.hall?.let { Text("Hall: ${it.name}", style = MaterialTheme.typography.bodyMedium) }
                    profile.department?.let { Text("Department: ${it.name}", style = MaterialTheme.typography.bodyMedium) }
                    profile.session?.let { Text("Session: $it", style = MaterialTheme.typography.bodyMedium) }

                    if (profile.shops.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                        Text("Shops", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        profile.shops.forEach { shop ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            ) {
                                Text("🏪", modifier = Modifier.padding(end = 8.dp))
                                Text(shop.shopName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (profile.recentRatings.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                        Text("Recent reviews", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        profile.recentRatings.forEach { rating ->
                            RatingRow(rating, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingRow(rating: RatingDto, modifier: Modifier = Modifier) {
    KbCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                repeat(5) { i ->
                    Text(if (i < rating.stars) "★" else "☆", color = MaterialTheme.colorScheme.primary)
                }
            }
            rating.reviewText?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
            Text(
                "— ${rating.rater.fullName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
