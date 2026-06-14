package com.controlprestamos.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.AccentTeal
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            if (action != null) {
                action()
            }
        }
    }
}
