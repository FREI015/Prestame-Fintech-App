package com.controlprestamos.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.controlprestamos.R
import com.controlprestamos.core.ui.theme.AppColors

data class AppBottomNavigationItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

val AppBottomNavigationItems = listOf(
    AppBottomNavigationItem(
        route = "dashboard",
        label = "Inicio",
        iconRes = R.drawable.ic_nav_home
    ),
    AppBottomNavigationItem(
        route = "clients",
        label = "Clientes",
        iconRes = R.drawable.ic_nav_clients
    ),
    AppBottomNavigationItem(
        route = "loans",
        label = "Préstamos",
        iconRes = R.drawable.ic_nav_loans
    ),
    AppBottomNavigationItem(
        route = "payments",
        label = "Pagos",
        iconRes = R.drawable.ic_nav_payments
    )
)

@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = AppColors.Surface,
        contentColor = AppColors.Gray600
    ) {
        AppBottomNavigationItems.forEach { item ->
            val selected = isBottomItemSelected(
                currentRoute = currentRoute,
                itemRoute = item.route
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onNavigate(item.route)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.AccentTeal,
                    selectedTextColor = AppColors.AccentTeal,
                    indicatorColor = AppColors.AccentTeal.copy(alpha = 0.12f),
                    unselectedIconColor = AppColors.Gray400,
                    unselectedTextColor = AppColors.Gray600
                )
            )
        }
    }
}

private fun isBottomItemSelected(
    currentRoute: String?,
    itemRoute: String
): Boolean {
    if (currentRoute == null) return itemRoute == "dashboard"

    return when (itemRoute) {
        "dashboard" -> currentRoute == "dashboard"
        "clients" -> currentRoute.startsWith("clients")
        "loans" -> currentRoute.startsWith("loans") || currentRoute.contains("/loans")
        "payments" -> currentRoute.startsWith("payments") || currentRoute.contains("/payments")
        else -> false
    }
}
