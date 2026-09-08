package com.controlprestamos.features.audit.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max

private const val MONEY_TOLERANCE = 0.01

@Composable
fun FinancialAuditScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val loans = LocalLoanRepository.getAllLoans()
    val allPaymentHistory = LocalPaymentRepository.getAllPaymentHistory()

    val rows = loans
        .map { loan ->
            buildLoanAuditRow(loan)
        }
        .sortedWith(
            compareBy<LoanAuditRow> { row ->
                when (row.status) {
                    AuditStatus.ERROR -> 0
                    AuditStatus.WARNING -> 1
                    AuditStatus.OK -> 2
                }
            }.thenBy { row ->
                row.clientName
            }
        )

    val orphanPayments = allPaymentHistory.filter { payment ->
        loans.none { loan ->
            loan.id == payment.loanId
        }
    }

    val totals = buildAuditTotals(
        rows = rows,
        orphanPaymentCount = orphanPayments.size,
        orphanPaymentAmount = orphanPayments.sumOf { payment ->
            payment.amount
        }
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Auditoría financiera",
                subtitle = "Saldos, pagos y cuotas",
                showBack = true,
                onBack = onNavigateBack
            )
        },
        containerColor = AppColors.Background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = AppColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                AuditHeroCard(
                    totals = totals,
                    currencySymbol = preferences.currencySymbol
                )

                AuditSummaryCard(
                    totals = totals,
                    currencySymbol = preferences.currencySymbol
                )

                AuditRulesCard()

                SectionHeader(
                    title = "Préstamos revisados",
                    subtitle = "Los préstamos con errores aparecen primero.",
                    count = rows.size,
                    color = when {
                        totals.errorCount > 0 -> AppColors.Error
                        totals.warningCount > 0 -> AppColors.Warning
                        else -> AppColors.Success
                    }
                )

                if (rows.isEmpty()) {
                    EmptyState(
                        title = "Sin préstamos",
                        description = "Todavía no hay préstamos para auditar."
                    )
                } else {
                    rows.forEach { row ->
                        LoanAuditCard(
                            row = row,
                            currencySymbol = preferences.currencySymbol
                        )
                    }
                }

                if (orphanPayments.isNotEmpty()) {
                    OrphanPaymentsCard(
                        count = orphanPayments.size,
                        amount = orphanPayments.sumOf { payment ->
                            payment.amount
                        },
                        currencySymbol = preferences.currencySymbol
                    )
                }

                TechnicalReadingCard()

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun AuditHeroCard(
    totals: AuditTotals,
    currencySymbol: String
) {
    val statusColor = when {
        totals.errorCount > 0 -> AppColors.Error
        totals.warningCount > 0 -> AppColors.Warning
        else -> AppColors.Success
    }

    val icon = when {
        totals.errorCount > 0 -> "⚠️"
        totals.warningCount > 0 -> "🟡"
        else -> "✅"
    }

    val title = when {
        totals.errorCount > 0 -> "Hay diferencias que revisar"
        totals.warningCount > 0 -> "Hay alertas menores"
        else -> "Todo luce coherente"
    }

    val subtitle = when {
        totals.errorCount > 0 -> "Existen préstamos con diferencias financieras."
        totals.warningCount > 0 -> "La cartera está usable, pero hay detalles por revisar."
        else -> "Pagos, saldos y cuotas coinciden correctamente."
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Préstamos",
                    value = totals.loanCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                AuditMetricCard(
                    title = "Errores",
                    value = totals.errorCount.toString(),
                    modifier = Modifier.weight(1f),
                    danger = totals.errorCount > 0
                )

                AuditMetricCard(
                    title = "Alertas",
                    value = totals.warningCount.toString(),
                    modifier = Modifier.weight(1f),
                    warning = totals.warningCount > 0
                )
            }

            if (totals.orphanPaymentCount > 0) {
                AuditNoticeBox(
                    title = "Pagos sin préstamo",
                    value = "${totals.orphanPaymentCount} pagos · ${formatMoney(totals.orphanPaymentAmount, currencySymbol)}",
                    color = AppColors.Error
                )
            }
        }
    }
}

