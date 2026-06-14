package com.controlprestamos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun ProgressStatCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    AppCard(
        modifier = modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = AppColors.Gray100,
                        shape = RoundedCornerShape(AppRadius.sm)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(safeProgress)
                        .height(8.dp)
                        .background(
                            color = AppColors.AccentTeal,
                            shape = RoundedCornerShape(AppRadius.sm)
                        )
                )
            }

            Text(
                text = "${(safeProgress * 100).toInt()}% cobrado",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )
        }
    }
}
