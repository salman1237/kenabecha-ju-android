package com.kenabechaju.app.ui.common

import java.text.DecimalFormat

/** Mirrors web's `formatPriceLocalized` (lib/i18n/format.ts): Taka sign on
 *  the left, grouped digits, trailing "(Negotiable)"/"/unit" suffixes —
 *  English-only for now, matching Phase 1-6's English-first scope. */
fun formatPrice(price: String?, priceType: String, unit: String? = null): String {
    if (priceType == "free") return "Free"
    val amount = price?.toDoubleOrNull() ?: return ""
    var result = "৳${DecimalFormat("#,##0.##").format(amount)}"
    if (!unit.isNullOrBlank()) result += "/$unit"
    if (priceType == "negotiable") result += " (Negotiable)"
    return result
}

fun conditionLabel(condition: String): String = when (condition) {
    "new" -> "New"
    "used_like_new" -> "Used - Like New"
    "used_good" -> "Used - Good"
    "used_fair" -> "Used - Fair"
    else -> condition
}

fun fulfillmentLabel(fulfillmentType: String): String = when (fulfillmentType) {
    "pickup" -> "Pickup"
    "delivery" -> "Delivery"
    "both" -> "Pickup or delivery"
    else -> fulfillmentType
}

fun statusLabel(status: String): String = when (status) {
    "active" -> "Active"
    "sold" -> "Sold"
    "out_of_stock" -> "Out of stock"
    "removed" -> "Removed"
    "expired" -> "Expired"
    "paused" -> "Paused"
    "pending" -> "Pending"
    "published" -> "Published"
    "rejected" -> "Rejected"
    else -> status
}
