package com.controlprestamos.features.loans.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InstallmentTimelineCard(
    installments: List<Installment>,
    modifier: Modifier = Modifier
) {
    val paidCount = installments.count { it.status == InstallmentStatus.PAID }
    val overdueCount = installments.count { it.status == InstallmentStatus.OVERDUE }
    val pendingCount = installments.count {
        it.status == InstallmentStatus.PENDING || it.status == InstallmentStatus.PARTIAL
    }

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
                    text = "Calendario de cuotas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "${installments.size} cuotas · $paidCount pagadas · $pendingCount pendientes · $overdueCount vencidas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            }

            if (installments.isEmpty()) {
                Text(
                    text = "Este préstamo todavía no tiene cuotas generadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            } else {
                installments.take(10).forEach { installment ->
                    InstallmentTimelineRow(installment = installment)
                }

                if (installments.size > 10) {
                    Text(
                        text = "Mostrando 10 de ${installments.size} cuotas.",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray600
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallmentTimelineRow(
    installment: Installment
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    color = statusColor(installment.status),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Cuota #${installment.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = "Vence: ${formatDate(installment.dueDateMillis)} · ${formatMoney(installment.expectedAmount)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            if (installment.paidAmount > 0.0) {
                Text(
                    text = "Pagado: ${formatMoney(installment.paidAmount)} · Pendiente: ${formatMoney(installment.pendingAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray600
                )
            }
        }

        StatusChip(status = installment.status.toAppStatus())
    }
}

private fun InstallmentStatus.toAppStatus(): AppStatus {
    return when (this) {
        InstallmentStatus.PENDING -> AppStatus.PENDING
        InstallmentStatus.PARTIAL -> AppStatus.PENDING
        InstallmentStatus.PAID -> AppStatus.COMPLETED
        InstallmentStatus.OVERDUE -> AppStatus.OVERDUE
        InstallmentStatus.CANCELLED -> AppStatus.INACTIVE
    }
}

private fun statusColor(status: InstallmentStatus) = when (status) {
    InstallmentStatus.PENDING -> AppColors.Warning
    InstallmentStatus.PARTIAL -> AppColors.Warning
    InstallmentStatus.PAID -> AppColors.Success
    InstallmentStatus.OVERDUE -> AppColors.Error
    InstallmentStatus.CANCELLED -> AppColors.Gray400
}

private fun formatMoney(value: Double): String {
    return "$" + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}

