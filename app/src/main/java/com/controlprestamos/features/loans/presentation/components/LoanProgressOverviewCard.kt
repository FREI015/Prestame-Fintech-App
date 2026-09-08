package com.controlprestamos.features.loans.presentation.components

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
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import java.text.DecimalFormat

@Composable
fun LoanProgressOverviewCard(
    totalExpected: Double,
    totalPaid: Double,
    remainingAmount: Double,
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
                text = "Avance del préstamo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = formatMoney(totalPaid),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )

            Text(
                text = "Cobrado de ${formatMoney(totalExpected)}",
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
                text = "${(safeProgress * 100).toInt()}% cobrado · Pendiente: ${formatMoney(remainingAmount)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )
        }
    }
}

private fun formatMoney(value: Double): String {
    return "$" + DecimalFormat("#,##0.00").format(value)
}

