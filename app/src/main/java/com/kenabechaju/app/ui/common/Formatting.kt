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

/** The price to show for a listing in a list/card context. A listing with
 *  priced options shows its cheapest one prefixed with "From" — the server
 *  already keeps `price`/`unit` synced to that cheapest option, so this
 *  only has to add the cue. */
fun formatListingPrice(
    price: String?,
    priceType: String,
    unit: String?,
    variantCount: Int,
): String {
    val formatted = formatPrice(price, priceType, unit)
    return if (variantCount > 0 && formatted.isNotEmpty()) "From $formatted" else formatted
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
