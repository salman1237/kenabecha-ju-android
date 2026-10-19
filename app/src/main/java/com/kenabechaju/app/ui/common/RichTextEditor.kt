package com.kenabechaju.app.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/** The same five accent colors the web composer offers (`PostEditor.tsx`'s
 *  `COLOR_OPTIONS`) — matched by hex, not re-picked, so a post colored on
 *  one platform renders identically on the other. */
val POST_TEXT_COLORS: List<Pair<String, String>> = listOf(
    "Emerald" to "#059669",
    "Red" to "#dc2626",
    "Blue" to "#2563eb",
    "Amber" to "#d97706",
    "Violet" to "#7c3aed",
)

private fun colorFor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

/** Wraps the current selection in [prefix]/[suffix] (or inserts an empty
 *  pair at the cursor with the cursor left between them, so typing
 *  continues inside the new formatting). This is the one operation every
 *  toolbar button performs, just with different markers. */
private fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val start = value.selection.min
    val end = value.selection.max
    val newText = value.text.substring(0, start) + prefix + value.text.substring(start, end) + suffix + value.text.substring(end)
    val newSelection = if (start == end) {
        TextRange(start + prefix.length)
    } else {
        TextRange(start + prefix.length, end + prefix.length)
    }
    return TextFieldValue(newText, newSelection)
}

/**
 * A post body editor with Bold/Italic/Underline/Color controls — the web
 * composer is a full Tiptap WYSIWYG; this is a bounded but real equivalent
 * rather than attempting a WebView-based rich editor. Formatting is kept
 * as plain inline markers in the edited text itself (`**bold**`, `_italic_`,
 * `~underline~`, `[c:Emerald]text[/c]`) — visible while typing, like a
 * lightweight markdown editor — and converted to/from the same sanitized
 * HTML the backend already stores via [editableTextToHtml]/
 * [htmlToEditableText]. The actual rendered (not raw-marker) result is what
 * `parsePostHtml` already shows on every read surface (PostCard,
 * PostDetailScreen), so formatting applied here is real, not cosmetic.
 */
@Composable
fun RichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "What's new?",
) {
    var fieldValue by remember(Unit) { mutableStateOf(TextFieldValue(value)) }
    // Keeps the field in sync if the ViewModel's own string changes from
    // outside this composable (e.g. loading an existing post to edit),
    // without fighting the field's own selection state on every keystroke.
    if (fieldValue.text != value) {
        fieldValue = TextFieldValue(value, TextRange(value.length))
    }
    var colorMenuExpanded by remember { mutableStateOf(false) }

    fun apply(prefix: String, suffix: String = prefix) {
        val updated = wrapSelection(fieldValue, prefix, suffix)
        fieldValue = updated
        onValueChange(updated.text)
    }

    Row {
        IconButton(onClick = { apply("**") }) {
            Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
        }
        IconButton(onClick = { apply("_") }) {
            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
        }
        IconButton(onClick = { apply("~") }) {
            Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline")
        }
        Box {
            IconButton(onClick = { colorMenuExpanded = true }) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(colorFor(POST_TEXT_COLORS[0].second))
                }
            }
            DropdownMenu(expanded = colorMenuExpanded, onDismissRequest = { colorMenuExpanded = false }) {
                POST_TEXT_COLORS.forEach { (name, hex) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        leadingIcon = {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
                                drawCircle(colorFor(hex))
                            }
                        },
                        onClick = {
                            apply("[c:$name]", "[/c]")
                            colorMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = { updated ->
            fieldValue = updated
            onValueChange(updated.text)
        },
        label = { Text(label) },
        minLines = 5,
        modifier = modifier.padding(top = 4.dp),
    )
}

private val BOLD_REGEX = Regex("\\*\\*(.+?)\\*\\*", RegexOption.DOT_MATCHES_ALL)
private val ITALIC_REGEX = Regex("_(.+?)_", RegexOption.DOT_MATCHES_ALL)
private val UNDERLINE_REGEX = Regex("~(.+?)~", RegexOption.DOT_MATCHES_ALL)
private val COLOR_REGEX = Regex("\\[c:([A-Za-z]+)](.+?)\\[/c]", RegexOption.DOT_MATCHES_ALL)
private val COLOR_BY_NAME = POST_TEXT_COLORS.toMap()
private val NAME_BY_COLOR = POST_TEXT_COLORS.associate { (name, hex) -> hex to name }

private fun escapeHtml(s: String): String = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

/** Marker text -> the same sanitized HTML shape the web editor produces
 *  (`<p>` per paragraph, `<b>`/`<i>`/`<u>`/`<span style="color:...">`
 *  inline) — color applied outermost so `**[c:Red]bold red[/c]**` nests as
 *  `<b><span ...>bold red</span></b>`, matching how the regex substitution
 *  order below actually runs. */
fun editableTextToHtml(text: String): String =
    text.split("\n").filter { it.isNotBlank() }.joinToString("") { line ->
        var escaped = escapeHtml(line.trim())
        escaped = COLOR_REGEX.replace(escaped) { m ->
            val hex = COLOR_BY_NAME[m.groupValues[1]] ?: return@replace m.groupValues[2]
            "<span style=\"color:$hex\">${m.groupValues[2]}</span>"
        }
        escaped = BOLD_REGEX.replace(escaped) { "<b>${it.groupValues[1]}</b>" }
        escaped = ITALIC_REGEX.replace(escaped) { "<i>${it.groupValues[1]}</i>" }
        escaped = UNDERLINE_REGEX.replace(escaped) { "<u>${it.groupValues[1]}</u>" }
        "<p>$escaped</p>"
    }

private val HTML_COLOR_SPAN_REGEX = Regex("<span style=\"color:(#[0-9a-fA-F]{6})\">(.*?)</span>", RegexOption.DOT_MATCHES_ALL)

/** The inverse of [editableTextToHtml] — unlike the old plain-text
 *  composer's `htmlToPlainText`, this preserves formatting instead of
 *  discarding it, so editing a post authored (with formatting) on web no
 *  longer silently strips it the moment it's opened on Android. */
fun htmlToEditableText(html: String): String {
    var text = html
    text = HTML_COLOR_SPAN_REGEX.replace(text) { m ->
        val name = NAME_BY_COLOR[m.groupValues[1].lowercase()] ?: return@replace m.groupValues[2]
        "[c:$name]${m.groupValues[2]}[/c]"
    }
    text = text
        .replace(Regex("</?(b|strong)>"), "**")
        .replace(Regex("</?(i|em)>"), "_")
        .replace(Regex("</?u>"), "~")
        .replace(Regex("</p>\\s*<p>"), "\n")
        .replace(Regex("</?p>"), "")
        .replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]*>"), "") // any other tag this editor doesn't offer a control for (e.g. font-family spans) is stripped, not preserved
    return text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .trim()
}
