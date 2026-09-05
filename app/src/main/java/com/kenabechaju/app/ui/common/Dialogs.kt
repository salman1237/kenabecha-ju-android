package com.kenabechaju.app.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val REPORT_REASONS = listOf(
    "spam" to "Spam",
    "scam_fraud" to "Scam or fraud",
    "inappropriate_content" to "Inappropriate content",
    "counterfeit" to "Counterfeit item",
    "harassment" to "Harassment",
    "other" to "Other",
)

/** Shared by listing/shop/user report entry points — the reason code list
 *  is the exact same across all three target types server-side. */
@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (reasonCode: String, note: String?) -> Unit,
    isSubmitting: Boolean,
    error: String?,
) {
    var reasonCode by remember { mutableStateOf(REPORT_REASONS.first().first) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report") },
        text = {
            Column {
                LabeledDropdown(
                    label = "Reason",
                    options = REPORT_REASONS,
                    selected = REPORT_REASONS.find { it.first == reasonCode },
                    optionLabel = { it.second },
                    onSelect = { reasonCode = it.first },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Details (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(reasonCode, note.trim().ifBlank { null }) }, enabled = !isSubmitting) {
                Text(if (isSubmitting) "Submitting…" else "Submit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun RateDialog(
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, reviewText: String?) -> Unit,
    isSubmitting: Boolean,
    error: String?,
) {
    var stars by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this transaction") },
        text = {
            Column {
                Row {
                    repeat(5) { i ->
                        IconButton(onClick = { stars = i + 1 }) {
                            Icon(
                                imageVector = if (i < stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "${i + 1} stars",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Review (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(stars, reviewText.trim().ifBlank { null }) }, enabled = !isSubmitting) {
                Text(if (isSubmitting) "Submitting…" else "Submit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
