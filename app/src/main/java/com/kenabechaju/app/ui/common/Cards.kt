package com.kenabechaju.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kenabechaju.app.ui.theme.KbColors

/**
 * Replaces the ~11 ad hoc `Card(...)` call sites across the app that used
 * bare Material3 defaults (near-invisible on a white background — 1dp of
 * plain black-tinted elevation). Uses an explicit brand-tinted shadow
 * (approximating web's `--shadow-soft-primary`) instead of Card's own
 * default elevation shadow, plus a faint primary-tinted border — the
 * "glow card" look web's `GradientCard.tsx` has, within what a real
 * blur-less `Modifier.shadow` can do at this app's minSdk 24.
 */
@Composable
fun KbCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shadowTint = KbColors.shadowPrimary
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val shadowedModifier = modifier.shadow(
        elevation = 4.dp,
        shape = shape,
        ambientColor = shadowTint,
        spotColor = shadowTint,
    )
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(1.dp, borderColor)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = shadowedModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content,
        )
    } else {
        Card(
            modifier = shadowedModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content,
        )
    }
}
