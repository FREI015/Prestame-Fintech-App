package com.controlprestamos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import java.text.DecimalFormat

data class ChartBarItem(
    val label: String,
    val value: Double
)

@Composable
fun ChartCard(
    title: String,
    subtitle: String,
    items: List<ChartBarItem>,
    modifier: Modifier = Modifier
) {
    val maxValue = items.maxOfOrNull { it.value } ?: 0.0
    val total = items.sumOf { it.value }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            }

            if (items.isEmpty() || maxValue <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .background(
                            color = AppColors.Gray100,
                            shape = RoundedCornerShape(AppRadius.card)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin cobros para graficar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items.forEach { item ->
                        ChartBar(
                            item = item,
                            maxValue = maxValue
                        )
                    }
                }
            }

            Text(
                text = "Total mostrado: ${formatMoney(total)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun ChartBar(
    item: ChartBarItem,
    maxValue: Double
) {
    val normalized = if (maxValue <= 0.0) 0.0 else item.value / maxValue
    val barHeight = (24 + (normalized * 96)).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(barHeight)
                .background(
                    color = AppColors.AccentTeal,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray600
        )
    }
}

private fun formatMoney(value: Double): String {
    return "$" + DecimalFormat("#,##0.00").format(value)
}
