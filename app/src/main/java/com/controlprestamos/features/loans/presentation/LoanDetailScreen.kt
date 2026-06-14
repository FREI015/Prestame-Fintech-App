package com.controlprestamos.features.loans.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private var screenCurrencySymbol = "$"

@Composable
fun LoanDetailScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onCreatePayment: () -> Unit,
    onOpenPayments: () -> Unit,
    onEditLoan: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)
    screenCurrencySymbol = preferences.currencySymbol

    val loan = LocalLoanRepository.getLoanById(loanId)
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del préstamo",
                subtitle = client?.fullName ?: "Préstamo no encontrado",
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
                if (loan == null) {
                    EmptyState(
                        title = "Préstamo no encontrado",
                        description = "No pudimos encontrar el préstamo solicitado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                val installments = getOrCreateInstallments(loan)
                val totalPaid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
                val remainingAmount = max(loan.totalExpectedAmount - totalPaid, 0.0)
                val pendingInstallments = installments.filter {
                    it.status == InstallmentStatus.PENDING ||
                        it.status == InstallmentStatus.PARTIAL ||
                        it.status == InstallmentStatus.OVERDUE
                }
                val overdueInstallments = installments.filter {
                    it.status == InstallmentStatus.OVERDUE
                }
                val paidInstallments = installments.filter {
                    it.status == InstallmentStatus.PAID
                }
                val nextInstallment = pendingInstallments
                    .filter { it.pendingAmount > 0.0 }
                    .minByOrNull { it.dueDateMillis }

                val status = resolveLoanStatus(
                    loan = loan,
                    remainingAmount = remainingAmount,
                    overdueCount = overdueInstallments.size
                )

                LoanHeaderCard(
                    loan = loan,
                    clientName = client?.fullName ?: "Cliente no encontrado",
                    clientPhone = client?.phone.orEmpty(),
                    totalPaid = totalPaid,
                    remainingAmount = remainingAmount,
                    status = status
                )

                LoanActionButtons(
                    loan = loan,
                    remainingAmount = remainingAmount,
                    onCreatePayment = onCreatePayment,
                    onOpenPayments = onOpenPayments,
                    onEditLoan = onEditLoan
                )

                LoanCalendarSummaryCard(
                    loan = loan,
                    installments = installments,
                    nextInstallment = nextInstallment,
                    overdueCount = overdueInstallments.size,
                    paidCount = paidInstallments.size,
                    pendingCount = pendingInstallments.size
                )

                LoanMoneySummaryCard(
                    loan = loan,
                    totalPaid = totalPaid,
                    remainingAmount = remainingAmount
                )

                LoanInstallmentsPreviewCard(
                    installments = installments
                )

                LoanInfoCard(
                    title = "Descripción",
                    value = loan.description.ifBlank { "Sin descripción" }
                )
            }
        }
    }
}

@Composable
private fun LoanHeaderCard(
    loan: Loan,
    clientName: String,
    clientPhone: String,
    totalPaid: Double,
    remainingAmount: Double,
    status: AppStatus
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    Text(
                        text = clientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    if (clientPhone.isNotBlank()) {
                        Text(
                            text = clientPhone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }

                    Text(
                        text = "Inicio: ${formatDate(loan.startDateMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }

                StatusChip(status = status)
            }

            SummaryRow(
                label = "Saldo pendiente",
                value = formatMoney(remainingAmount),
                highlight = true
            )

            SummaryRow(
                label = "Pagado",
                value = formatMoney(totalPaid)
            )
        }
    }
}

@Composable
private fun LoanActionButtons(
    loan: Loan,
    remainingAmount: Double,
    onCreatePayment: () -> Unit,
    onOpenPayments: () -> Unit,
    onEditLoan: () -> Unit
) {
    val canReceivePayment = loan.status != LoanStatus.CANCELLED && remainingAmount > 0.0

    PrimaryButton(
        text = "Registrar pago",
        enabled = canReceivePayment,
        onClick = onCreatePayment
    )

    SecondaryButton(
        text = "Ver historial de pagos",
        onClick = onOpenPayments
    )

    SecondaryButton(
        text = "Editar préstamo",
        enabled = loan.status != LoanStatus.CANCELLED,
        onClick = onEditLoan
    )
}