@Composable
private fun AuditSummaryCard(
    totals: AuditTotals,
    currencySymbol: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Resumen general",
                subtitle = "Comparación global entre préstamos, pagos activos y cuotas pendientes."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Total esperado",
                    value = formatMoney(totals.totalExpected, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                AuditMetricCard(
                    title = "Pagado activo",
                    value = formatMoney(totals.totalActivePaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    success = totals.totalActivePaid > 0.0
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Saldo por pagos",
                    value = formatMoney(totals.totalCalculatedBalance, currencySymbol),
                    modifier = Modifier.weight(1f),
                    warning = totals.totalCalculatedBalance > 0.0
                )

                AuditMetricCard(
                    title = "Pendiente cuotas",
                    value = formatMoney(totals.totalInstallmentPending, currencySymbol),
                    modifier = Modifier.weight(1f),
                    warning = totals.totalInstallmentPending > 0.0
                )
            }

            AuditDifferenceBox(
                calculatedBalance = totals.totalCalculatedBalance,
                installmentPending = totals.totalInstallmentPending,
                currencySymbol = currencySymbol
            )
        }
    }
}

@Composable
private fun AuditDifferenceBox(
    calculatedBalance: Double,
    installmentPending: Double,
    currencySymbol: String
) {
    val difference = calculatedBalance - installmentPending
    val hasDifference = abs(difference) > MONEY_TOLERANCE
    val color = if (hasDifference) AppColors.Error else AppColors.Success

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diferencia global",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = formatMoney(difference, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AuditMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    success: Boolean = false,
    danger: Boolean = false,
    warning: Boolean = false
) {
    val color = when {
        danger -> AppColors.Error
        warning -> AppColors.Warning
        success -> AppColors.Success
        else -> AppColors.Gray900
    }

    val backgroundColor = when {
        danger -> AppColors.Error.copy(alpha = 0.08f)
        warning -> AppColors.Warning.copy(alpha = 0.08f)
        success -> AppColors.Success.copy(alpha = 0.08f)
        else -> AppColors.SurfaceMuted
    }

    Surface(
        modifier = modifier.heightIn(min = 72.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AuditRulesCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Reglas verificadas",
                subtitle = "La auditoría no modifica datos. Solo compara información guardada."
            )

            AuditRuleItem(
                number = "1",
                text = "Pagos activos suman. Pagos anulados no suman."
            )

            AuditRuleItem(
                number = "2",
                text = "Saldo por pagos debe coincidir con pendiente de cuotas."
            )

            AuditRuleItem(
                number = "3",
                text = "Préstamo con saldo cero debe estar Pagado, salvo que esté Cancelado."
            )

            AuditRuleItem(
                number = "4",
                text = "Préstamo activo debe tener cuotas generadas."
            )
        }
    }
}

@Composable
private fun AuditRuleItem(
    number: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(AppColors.AccentTeal.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )
    }
}

