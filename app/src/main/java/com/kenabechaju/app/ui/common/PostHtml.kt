package com.kenabechaju.app.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Turns a post's already-sanitized `description_html` into an
 * `AnnotatedString`, instead of a `WebView` — the backend's own allow-list
 * (`p br b strong i em u span` + a handful of inline style properties,
 * see `sanitize.py`) is tiny enough that a WebView would be a full browser
 * engine for six tags. Assumes well-formed, properly-nested tags, which
 * holds here because the only path content takes to storage is through
 * that sanitizer — it never emits malformed markup.
 */
private val TAG_REGEX = Regex("<(/?)([a-zA-Z]+)([^>]*)>")
private val STYLE_DECL_REGEX = Regex("style=\"([^\"]*)\"")

fun parsePostHtml(html: String): AnnotatedString = buildAnnotatedString {
    var pos = 0
    var firstParagraph = true

    for (match in TAG_REGEX.findAll(html)) {
        val text = html.substring(pos, match.range.first)
        if (text.isNotEmpty()) append(decodeHtmlEntities(text))
        pos = match.range.last + 1

        val closing = match.groupValues[1] == "/"
        val tag = match.groupValues[2].lowercase()
        val attrs = match.groupValues[3]

        when (tag) {
            "p" -> if (!closing) {
                if (!firstParagraph) append("\n")
                firstParagraph = false
            }
            "br" -> append("\n")
            "b", "strong" -> if (!closing) pushStyle(SpanStyle(fontWeight = FontWeight.Bold)) else pop()
            "i", "em" -> if (!closing) pushStyle(SpanStyle(fontStyle = FontStyle.Italic)) else pop()
            "u" -> if (!closing) pushStyle(SpanStyle(textDecoration = TextDecoration.Underline)) else pop()
            "span" -> if (!closing) pushStyle(parseSpanStyle(attrs)) else pop()
        }
    }
    val rest = html.substring(pos)
    if (rest.isNotEmpty()) append(decodeHtmlEntities(rest))
}

private fun decodeHtmlEntities(s: String): String = s
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")

private fun parseSpanStyle(attrs: String): SpanStyle {
    val styleValue = STYLE_DECL_REGEX.find(attrs)?.groupValues?.get(1) ?: return SpanStyle()
    var color: Color? = null
    var fontWeight: FontWeight? = null
    var fontStyle: FontStyle? = null
    var textDecoration: TextDecoration? = null

    for (declaration in styleValue.split(";")) {
        val parts = declaration.split(":", limit = 2)
        if (parts.size != 2) continue
        val prop = parts[0].trim().lowercase()
        val value = parts[1].trim()
        when (prop) {
            "color" -> color = parseCssColor(value)
            "font-weight" -> if (value == "bold" || (value.toIntOrNull() ?: 0) >= 600) fontWeight = FontWeight.Bold
            "font-style" -> if (value == "italic") fontStyle = FontStyle.Italic
            "text-decoration" -> if (value.contains("underline")) textDecoration = TextDecoration.Underline
        }
    }
    return SpanStyle(color = color ?: Color.Unspecified, fontWeight = fontWeight, fontStyle = fontStyle, textDecoration = textDecoration)
}

private fun parseCssColor(value: String): Color? = try {
    android.graphics.Color.parseColor(value).let(::Color)
} catch (e: IllegalArgumentException) {
    null
}
