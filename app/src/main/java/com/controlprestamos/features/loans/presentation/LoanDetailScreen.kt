package com.controlprestamos.features.loans.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.ClientAvatar
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun LoanDetailScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onEditLoan: () -> Unit = {},
    onCreatePayment: () -> Unit = {},
    onOpenClient: (String) -> Unit = {},
    onOpenClientDetail: (String) -> Unit = onOpenClient,
    onOpenPaymentsByLoan: (String) -> Unit = {},
    onOpenPayments: () -> Unit = { onOpenPaymentsByLoan(loanId) },
    onCancelLoan: () -> Unit = {}
) {
    val loan = LocalLoanRepository.getLoanById(loanId)
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Préstamo",
                subtitle = loan?.description?.ifBlank { "Detalle financiero" } ?: "No encontrado",
                showBack = true,
                showNotifications = false,
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

                if (loan == null) {
                    LoanNotFoundCard(
                        onNavigateBack = onNavigateBack
                    )

                    return@Column
                }

                val state = buildLoanDetailState(
                    loan = loan,
                    currencySymbol = preferences.currencySymbol
                )

                LoanClientHeader(
                    state = state,
                    onOpenClient = { onOpenClientDetail(loan.clientId) }
                )

                LoanFinancialCard(
                    state = state
                )

                LoanDetailActions(
                    state = state,
                    onCreatePayment = onCreatePayment,
                    onEditLoan = onEditLoan,
                    onOpenPaymentsByLoan = onOpenPayments
                )

                LoanInstallmentsCard(
                    installments = state.installmentRows
                )

                LoanPaymentsCard(
                    payments = state.paymentRows
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun LoanNotFoundCard(
    onNavigateBack: () -> Unit
) {
    ReferenceCard {
        Text(
            text = "Préstamo no encontrado",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "No pudimos encontrar el préstamo solicitado.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        DetailActionButton(
            text = "Volver",
            color = AppColors.PrimaryDark,
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoanClientHeader(
    state: LoanDetailState,
    onOpenClient: () -> Unit
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientAvatar(
                fullName = state.clientName,
                size = 58.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = state.clientName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = state.loanLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }

            LoanStatusPill(
                text = state.statusText,
                color = state.statusColor
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            CompactInfoPill(
                title = "Inicio",
                value = state.startDateText,
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            CompactInfoPill(
                title = "Cuotas",
                value = state.termText,
                color = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )

            CompactInfoPill(
                title = "Diario",
                value = state.dailyText,
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        CompactGhostButton(
            text = "Ver cliente",
            color = AppColors.PrimaryDark,
            onClick = onOpenClient
        )
    }
}

@Composable
private fun LoanFinancialCard(
    state: LoanDetailState
) {
    ReferenceCard {
        Text(
            text = "Resumen financiero",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanMiniStat(
                title = "Capital",
                value = state.principalText,
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            LoanMiniStat(
                title = "Interés",
                value = state.interestText,
                color = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanMiniStat(
                title = "A recaudar",
                value = state.totalExpectedText,
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            LoanMiniStat(
                title = "Cobrado",
                value = state.totalPaidText,
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanMiniStat(
                title = "Pendiente",
                value = state.pendingText,
                color = if (state.pendingAmount > 0.0) AppColors.Warning else AppColors.Success,
                modifier = Modifier.weight(1f)
            )

            LoanMiniStat(
                title = "Vencidas",
                value = state.overdueCount.toString(),
                color = if (state.overdueCount > 0) AppColors.Error else AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Text(
            text = "Progreso de cobro",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray600
        )

        ProgressBar(progress = state.progress)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.progressText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )

            Text(
                text = state.nextInstallmentText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun LoanDetailActions(
    state: LoanDetailState,
    onCreatePayment: () -> Unit,
    onEditLoan: () -> Unit,
    onOpenPaymentsByLoan: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        if (state.canRegisterPayment) {
            DetailActionButton(
                text = "Registrar pago",
                color = AppColors.AccentTeal,
                onClick = onCreatePayment,
                modifier = Modifier.weight(1f)
            )
        }

        DetailActionButton(
            text = "Editar",
            color = AppColors.PrimaryDark,
            onClick = onEditLoan,
            modifier = Modifier.weight(1f)
        )

        DetailActionButton(
            text = "Pagos",
            color = AppColors.SecondaryDark,
            onClick = onOpenPaymentsByLoan,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LoanInstallmentsCard(
    installments: List<InstallmentRow>
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plan de cuotas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = installments.size.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (installments.isEmpty()) {
            Text(
                text = "No hay cuotas generadas para este préstamo.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                installments.take(10).forEach { installment ->
                    InstallmentReferenceRow(
                        installment = installment
                    )
                }
            }

            if (installments.size > 10) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                Text(
                    text = "Mostrando 10 de ${installments.size} cuotas.",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

@Composable
private fun InstallmentReferenceRow(
    installment: InstallmentRow
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppColors.SurfaceMuted)
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = installment.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = installment.dueDateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }

            LoanStatusPill(
                text = installment.statusText,
                color = installment.statusColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Esperado ${installment.expectedText}",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )

            Text(
                text = "Pendiente ${installment.pendingText}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (installment.pendingAmount > 0.0) AppColors.Warning else AppColors.Success
            )
        }

        ProgressBar(progress = installment.progress)
    }
}

@Composable
private fun LoanPaymentsCard(
    payments: List<PaymentRow>
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pagos recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = payments.size.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Success
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (payments.isEmpty()) {
            Text(
                text = "Aún no hay pagos registrados para este préstamo.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                payments.forEach { payment ->
                    PaymentReferenceRow(payment = payment)
                }
            }
        }
    }
}

@Composable
private fun PaymentReferenceRow(
    payment: PaymentRow
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppColors.SurfaceMuted)
            .padding(AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(AppColors.Success.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Success
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = payment.amountText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = payment.dateText,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )
        }

        Text(
            text = payment.methodText,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Gray600
        )
    }
}

@Composable
private fun CompactInfoPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 62.dp),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun LoanMiniStat(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 72.dp),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun LoanStatusPill(
    text: String,
    color: Color
) {
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
private fun CompactGhostButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .clickable { onClick() },
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.22f)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = AppSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun DetailActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable { onClick() },
        color = color,
        shape = RoundedCornerShape(AppRadius.card),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = AppSpacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .background(AppColors.Gray100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(AppColors.AccentTeal)
        )
    }
}

@Composable
private fun ReferenceCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Surface,
        shape = RoundedCornerShape(AppRadius.cardLarge),
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            content = content
        )
    }
}

private data class LoanDetailState(
    val loanId: String,
    val clientName: String,
    val loanLabel: String,
    val statusText: String,
    val statusColor: Color,
    val principalText: String,
    val interestText: String,
    val totalExpectedText: String,
    val totalPaidText: String,
    val pendingText: String,
    val pendingAmount: Double,
    val progress: Float,
    val progressText: String,
    val startDateText: String,
    val termText: String,
    val dailyText: String,
    val nextInstallmentText: String,
    val overdueCount: Int,
    val canRegisterPayment: Boolean,
    val installmentRows: List<InstallmentRow>,
    val paymentRows: List<PaymentRow>
)

private data class InstallmentRow(
    val title: String,
    val dueDateText: String,
    val expectedText: String,
    val pendingText: String,
    val pendingAmount: Double,
    val progress: Float,
    val statusText: String,
    val statusColor: Color
)

private data class PaymentRow(
    val amountText: String,
    val dateText: String,
    val methodText: String
)

private fun buildLoanDetailState(
    loan: Loan,
    currencySymbol: String
): LoanDetailState {
    val client = LocalClientRepository.getClientById(loan.clientId)
    val clientName = client?.fullName.orEmpty().ifBlank { "Cliente" }

    val activePayments = LocalPaymentRepository
        .getPaymentsByLoan(loan.id)
        .filter { payment -> payment.status == PaymentStatus.ACTIVE }

    val paid = activePayments.sumOf { payment -> payment.amount }
    val pending = max(loan.totalExpectedAmount - paid, 0.0)

    val progress = if (loan.totalExpectedAmount > 0.0) {
        (paid / loan.totalExpectedAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val installments = LocalInstallmentRepository
        .getInstallmentsByLoan(loan.id)
        .sortedWith(
            compareBy<Installment> { installment ->
                when (installment.status) {
                    InstallmentStatus.OVERDUE -> 0
                    InstallmentStatus.PARTIAL -> 1
                    InstallmentStatus.PENDING -> 2
                    InstallmentStatus.PAID -> 3
                    InstallmentStatus.CANCELLED -> 4
                }
            }.thenBy { installment -> installment.dueDateMillis }
        )

    val nextInstallment = installments.firstOrNull { installment ->
        installment.status == InstallmentStatus.PENDING ||
            installment.status == InstallmentStatus.PARTIAL ||
            installment.status == InstallmentStatus.OVERDUE
    }

    val overdueCount = installments.count { installment ->
        installment.status == InstallmentStatus.OVERDUE
    }

    val statusText = when (loan.status) {
        LoanStatus.ACTIVE -> if (overdueCount > 0) "Vencido" else "Activo"
        LoanStatus.PAID -> "Pagado"
        LoanStatus.CANCELLED -> "Cancelado"
    }

    val statusColor = when (loan.status) {
        LoanStatus.ACTIVE -> if (overdueCount > 0) AppColors.Error else AppColors.AccentTeal
        LoanStatus.PAID -> AppColors.Success
        LoanStatus.CANCELLED -> AppColors.Gray500
    }

    return LoanDetailState(
        loanId = loan.id,
        clientName = clientName,
        loanLabel = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
        statusText = statusText,
        statusColor = statusColor,
        principalText = formatMoney(loan.principalAmount, currencySymbol),
        interestText = formatPercent(loan.interestRatePercent),
        totalExpectedText = formatMoney(loan.totalExpectedAmount, currencySymbol),
        totalPaidText = formatMoney(paid, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        pendingAmount = pending,
        progress = progress,
        progressText = formatPercent(progress.toDouble() * 100.0),
        startDateText = formatDate(loan.startDateMillis),
        termText = "${loan.termInDays} días",
        dailyText = formatMoney(loan.estimatedDailyAmount, currencySymbol),
        nextInstallmentText = nextInstallment?.let { installment ->
            "Próxima: ${formatMoney(installment.pendingAmount, currencySymbol)} · ${formatDate(installment.dueDateMillis)}"
        } ?: "Sin cuota pendiente",
        overdueCount = overdueCount,
        canRegisterPayment = loan.status == LoanStatus.ACTIVE && pending > 0.0,
        installmentRows = installments.map { installment ->
            buildInstallmentRow(
                installment = installment,
                currencySymbol = currencySymbol
            )
        },
        paymentRows = activePayments
            .sortedByDescending { payment -> payment.createdAtMillis }
            .take(5)
            .map { payment ->
                buildPaymentRow(
                    payment = payment,
                    currencySymbol = currencySymbol
                )
            }
    )
}

private fun buildInstallmentRow(
    installment: Installment,
    currencySymbol: String
): InstallmentRow {
    val paid = installment.paidAmount
    val expected = installment.expectedAmount
    val pending = max(installment.pendingAmount, 0.0)

    val progress = if (expected > 0.0) {
        (paid / expected).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val statusText = when (installment.status) {
        InstallmentStatus.PENDING -> "Pendiente"
        InstallmentStatus.PARTIAL -> "Parcial"
        InstallmentStatus.PAID -> "Pagada"
        InstallmentStatus.OVERDUE -> "Vencida"
        InstallmentStatus.CANCELLED -> "Cancelada"
    }

    val statusColor = when (installment.status) {
        InstallmentStatus.PENDING -> AppColors.Warning
        InstallmentStatus.PARTIAL -> AppColors.AccentTeal
        InstallmentStatus.PAID -> AppColors.Success
        InstallmentStatus.OVERDUE -> AppColors.Error
        InstallmentStatus.CANCELLED -> AppColors.Gray500
    }

    return InstallmentRow(
        title = "Cuota ${installment.number}",
        dueDateText = formatDate(installment.dueDateMillis),
        expectedText = formatMoney(expected, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        pendingAmount = pending,
        progress = progress,
        statusText = statusText,
        statusColor = statusColor
    )
}

private fun buildPaymentRow(
    payment: Payment,
    currencySymbol: String
): PaymentRow {
    return PaymentRow(
        amountText = formatMoney(payment.amount, currencySymbol),
        dateText = formatDate(payment.createdAtMillis),
        methodText = payment.method.ifBlank { "Pago" }
    )
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0").format(value)
}

private fun formatPercent(value: Double): String {
    return DecimalFormat("#,##0.#").format(value) + "%"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

