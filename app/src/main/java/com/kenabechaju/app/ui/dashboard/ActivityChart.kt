package com.kenabechaju.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.kenabechaju.app.data.remote.dto.ActivityPointDto

/**
 * A plain Canvas bar chart for the trailing-30-days listing activity --
 * the web dashboard's own `ActivityChart` had no Android equivalent at
 * all. No charting library: the shape is one series of small whole-number
 * counts over a fixed window, well within what a few `drawRoundRect` calls
 * handle directly, matching this codebase's existing preference (seen in
 * `KbCard`'s own shadow work) for native Compose drawing over a new
 * dependency for something this simple.
 */
@Composable
fun ActivityChart(points: List<ActivityPointDto>, modifier: Modifier = Modifier) {
    val maxCount = (points.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "New listings, last ${points.size} days",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "${points.sumOf { it.count }} total",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val barCount = points.size
                if (barCount == 0) return@Canvas
                val gap = 2.dp.toPx()
                val barWidth = (size.width - gap * (barCount - 1)) / barCount
                points.forEachIndexed { index, point ->
                    val barHeight = (point.count.toFloat() / maxCount) * size.height
                    val x = index * (barWidth + gap)
                    // Full-height faint track behind every bar, so a
                    // zero-count day still reads as "a day with no
                    // activity" rather than empty space indistinguishable
                    // from outside the chart's own bounds.
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )
                    if (point.count > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight.coerceAtLeast(3.dp.toPx())),
                            cornerRadius = CornerRadius(3.dp.toPx()),
                        )
                    }
                }
            }
            if (points.isNotEmpty()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    Text(points.first().date.takeLast(5), style = MaterialTheme.typography.labelSmall, color = labelColor)
                    Text(points.last().date.takeLast(5), style = MaterialTheme.typography.labelSmall, color = labelColor)
                }
            }
        }
    }
}
