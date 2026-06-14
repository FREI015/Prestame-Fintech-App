package com.controlprestamos.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

enum class DashboardAlertType {
    SUCCESS,
    WARNING,
    INFO
}

@Composable
fun DashboardAlertCard(
    title: String,
    message: String,
    type: DashboardAlertType,
    modifier: Modifier = Modifier
) {
    val accentColor = when (type) {
        DashboardAlertType.SUCCESS -> AppColors.Success
        DashboardAlertType.WARNING -> AppColors.Warning
        DashboardAlertType.INFO -> AppColors.AccentTeal
    }

    AppCard(
        modifier = modifier,
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}
