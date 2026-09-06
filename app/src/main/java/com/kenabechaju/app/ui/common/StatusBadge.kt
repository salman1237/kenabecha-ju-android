package com.kenabechaju.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kenabechaju.app.ui.theme.KbColors

private data class StatusColors(val container: Color, val onContainer: Color)

/**
 * Every status badge in the app used to be a bare `Badge { }`, which is
 * always Material3's red/error color regardless of what the status
 * actually means — "Active," "Sold," and "Pending" all rendered
 * identically red. This maps each real status to a semantic color instead.
 */
@Composable
private fun statusColors(status: String): StatusColors = when (status) {
    "active", "published" -> StatusColors(KbColors.successContainer, KbColors.onSuccessContainer)
    "pending", "out_of_stock" -> StatusColors(KbColors.warningContainer, KbColors.onWarningContainer)
    "sold" -> StatusColors(KbColors.infoContainer, KbColors.onInfoContainer)
    "rejected", "removed" -> StatusColors(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )
    else -> StatusColors( // "expired", "paused", anything unrecognized
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val colors = statusColors(status)
    Text(
        statusLabel(status),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = colors.onContainer,
        modifier = modifier
            .background(colors.container, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