@Composable
private fun LoanCalendarSummaryCard(
    loan: Loan,
    installments: List<Installment>,
    nextInstallment: Installment?,
    overdueCount: Int,
    paidCount: Int,
    pendingCount: Int
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Calendario del préstamo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            SummaryRow(
                label = "Fecha de inicio",
                value = formatDate(loan.startDateMillis)
            )

            SummaryRow(
                label = "Fecha de creación",
                value = formatDate(loan.createdAtMillis)
            )

            SummaryRow(
                label = "Total de cuotas",
                value = installments.size.toString()
            )

            SummaryRow(
                label = "Cuotas pagadas",
                value = paidCount.toString()
            )

            SummaryRow(
                label = "Cuotas pendientes",
                value = pendingCount.toString()
            )

            SummaryRow(
                label = "Cuotas vencidas",
                value = overdueCount.toString(),
                highlight = overdueCount > 0
            )

            SummaryRow(
                label = "Próxima cuota",
                value = nextInstallment?.let {
                    "${formatDate(it.dueDateMillis)} · ${formatMoney(it.pendingAmount)}"
                } ?: "Sin cuotas pendientes"
            )
        }
    }
}

@Composable
private fun LoanMoneySummaryCard(
    loan: Loan,
    totalPaid: Double,
    remainingAmount: Double
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Resumen financiero",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            SummaryRow(
                label = "Monto prestado",
                value = formatMoney(loan.principalAmount)
            )

            SummaryRow(
                label = "Interés",
                value = "${loan.interestRatePercent}%"
            )

            SummaryRow(
                label = "Total esperado",
                value = formatMoney(loan.totalExpectedAmount)
            )

            SummaryRow(
                label = "Total pagado",
                value = formatMoney(totalPaid)
            )

            SummaryRow(
                label = "Saldo pendiente",
                value = formatMoney(remainingAmount),
                highlight = true
            )

            SummaryRow(
                label = "Modalidad",
                value = loan.repaymentPlanType.label
            )

            SummaryRow(
                label = "Plazo",
                value = "${loan.termInDays} días"
            )

            SummaryRow(
                label = "Cuota estimada",
                value = formatMoney(loan.estimatedDailyAmount)
            )
        }
    }
}

@Composable
private fun LoanInstallmentsPreviewCard(
    installments: List<Installment>
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Próximas cuotas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            val visibleInstallments = installments
                .sortedBy { it.dueDateMillis }
                .take(8)

            if (visibleInstallments.isEmpty()) {
                Text(
                    text = "Este préstamo todavía no tiene cuotas generadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            } else {
                visibleInstallments.forEach { installment ->
                    InstallmentPreviewRow(
                        installment = installment
                    )
                }

                if (installments.size > visibleInstallments.size) {
                    Text(
                        text = "Mostrando ${visibleInstallments.size} de ${installments.size} cuotas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallmentPreviewRow(
    installment: Installment
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Cuota #${installment.number}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = formatDate(installment.dueDateMillis),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = formatMoney(installment.pendingAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = installment.status.label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun LoanInfoCard(
    title: String,
    value: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.Gray900
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) AppColors.AccentTeal else AppColors.Gray900
        )
    }
}

private fun getOrCreateInstallments(loan: Loan): List<Installment> {
    val currentInstallments = LocalInstallmentRepository.getInstallmentsByLoan(loan.id)

    return if (currentInstallments.isEmpty()) {
        LocalInstallmentRepository.generateInstallmentsForLoan(loan)
    } else {
        currentInstallments
    }
}

private fun resolveLoanStatus(
    loan: Loan,
    remainingAmount: Double,
    overdueCount: Int
): AppStatus {
    return when {
        loan.status == LoanStatus.CANCELLED -> AppStatus.INACTIVE
        loan.status == LoanStatus.PAID -> AppStatus.COMPLETED
        remainingAmount <= 0.0 -> AppStatus.COMPLETED
        overdueCount > 0 -> AppStatus.OVERDUE
        else -> AppStatus.ACTIVE
    }
}

private fun formatMoney(value: Double): String {
    return screenCurrencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}


