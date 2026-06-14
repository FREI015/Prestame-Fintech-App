package com.controlprestamos.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.controlprestamos.R
import com.controlprestamos.core.ui.theme.AppColors

@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    onOpenDashboard: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenPayments: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = onOpenDashboard,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_home),
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text(text = "Inicio")
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = currentRoute == "clients",
            onClick = onOpenClients,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_clients),
                    contentDescription = "Clientes"
                )
            },
            label = {
                Text(text = "Clientes")
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = currentRoute == "loans",
            onClick = onOpenLoans,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_loans),
                    contentDescription = "Préstamos"
                )
            },
            label = {
                Text(text = "Préstamos")
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = currentRoute == "payments",
            onClick = onOpenPayments,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_payments),
                    contentDescription = "Pagos"
                )
            },
            label = {
                Text(text = "Pagos")
            },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppColors.AccentTeal,
    selectedTextColor = AppColors.AccentTeal,
    indicatorColor = AppColors.AccentTeal.copy(alpha = 0.12f),
    unselectedIconColor = AppColors.Gray600,
    unselectedTextColor = AppColors.Gray600
)
