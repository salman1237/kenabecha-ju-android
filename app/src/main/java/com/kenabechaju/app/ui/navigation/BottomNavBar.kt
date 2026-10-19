package com.kenabechaju.app.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The five top-level destinations a signed-in user actually returns to
 *  over and over — everything else (listing detail, chat thread, forms,
 *  shop storefronts) is a drill-down from one of these and hides the bar,
 *  the same convention Material's own bottom-nav guidance describes. */
private enum class BottomDestination(val route: String) {
    BROWSE(Routes.HOME),
    SHOPS(Routes.BROWSE_SHOPS),
    SELL(Routes.CREATE_LISTING),
    INBOX(Routes.INBOX),
    ACCOUNT(Routes.ACCOUNT_SETTINGS),
}

/** Routes that should show the bar, as the currently-active tab. Anything
 *  not in this set (detail screens, forms, chat threads, auth) hides it
 *  entirely rather than showing it with nothing selected. */
val BOTTOM_NAV_ROUTES: Set<String> = BottomDestination.entries.map { it.route }.toSet()

@Composable
fun KenaBechaBottomBar(
    currentRoute: String?,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == BottomDestination.BROWSE.route,
            onClick = { onNavigate(BottomDestination.BROWSE.route) },
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.BROWSE.route) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = null,
                )
            },
            label = { Text("Browse") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.SHOPS.route,
            onClick = { onNavigate(BottomDestination.SHOPS.route) },
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.SHOPS.route) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                    contentDescription = null,
                )
            },
            label = { Text("Shops") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.SELL.route,
            onClick = { onNavigate(BottomDestination.SELL.route) },
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.SELL.route) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            label = { Text("Sell") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.INBOX.route,
            onClick = { onNavigate(BottomDestination.INBOX.route) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text(if (unreadCount > 9) "9+" else unreadCount.toString()) }
                        }
                    },
                ) {
                    Icon(
                        if (currentRoute == BottomDestination.INBOX.route) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubble,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            label = { Text("Inbox") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.ACCOUNT.route,
            onClick = { onNavigate(BottomDestination.ACCOUNT.route) },
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.ACCOUNT.route) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = null,
                )
            },
            label = { Text("Account") },
        )
    }
}
