package com.kenabechaju.app.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
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

/** The seven top-level destinations a signed-in user returns to over and
 *  over — everything else (listing detail, chat thread, forms, shop
 *  storefronts) is a drill-down from one of these and hides the bar, the
 *  same convention Material's own bottom-nav guidance describes. Labels
 *  only show for the selected item (`alwaysShowLabel = false` below) so
 *  seven items stay legible instead of cramming seven permanent labels
 *  into one row. */
private enum class BottomDestination(val route: String, val label: String) {
    BROWSE(Routes.HOME, "Browse"),
    SHOPS(Routes.BROWSE_SHOPS, "Shops"),
    SELL(Routes.CREATE_LISTING, "Sell"),
    INBOX(Routes.INBOX, "Inbox"),
    NOTIFICATIONS(Routes.NOTIFICATIONS, "Alerts"),
    DASHBOARD(Routes.DASHBOARD, "Dashboard"),
    ACCOUNT(Routes.ACCOUNT_SETTINGS, "Account"),
}

/** Routes that should show the bar, as the currently-active tab. Anything
 *  not in this set (detail screens, forms, chat threads, auth) hides it
 *  entirely rather than showing it with nothing selected. */
val BOTTOM_NAV_ROUTES: Set<String> = BottomDestination.entries.map { it.route }.toSet()

@Composable
fun KenaBechaBottomBar(
    currentRoute: String?,
    unreadMessageCount: Int,
    unreadNotificationCount: Int,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == BottomDestination.BROWSE.route,
            onClick = { onNavigate(BottomDestination.BROWSE.route) },
            alwaysShowLabel = false,
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.BROWSE.route) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = null,
                )
            },
            label = { Text(BottomDestination.BROWSE.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.SHOPS.route,
            onClick = { onNavigate(BottomDestination.SHOPS.route) },
            alwaysShowLabel = false,
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.SHOPS.route) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                    contentDescription = null,
                )
            },
            label = { Text(BottomDestination.SHOPS.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.SELL.route,
            onClick = { onNavigate(BottomDestination.SELL.route) },
            alwaysShowLabel = false,
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.SELL.route) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            label = { Text(BottomDestination.SELL.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.INBOX.route,
            onClick = { onNavigate(BottomDestination.INBOX.route) },
            alwaysShowLabel = false,
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessageCount > 0) {
                            Badge { Text(if (unreadMessageCount > 9) "9+" else unreadMessageCount.toString()) }
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
            label = { Text(BottomDestination.INBOX.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.NOTIFICATIONS.route,
            onClick = { onNavigate(BottomDestination.NOTIFICATIONS.route) },
            alwaysShowLabel = false,
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge { Text(if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString()) }
                        }
                    },
                ) {
                    Icon(
                        if (currentRoute == BottomDestination.NOTIFICATIONS.route) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            label = { Text(BottomDestination.NOTIFICATIONS.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.DASHBOARD.route,
            onClick = { onNavigate(BottomDestination.DASHBOARD.route) },
            alwaysShowLabel = false,
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.DASHBOARD.route) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = null,
                )
            },
            label = { Text(BottomDestination.DASHBOARD.label) },
        )
        NavigationBarItem(
            selected = currentRoute == BottomDestination.ACCOUNT.route,
            onClick = { onNavigate(BottomDestination.ACCOUNT.route) },
            alwaysShowLabel = false,
            icon = {
                Icon(
                    if (currentRoute == BottomDestination.ACCOUNT.route) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = null,
                )
            },
            label = { Text(BottomDestination.ACCOUNT.label) },
        )
    }
}
