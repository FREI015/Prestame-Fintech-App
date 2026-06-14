package com.controlprestamos.features.audit.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max

private const val MONEY_TOLERANCE = 0.01

@Composable
fun FinancialAuditScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val loans = LocalLoanRepository.getAllLoans()
    val allPaymentHistory = LocalPaymentRepository.getAllPaymentHistory()

    val rows = loans.map { loan ->
        buildLoanAuditRow(loan)
    }

    val orphanPayments = allPaymentHistory.filter { payment ->
        loans.none { loan -> loan.id == payment.loanId }
    }

    val totals = buildAuditTotals(
        rows = rows,
        orphanPaymentCount = orphanPayments.size,
        orphanPaymentAmount = orphanPayments.sumOf { it.amount }
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Auditoría financiera",
                subtitle = "Diagnóstico interno de saldos, pagos y cuotas",
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
                Text(
                    text = "Resumen general",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                AuditSummaryCard(
                    totals = totals,
                    currencySymbol = preferences.currencySymbol
                )

                AuditRulesCard()

                Text(
                    text = "Préstamos revisados",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
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
                        amount = orphanPayments.sumOf { it.amount },
                        currencySymbol = preferences.currencySymbol
                    )
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Lectura técnica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Esta pantalla no modifica datos. Solo compara lo que dicen los préstamos, pagos y cuotas para detectar diferencias antes de tocar backup, edición financiera o reportes avanzados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Si aparece una diferencia, el siguiente paso no es borrar datos: es reconstruir cuotas desde pagos activos y revisar el préstamo afectado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }
                }
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
            Text(
                text = when {
                    totals.errorCount > 0 -> "Hay diferencias que revisar"
                    totals.warningCount > 0 -> "Hay alertas menores"
                    else -> "Todo luce coherente"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    totals.errorCount > 0 -> AppColors.Error
                    totals.warningCount > 0 -> AppColors.Warning
                    else -> AppColors.Success
                }
            )

            Text(
                text = "Esta revisión compara saldos calculados contra cuotas guardadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

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
                    title = "Con error",
                    value = totals.errorCount.toString(),
                    modifier = Modifier.weight(1f),
                    danger = totals.errorCount > 0
                )
            }

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
                    modifier = Modifier.weight(1f)
                )

                AuditMetricCard(
                    title = "Pendiente cuotas",
                    value = formatMoney(totals.totalInstallmentPending, currencySymbol),
                    modifier = Modifier.weight(1f)
                )
            }

            if (totals.orphanPaymentCount > 0) {
                Text(
                    text = "Pagos sin préstamo detectados: ${totals.orphanPaymentCount} por ${formatMoney(totals.orphanPaymentAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Error
                )
            }
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
    AppCard(
        modifier = modifier,
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    danger -> AppColors.Error
                    warning -> AppColors.Warning
                    success -> AppColors.Success
                    else -> AppColors.Gray900
                }
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Reglas que se están verificando",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "1. Pagos activos suman. Pagos anulados no suman.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "2. Saldo por pagos debe coincidir con pendiente de cuotas.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "3. Préstamo con saldo cero debe estar Pagado, salvo que esté Cancelado.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "4. Préstamo activo debe tener cuotas generadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun LoanAuditCard(
    row: LoanAuditRow,
    currencySymbol: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = row.clientName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Préstamo: ${row.loanId.take(8)} · Estado: ${row.loanStatus}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = row.statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when (row.status) {
                    AuditStatus.OK -> AppColors.Success
                    AuditStatus.WARNING -> AppColors.Warning
                    AuditStatus.ERROR -> AppColors.Error
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AuditMetricCard(
                    title = "Saldo pagos",
                    value = formatMoney(row.calculatedBalance, currencySymbol),
                    modifier = Modifier.weight(1f)
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    row.messages.forEach { message ->
                        Text(
                            text = "• $message",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (row.status) {
                                AuditStatus.ERROR -> AppColors.Error
                                AuditStatus.WARNING -> AppColors.Warning
                                AuditStatus.OK -> AppColors.Gray600
                            }
                        )
                    }
                }
            }
        }
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
            Text(
                text = "Pagos sin préstamo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Error
            )

            Text(
                text = "Se detectaron $count pagos asociados a préstamos inexistentes por ${formatMoney(amount, currencySymbol)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Esto no debe corregirse borrando a mano. Primero hay que revisar si viene de una restauración o de datos antiguos.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

private fun buildAuditTotals(
    rows: List<LoanAuditRow>,
    orphanPaymentCount: Int,
    orphanPaymentAmount: Double
): AuditTotals {
    return AuditTotals(
        loanCount = rows.size,
        errorCount = rows.count { it.status == AuditStatus.ERROR },
        warningCount = rows.count { it.status == AuditStatus.WARNING },
        totalExpected = rows.sumOf { it.totalExpected },
        totalActivePaid = rows.sumOf { it.activePaid },
        totalCalculatedBalance = rows.sumOf { it.calculatedBalance },
        totalInstallmentPending = rows.sumOf { it.installmentPending },
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

    val activePayments = payments.filter { FinancialOperationRules.shouldCountPaymentFinancially(it) }
    val cancelledPayments = payments.filter { !FinancialOperationRules.shouldCountPaymentFinancially(it) }

    val activePaid = activePayments.sumOf { it.amount }
    val cancelledAmount = cancelledPayments.sumOf { it.amount }

    val calculatedBalance = max(
        loan.totalExpectedAmount - activePaid,
        0.0
    )

    val installmentPending = installments
        .filter { it.status != InstallmentStatus.CANCELLED }
        .sumOf { it.pendingAmount }

    val installmentPaid = installments
        .filter { it.status != InstallmentStatus.CANCELLED }
        .sumOf { it.paidAmount }

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
        messages.any {
            it.contains("no coincide", ignoreCase = true) ||
                it.contains("superan", ignoreCase = true) ||
                it.contains("todavía tiene saldo", ignoreCase = true)
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


