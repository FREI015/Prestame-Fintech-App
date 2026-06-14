package com.controlprestamos.features.clients.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun ClientDetailScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onEditClient: () -> Unit = {},
    onCreateLoan: () -> Unit = {},
    onLoanClick: (String) -> Unit = {},
    onOpenLoanDetail: (String) -> Unit = {},
    onCreatePayment: () -> Unit = {}
) {
    val client = LocalClientRepository.getClientById(clientId)
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Cliente",
                subtitle = client?.fullName ?: "No encontrado",
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

                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "No pudimos encontrar el cliente solicitado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                val state = buildClientDetailState(
                    clientId = client.id,
                    currencySymbol = preferences.currencySymbol
                )

                ClientProfileHeader(
                    fullName = client.fullName,
                    status = client.status,
                    documentId = client.documentId,
                    phone = client.phone,
                    address = client.address,
                    notes = client.notes,
                    onEditClient = onEditClient
                )

                ClientFinancialSummary(
                    state = state
                )

                ClientDetailActions(
                    onCreateLoan = onCreateLoan,
                    onCreatePayment = onCreatePayment,
                    onEditClient = onEditClient
                )

                ClientLoansCard(
                    loans = state.loanRows,
                    onLoanClick = { loanId ->
                        onOpenLoanDetail(loanId)
                        onLoanClick(loanId)
                    }
                )

                ClientPaymentsCard(
                    payments = state.paymentRows
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun ClientProfileHeader(
    fullName: String,
    status: ClientStatus,
    documentId: String,
    phone: String,
    address: String,
    notes: String,
    onEditClient: () -> Unit
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientAvatar(
                fullName = fullName,
                size = 58.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = fullName.ifBlank { "Cliente" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                ClientStatusPill(
                    active = status == ClientStatus.ACTIVE
                )
            }

            CompactIconButton(
                text = "Editar",
                color = AppColors.PrimaryDark,
                onClick = onEditClient
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            InfoLine(
                label = "Documento",
                value = documentId.ifBlank { "Sin documento" }
            )

            InfoLine(
                label = "Teléfono",
                value = phone.ifBlank { "Sin teléfono" }
            )

            InfoLine(
                label = "Dirección",
                value = address.ifBlank { "Sin dirección" }
            )

            if (notes.isNotBlank()) {
                InfoLine(
                    label = "Notas",
                    value = notes
                )
            }
        }
    }
}

@Composable
private fun ClientFinancialSummary(
    state: ClientDetailState
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
            ClientDetailMiniStat(
                title = "Prestado",
                value = state.totalLoanedText,
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            ClientDetailMiniStat(
                title = "A recaudar",
                value = state.totalExpectedText,
                color = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientDetailMiniStat(
                title = "Cobrado",
                value = state.totalPaidText,
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )

            ClientDetailMiniStat(
                title = "Pendiente",
                value = state.totalPendingText,
                color = if (state.totalPending > 0.0) AppColors.Warning else AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Text(
            text = "Progreso general",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray600
        )

        ProgressBar(
            progress = state.progress
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.activeLoans} activos",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )

            Text(
                text = state.progressText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }
    }
}

@Composable
private fun ClientDetailActions(
    onCreateLoan: () -> Unit,
    onCreatePayment: () -> Unit,
    onEditClient: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        DetailActionButton(
            text = "Nuevo préstamo",
            color = AppColors.AccentTeal,
            onClick = onCreateLoan,
            modifier = Modifier.weight(1f)
        )

        DetailActionButton(
            text = "Registrar pago",
            color = AppColors.PrimaryDark,
            onClick = onCreatePayment,
            modifier = Modifier.weight(1f)
        )

        DetailActionButton(
            text = "Editar",
            color = AppColors.SecondaryDark,
            onClick = onEditClient,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClientLoansCard(
    loans: List<ClientLoanRow>,
    onLoanClick: (String) -> Unit
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Préstamos del cliente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = loans.size.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (loans.isEmpty()) {
            Text(
                text = "Este cliente no tiene préstamos registrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                loans.forEach { loan ->
                    LoanReferenceRow(
                        loan = loan,
                        onClick = { onLoanClick(loan.loanId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanReferenceRow(
    loan: ClientLoanRow,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable { onClick() }
            .background(AppColors.SurfaceMuted)
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = loan.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = loan.statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = loan.statusColor
                )
            }

            Text(
                text = loan.pendingText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (loan.pending > 0.0) AppColors.Warning else AppColors.Success
            )
        }

        Text(
            text = "Prestado ${loan.principalText} · Total ${loan.totalText}",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray600
        )

        ProgressBar(
            progress = loan.progress
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cobrado ${loan.paidText}",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )

            Text(
                text = loan.progressText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }
    }
}

@Composable
private fun ClientPaymentsCard(
    payments: List<ClientPaymentRow>
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
                text = "Aún no hay pagos registrados para este cliente.",
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
    payment: ClientPaymentRow
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
private fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray500
        )

        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Gray900
        )
    }
}

@Composable
private fun ClientDetailMiniStat(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
private fun ClientStatusPill(
    active: Boolean
) {
    val color = if (active) AppColors.Success else AppColors.Gray500
    val text = if (active) "Activo" else "Archivado"

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
private fun CompactIconButton(
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

private data class ClientDetailState(
    val totalLoanedText: String,
    val totalExpectedText: String,
    val totalPaidText: String,
    val totalPendingText: String,
    val totalPending: Double,
    val progress: Float,
    val progressText: String,
    val activeLoans: Int,
    val loanRows: List<ClientLoanRow>,
    val paymentRows: List<ClientPaymentRow>
)

private data class ClientLoanRow(
    val loanId: String,
    val label: String,
    val principalText: String,
    val totalText: String,
    val paidText: String,
    val pendingText: String,
    val pending: Double,
    val progress: Float,
    val progressText: String,
    val statusText: String,
    val statusColor: Color
)

private data class ClientPaymentRow(
    val amountText: String,
    val dateText: String,
    val methodText: String
)

private fun buildClientDetailState(
    clientId: String,
    currencySymbol: String
): ClientDetailState {
    val loans = LocalLoanRepository
        .getLoansByClient(clientId)
        .filter { loan -> loan.status != LoanStatus.CANCELLED }

    val totalLoaned = loans.sumOf { loan -> loan.principalAmount }
    val totalExpected = loans.sumOf { loan -> loan.totalExpectedAmount }
    val totalPaid = loans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }
    val totalPending = max(totalExpected - totalPaid, 0.0)

    val progress = if (totalExpected > 0.0) {
        (totalPaid / totalExpected).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    return ClientDetailState(
        totalLoanedText = formatMoney(totalLoaned, currencySymbol),
        totalExpectedText = formatMoney(totalExpected, currencySymbol),
        totalPaidText = formatMoney(totalPaid, currencySymbol),
        totalPendingText = formatMoney(totalPending, currencySymbol),
        totalPending = totalPending,
        progress = progress,
        progressText = formatPercent(progress.toDouble() * 100.0),
        activeLoans = loans.count { loan -> loan.status == LoanStatus.ACTIVE },
        loanRows = loans.map { loan ->
            buildLoanRow(
                loan = loan,
                currencySymbol = currencySymbol
            )
        },
        paymentRows = LocalPaymentRepository
            .getPaymentsByClient(clientId)
            .take(5)
            .map { payment ->
                buildPaymentRow(
                    payment = payment,
                    currencySymbol = currencySymbol
                )
            }
    )
}

private fun buildLoanRow(
    loan: Loan,
    currencySymbol: String
): ClientLoanRow {
    val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    val pending = max(loan.totalExpectedAmount - paid, 0.0)

    val progress = if (loan.totalExpectedAmount > 0.0) {
        (paid / loan.totalExpectedAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val statusText = when (loan.status) {
        LoanStatus.ACTIVE -> "Activo"
        LoanStatus.PAID -> "Pagado"
        LoanStatus.CANCELLED -> "Cancelado"
    }

    val statusColor = when (loan.status) {
        LoanStatus.ACTIVE -> AppColors.AccentTeal
        LoanStatus.PAID -> AppColors.Success
        LoanStatus.CANCELLED -> AppColors.Gray500
    }

    return ClientLoanRow(
        loanId = loan.id,
        label = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
        principalText = formatMoney(loan.principalAmount, currencySymbol),
        totalText = formatMoney(loan.totalExpectedAmount, currencySymbol),
        paidText = formatMoney(paid, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        pending = pending,
        progress = progress,
        progressText = formatPercent(progress.toDouble() * 100.0),
        statusText = statusText,
        statusColor = statusColor
    )
}

private fun buildPaymentRow(
    payment: Payment,
    currencySymbol: String
): ClientPaymentRow {
    return ClientPaymentRow(
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