@Composable
private fun LoanAuditCard(
    row: LoanAuditRow,
    currencySymbol: String
) {
    val statusColor = when (row.status) {
        AuditStatus.OK -> AppColors.Success
        AuditStatus.WARNING -> AppColors.Warning
        AuditStatus.ERROR -> AppColors.Error
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (row.status) {
                            AuditStatus.OK -> "✓"
                            AuditStatus.WARNING -> "!"
                            AuditStatus.ERROR -> "!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = row.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Préstamo: ${row.loanId.take(8)} · Estado: ${row.loanStatus}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray500
                    )
                }

                AuditStatusPill(
                    status = row.status
                )
            }

            Text(
                text = row.statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Saldo pagos",
                    value = formatMoney(row.calculatedBalance, currencySymbol),
                    modifier = Modifier.weight(1f),
                    warning = row.calculatedBalance > 0.0
                )

                AuditMetricCard(
                    title = "Pendiente cuotas",
                    value = formatMoney(row.installmentPending, currencySymbol),
                    modifier = Modifier.weight(1f),
                    danger = row.status == AuditStatus.ERROR
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Pagado activo",
                    value = formatMoney(row.activePaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    success = row.activePaid > 0.0
                )

                AuditMetricCard(
                    title = "Anulado",
                    value = formatMoney(row.cancelledAmount, currencySymbol),
                    modifier = Modifier.weight(1f),
                    warning = row.cancelledAmount > 0.0
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Cuotas",
                    value = row.installmentCount.toString(),
                    modifier = Modifier.weight(1f),
                    warning = row.installmentCount == 0
                )

                AuditMetricCard(
                    title = "Diferencia",
                    value = formatMoney(row.balanceDifference, currencySymbol),
                    modifier = Modifier.weight(1f),
                    danger = abs(row.balanceDifference) > MONEY_TOLERANCE
                )
            }

            if (row.messages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = statusColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(AppRadius.card),
                    border = BorderStroke(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.22f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        row.messages.forEach { message ->
                            Text(
                                text = "• $message",
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditStatusPill(
    status: AuditStatus
) {
    val color = when (status) {
        AuditStatus.OK -> AppColors.Success
        AuditStatus.WARNING -> AppColors.Warning
        AuditStatus.ERROR -> AppColors.Error
    }

    val text = when (status) {
        AuditStatus.OK -> "OK"
        AuditStatus.WARNING -> "Alerta"
        AuditStatus.ERROR -> "Error"
    }

    Surface(
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.28f)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = AppSpacing.sm,
                vertical = AppSpacing.xs
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun OrphanPaymentsCard(
    count: Int,
    amount: Double,
    currencySymbol: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Pagos sin préstamo",
                subtitle = "Se detectaron movimientos asociados a préstamos inexistentes."
            )

            AuditNoticeBox(
                title = "$count pagos detectados",
                value = formatMoney(amount, currencySymbol),
                color = AppColors.Error
            )

            Text(
                text = "Esto no debe corregirse borrando a mano. Primero revisa si viene de una restauración, datos antiguos o una migración incompleta.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun AuditNoticeBox(
    title: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TechnicalReadingCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Lectura técnica",
                subtitle = "Qué hacer si aparece una diferencia."
            )

            Text(
                text = "Esta pantalla no modifica datos. Solo compara lo que dicen los préstamos, pagos y cuotas para detectar diferencias antes de tocar backup, edición financiera o reportes avanzados.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Si aparece una diferencia, no borres datos manualmente. Primero reconstruye cuotas desde pagos activos y revisa el préstamo afectado.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )
    }
}

private fun buildAuditTotals(
    rows: List<LoanAuditRow>,
    orphanPaymentCount: Int,
    orphanPaymentAmount: Double
): AuditTotals {
    return AuditTotals(
        loanCount = rows.size,
        errorCount = rows.count { row ->
            row.status == AuditStatus.ERROR
        },
        warningCount = rows.count { row ->
            row.status == AuditStatus.WARNING
        },
        totalExpected = rows.sumOf { row ->
            row.totalExpected
        },
        totalActivePaid = rows.sumOf { row ->
            row.activePaid
        },
        totalCalculatedBalance = rows.sumOf { row ->
            row.calculatedBalance
        },
        totalInstallmentPending = rows.sumOf { row ->
            row.installmentPending
        },
        orphanPaymentCount = orphanPaymentCount,
        orphanPaymentAmount = orphanPaymentAmount
    )
}

private fun buildLoanAuditRow(
    loan: Loan
): LoanAuditRow {
    val client = LocalClientRepository.getClientById(loan.clientId)
    val payments = LocalPaymentRepository.getPaymentHistoryByLoan(loan.id)
    val installments = LocalInstallmentRepository.getInstallmentsByLoan(loan.id)

    val activePayments = payments.filter { payment ->
        FinancialOperationRules.shouldCountPaymentFinancially(payment)
    }

    val cancelledPayments = payments.filter { payment ->
        !FinancialOperationRules.shouldCountPaymentFinancially(payment)
    }

    val activePaid = activePayments.sumOf { payment ->
        payment.amount
    }

    val cancelledAmount = cancelledPayments.sumOf { payment ->
        payment.amount
    }

    val calculatedBalance = max(
        loan.totalExpectedAmount - activePaid,
        0.0
    )

    val activeInstallments = installments.filter { installment ->
        installment.status != InstallmentStatus.CANCELLED
    }

    val installmentPending = activeInstallments.sumOf { installment ->
        installment.pendingAmount
    }

    val installmentPaid = activeInstallments.sumOf { installment ->
        installment.paidAmount
    }

    val balanceDifference = calculatedBalance - installmentPending

    val messages = mutableListOf<String>()

    if (installments.isEmpty() && loan.status == LoanStatus.ACTIVE) {
        messages += "Préstamo activo sin cuotas generadas."
    }

    if (abs(balanceDifference) > MONEY_TOLERANCE) {
        messages += "Saldo por pagos no coincide con pendiente de cuotas."
    }

    if (calculatedBalance <= MONEY_TOLERANCE && loan.status == LoanStatus.ACTIVE) {
        messages += "El préstamo tiene saldo cero, pero sigue Activo."
    }

    if (calculatedBalance > MONEY_TOLERANCE && loan.status == LoanStatus.PAID) {
        messages += "El préstamo está Pagado, pero todavía tiene saldo pendiente."
    }

    if (activePaid > loan.totalExpectedAmount + MONEY_TOLERANCE) {
        messages += "Pagos activos superan el total esperado del préstamo."
    }

    if (cancelledPayments.isNotEmpty()) {
        messages += "Tiene pagos anulados visibles en historial."
    }

    val status = when {
        messages.any { message ->
            message.contains("no coincide", ignoreCase = true) ||
                message.contains("superan", ignoreCase = true) ||
                message.contains("todavía tiene saldo", ignoreCase = true)
        } -> AuditStatus.ERROR

        messages.isNotEmpty() -> AuditStatus.WARNING

        else -> AuditStatus.OK
    }

    val statusLabel = when (status) {
        AuditStatus.OK -> "OK: saldos y cuotas lucen coherentes."
        AuditStatus.WARNING -> "Alerta: hay detalles que revisar."
        AuditStatus.ERROR -> "Error: hay diferencia financiera."
    }

    return LoanAuditRow(
        loanId = loan.id,
        clientName = client?.fullName ?: "Cliente no encontrado",
        loanStatus = loan.status.label,
        totalExpected = loan.totalExpectedAmount,
        activePaid = activePaid,
        cancelledAmount = cancelledAmount,
        calculatedBalance = calculatedBalance,
        installmentPending = installmentPending,
        installmentPaid = installmentPaid,
        installmentCount = installments.size,
        balanceDifference = balanceDifference,
        status = status,
        statusLabel = statusLabel,
        messages = messages
    )
}

private data class AuditTotals(
    val loanCount: Int,
    val errorCount: Int,
    val warningCount: Int,
    val totalExpected: Double,
    val totalActivePaid: Double,
    val totalCalculatedBalance: Double,
    val totalInstallmentPending: Double,
    val orphanPaymentCount: Int,
    val orphanPaymentAmount: Double
)

private data class LoanAuditRow(
    val loanId: String,
    val clientName: String,
    val loanStatus: String,
    val totalExpected: Double,
    val activePaid: Double,
    val cancelledAmount: Double,
    val calculatedBalance: Double,
    val installmentPending: Double,
    val installmentPaid: Double,
    val installmentCount: Int,
    val balanceDifference: Double,
    val status: AuditStatus,
    val statusLabel: String,
    val messages: List<String>
)

private enum class AuditStatus {
    OK,
    WARNING,
    ERROR
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

