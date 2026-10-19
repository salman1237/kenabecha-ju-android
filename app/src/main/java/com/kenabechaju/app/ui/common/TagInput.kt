package com.kenabechaju.app.ui.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/** A chip-based tag editor — each committed tag renders as a removable
 *  `InputChip`, matching the web app's own tag UX, instead of making the
 *  seller type and re-edit a single raw comma-separated string by hand.
 *  Typing a comma (or pressing the keyboard's "Done"/"Next" action) commits
 *  the current draft as a new chip. [maxTags] mirrors the 10-tag cap the
 *  submit path already enforces, so the limit is visible before submit
 *  rejects it rather than after. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Tags",
    maxTags: Int = 10,
) {
    var draft by remember { mutableStateOf("") }

    fun commitDraft() {
        val tag = draft.trim().trimEnd(',')
        draft = ""
        if (tag.isEmpty() || tags.size >= maxTags || tags.any { it.equals(tag, ignoreCase = true) }) return
        onTagsChange(tags + tag)
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = { onTagsChange(tags - tag) },
                label = { Text(tag) },
                trailingIcon = {
                    Icon(Icons.Filled.Close, contentDescription = "Remove $tag", modifier = Modifier.size(InputChipDefaults.IconSize))
                },
            )
        }
    }
    OutlinedTextField(
        value = draft,
        onValueChange = { value ->
            if (value.endsWith(",")) commitDraft() else draft = value
        },
        label = { Text(if (tags.size >= maxTags) "$label (max $maxTags reached)" else label) },
        singleLine = true,
        enabled = tags.size < maxTags,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { commitDraft() }),
        modifier = modifier.fillMaxWidth().padding(top = 6.dp),
    )
}
