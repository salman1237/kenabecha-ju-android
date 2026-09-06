package com.kenabechaju.app.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle

/**
 * Extracted from `LoginScreen.kt`'s title, which already proved this
 * compiles and renders correctly (emerald→teal brush text, matching web's
 * `.gradient-text` utility) — generalized so other hero moments (the
 * Browse screen's app title, section headers) can reuse it instead of
 * every screen inventing its own gradient brush inline.
 */
@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val brush = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    )
    Text(text, modifier = modifier, style = style.copy(brush = brush))
}
