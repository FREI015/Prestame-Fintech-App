package com.controlprestamos.features.payments.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.documents.PdfShareUtils
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.receipt.PaymentReceiptFormatter
import com.controlprestamos.features.payments.domain.receipt.PaymentReceiptPdfGenerator
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private enum class PaymentHistoryFilter(
    val label: String
) {
    ALL("Todos"),
    ACTIVE("Activos"),
    CANCELLED("Anulados")
}

@Composable
fun PaymentsByLoanScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onCreatePayment: () -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    var refreshVersion by rememberSaveable {
        mutableIntStateOf(0)
    }

    var selectedFilter by rememberSaveable {
        mutableStateOf(PaymentHistoryFilter.ALL)
    }

    var cancellingPaymentId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var cancellationReason by rememberSaveable {
        mutableStateOf("")
    }

    var actionMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val loan = LocalLoanRepository.getLoanById(loanId)
    val client = loan?.let { currentLoan ->
        LocalClientRepository.getClientById(currentLoan.clientId)
    }

    val payments = LocalPaymentRepository
        .getPaymentHistoryByLoan(loanId)
        .let { if (refreshVersion >= 0) it else it }

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

    val remainingAmount = if (loan == null) {
        0.0
    } else {
        max(loan.totalExpectedAmount - activePaid, 0.0)
    }

    val balanceAfterByPaymentId = if (loan == null) {
        emptyMap()
    } else {
        buildBalanceAfterByPaymentId(
            loanTotal = loan.totalExpectedAmount,
            payments = payments
        )
    }

    val visiblePayments = when (selectedFilter) {
        PaymentHistoryFilter.ALL -> payments
        PaymentHistoryFilter.ACTIVE -> activePayments
        PaymentHistoryFilter.CANCELLED -> cancelledPayments
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Historial de pagos",
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
                Spacer(modifier = Modifier.height(AppSpacing.xs))

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

                PaymentLoanHeroCard(
                    clientName = client?.fullName ?: "Cliente",
                    loanName = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
                    remainingAmount = remainingAmount,
                    currencySymbol = preferences.currencySymbol
                )

                PaymentHistorySummaryCard(
                    totalExpected = loan.totalExpectedAmount,
                    activePaid = activePaid,
                    remainingAmount = remainingAmount,
                    activeCount = activePayments.size,
                    cancelledCount = cancelledPayments.size,
                    cancelledAmount = cancelledAmount,
                    currencySymbol = preferences.currencySymbol
                )

                if (remainingAmount > 0.0) {
                    PrimaryButton(
                        text = "Registrar pago",
                        onClick = onCreatePayment
                    )
                }

                actionMessage?.let { message ->
                    if (message.isNotBlank()) {
                        PaymentActionMessageCard(message = message)
                    }
                }

                PaymentHistoryFilterSelector(
                    selectedFilter = selectedFilter,
                    allCount = payments.size,
                    activeCount = activePayments.size,
                    cancelledCount = cancelledPayments.size,
                    onSelected = { filter ->
                        selectedFilter = filter
                        cancellingPaymentId = null
                        cancellationReason = ""
                        actionMessage = null
                    }
                )

                SectionTitle(
                    title = "Movimientos",
                    subtitle = when (selectedFilter) {
                        PaymentHistoryFilter.ALL -> "Todos los pagos registrados del préstamo."
                        PaymentHistoryFilter.ACTIVE -> "Pagos que sí cuentan en saldo y reportes."
                        PaymentHistoryFilter.CANCELLED -> "Pagos anulados conservados como auditoría."
                    }
                )

                if (visiblePayments.isEmpty()) {
                    EmptyState(
                        title = "Sin movimientos",
                        description = when (selectedFilter) {
                            PaymentHistoryFilter.ALL -> "Este préstamo todavía no tiene pagos registrados."
                            PaymentHistoryFilter.ACTIVE -> "Este préstamo no tiene pagos activos."
                            PaymentHistoryFilter.CANCELLED -> "Este préstamo no tiene pagos anulados."
                        }
                    )
                } else {
                    visiblePayments.forEach { payment ->
                        PaymentHistoryCard(
                            payment = payment,
                            balanceAfterPayment = balanceAfterByPaymentId[payment.id],
                            currencySymbol = preferences.currencySymbol,
                            dateFormat = preferences.dateFormat,
                            isCancelling = cancellingPaymentId == payment.id,
                            cancellationReason = cancellationReason,
                            onChangeCancellationReason = {
                                cancellationReason = it.take(180)
                                actionMessage = null
                            },
                            onShareReceiptText = {
                                val receipt = PaymentReceiptFormatter.buildPaymentReceipt(
                                    payment = payment,
                                    loan = loan,
                                    client = client,
                                    businessName = preferences.businessName,
                                    currencySymbol = preferences.currencySymbol
                                )

                                sharePlainText(
                                    context = context,
                                    title = "Recibo de pago",
                                    text = receipt
                                )
                            },
                            onShareReceiptPdf = {
                                val file = PaymentReceiptPdfGenerator.generate(
                                    context = context,
                                    client = client,
                                    loan = loan,
                                    payment = payment,
                                    businessName = preferences.businessName,
                                    currencySymbol = preferences.currencySymbol
                                )

                                PdfShareUtils.sharePdf(
                                    context = context,
                                    file = file,
                                    chooserTitle = "Compartir recibo PDF"
                                )
                            },
                            onStartCancel = {
                                cancellingPaymentId = payment.id
                                cancellationReason = ""
                                actionMessage = null
                            },
                            onCancelDismiss = {
                                cancellingPaymentId = null
                                cancellationReason = ""
                            },
                            onConfirmCancel = {
                                val cleanReason = cancellationReason.trim()

                                if (cleanReason.isBlank()) {
                                    actionMessage = "No se pudo anular: debes escribir un motivo."
                                    return@PaymentHistoryCard
                                }

                                val cancelled = LocalPaymentRepository.cancelPayment(
                                    paymentId = payment.id,
                                    reason = cleanReason
                                )

                                if (cancelled) {
                                    LocalInstallmentRepository.rebuildInstallmentsForLoan(
                                        loanId = payment.loanId
                                    )

                                    refreshVersion++
                                    cancellingPaymentId = null
                                    cancellationReason = ""
                                    actionMessage = "Pago anulado correctamente. Las cuotas, saldo y estado fueron recalculados."
                                } else {
                                    actionMessage = "No se pudo anular el pago."
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun PaymentLoanHeroCard(
    clientName: String,
    loanName: String,
    remainingAmount: Double,
    currencySymbol: String
) {
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
                        .background(AppColors.AccentTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧾",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = clientName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = loanName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (remainingAmount > 0.0) {
                    AppColors.Warning.copy(alpha = 0.08f)
                } else {
                    AppColors.Success.copy(alpha = 0.08f)
                },
                shape = RoundedCornerShape(AppRadius.card),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (remainingAmount > 0.0) {
                        AppColors.Warning.copy(alpha = 0.25f)
                    } else {
                        AppColors.Success.copy(alpha = 0.25f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (remainingAmount > 0.0) "Saldo pendiente" else "Estado del préstamo",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray600
                    )

                    Text(
                        text = if (remainingAmount > 0.0) {
                            formatMoney(remainingAmount, currencySymbol)
                        } else {
                            "Préstamo saldado"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingAmount > 0.0) AppColors.Warning else AppColors.Success
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentHistorySummaryCard(
    totalExpected: Double,
    activePaid: Double,
    remainingAmount: Double,
    activeCount: Int,
    cancelledCount: Int,
    cancelledAmount: Double,
    currencySymbol: String
) {
    val progress = if (totalExpected > 0.0) {
        (activePaid / totalExpected).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Resumen financiero",
                subtitle = "Pagos activos, saldo pendiente y auditoría de anulaciones."
            )

            PaymentProgressBar(
                progress = progress,
                color = if (remainingAmount > 0.0) AppColors.AccentTeal else AppColors.Success
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                CompactPaymentStat(
                    title = "Total",
                    value = formatMoney(totalExpected, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                CompactPaymentStat(
                    title = "Pagado",
                    value = formatMoney(activePaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    success = activePaid > 0.0
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                CompactPaymentStat(
                    title = "Pendiente",
                    value = formatMoney(remainingAmount, currencySymbol),
                    modifier = Modifier.weight(1f),
                    warning = remainingAmount > 0.0,
                    success = remainingAmount <= 0.0
                )

                CompactPaymentStat(
                    title = "Activos",
                    value = activeCount.toString(),
                    modifier = Modifier.weight(1f),
                    success = activeCount > 0
                )
            }

            CompactPaymentStat(
                title = "Pagos anulados",
                value = "$cancelledCount · ${formatMoney(cancelledAmount, currencySymbol)}",
                modifier = Modifier.fillMaxWidth(),
                danger = cancelledCount > 0
            )
        }
    }
}

@Composable
private fun CompactPaymentStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    success: Boolean = false,
    warning: Boolean = false,
    danger: Boolean = false
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
        modifier = modifier.heightIn(min = 70.dp),
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
private fun PaymentActionMessageCard(
    message: String
) {
    val isError = message.contains("No se pudo", ignoreCase = true)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) AppColors.Error else AppColors.Success
        )
    }
}

@Composable
private fun PaymentHistoryFilterSelector(
    selectedFilter: PaymentHistoryFilter,
    allCount: Int,
    activeCount: Int,
    cancelledCount: Int,
    onSelected: (PaymentHistoryFilter) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Filtrar movimientos",
                subtitle = "Separa pagos activos de pagos anulados para revisar mejor el historial."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PaymentHistoryFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        PaymentHistoryFilter.ALL -> allCount
                        PaymentHistoryFilter.ACTIVE -> activeCount
                        PaymentHistoryFilter.CANCELLED -> cancelledCount
                    }

                    PaymentFilterPill(
                        modifier = Modifier.weight(1f),
                        text = filter.label,
                        count = count,
                        selected = selectedFilter == filter,
                        onClick = {
                            onSelected(filter)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentFilterPill(
    modifier: Modifier = Modifier,
    text: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) AppColors.AccentTeal else AppColors.Gray500

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .clickable {
                onClick()
            },
        color = if (selected) color.copy(alpha = 0.12f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.42f) else AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun PaymentHistoryCard(
    payment: Payment,
    balanceAfterPayment: Double?,
    currencySymbol: String,
    dateFormat: String,
    isCancelling: Boolean,
    cancellationReason: String,
    onChangeCancellationReason: (String) -> Unit,
    onShareReceiptText: () -> Unit,
    onShareReceiptPdf: () -> Unit,
    onStartCancel: () -> Unit,
    onCancelDismiss: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    val isCancelled = !FinancialOperationRules.shouldCountPaymentFinancially(payment)
    val statusColor = if (isCancelled) AppColors.Error else AppColors.Success

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
                        text = if (isCancelled) "!" else "✓",
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
                        text = if (isCancelled) "Pago anulado" else "Pago activo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) AppColors.Error else AppColors.Gray900
                    )

                    Text(
                        text = formatDate(payment.createdAtMillis, dateFormat),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray500
                    )
                }

                if (isCancelled) {
                    Text(
                        text = "Anulado",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Error
                    )
                } else {
                    StatusChip(status = AppStatus.ACTIVE)
                }
            }

            Text(
                text = formatMoney(payment.amount, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCancelled) AppColors.Gray600 else AppColors.AccentTeal
            )

            PaymentInfoLine(
                label = "Método",
                value = payment.method.ifBlank { "No registrado" }
            )

            PaymentInfoLine(
                label = "Referencia",
                value = payment.reference.ifBlank { "Sin referencia" }
            )

            PaymentInfoLine(
                label = "Nota",
                value = payment.notes.ifBlank { "Sin nota" }
            )

            if (!isCancelled && balanceAfterPayment != null) {
                PaymentInfoLine(
                    label = "Saldo luego del pago",
                    value = formatMoney(balanceAfterPayment, currencySymbol),
                    valueColor = if (balanceAfterPayment > 0.0) AppColors.Warning else AppColors.Success
                )
            }

            if (isCancelled) {
                PaymentInfoLine(
                    label = "Motivo",
                    value = payment.cancellationReason ?: "Sin motivo registrado",
                    valueColor = AppColors.Error
                )

                payment.cancelledAtMillis?.let { cancelledAt ->
                    PaymentInfoLine(
                        label = "Anulado el",
                        value = formatDate(cancelledAt, dateFormat)
                    )
                }

                Text(
                    text = "Este registro queda guardado como auditoría, pero no suma en saldos ni reportes financieros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }

            if (!isCancelled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    SecondaryButton(
                        text = "Texto",
                        modifier = Modifier.weight(1f),
                        onClick = onShareReceiptText
                    )

                    SecondaryButton(
                        text = "PDF",
                        modifier = Modifier.weight(1f),
                        onClick = onShareReceiptPdf
                    )
                }

                if (isCancelling) {
                    AppTextField(
                        value = cancellationReason,
                        onValueChange = onChangeCancellationReason,
                        label = "Motivo de anulación",
                        singleLine = false,
                        supportingText = "Este pago no se borrará: quedará anulado en el historial."
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        SecondaryButton(
                            text = "Cancelar",
                            modifier = Modifier.weight(1f),
                            onClick = onCancelDismiss
                        )

                        PrimaryButton(
                            text = "Confirmar",
                            modifier = Modifier.weight(1f),
                            onClick = onConfirmCancel
                        )
                    }
                } else {
                    SecondaryButton(
                        text = "Anular pago",
                        onClick = onStartCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentInfoLine(
    label: String,
    value: String,
    valueColor: Color = AppColors.Gray900
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
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

@Composable
private fun PaymentProgressBar(
    progress: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .background(AppColors.Gray100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(9.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(color)
        )
    }
}

private fun buildBalanceAfterByPaymentId(
    loanTotal: Double,
    payments: List<Payment>
): Map<String, Double> {
    var activePaid = 0.0
    val result = mutableMapOf<String, Double>()

    payments
        .sortedWith(
            compareBy(
                { payment -> payment.createdAtMillis },
                { payment -> payment.id }
            )
        )
        .forEach { payment ->
            if (FinancialOperationRules.shouldCountPaymentFinancially(payment)) {
                activePaid += payment.amount
            }

            result[payment.id] = max(loanTotal - activePaid, 0.0)
        }

    return result
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(
    millis: Long,
    dateFormat: String
): String {
    val safePattern = when (dateFormat) {
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd" -> dateFormat
        else -> "dd/MM/yyyy"
    }

    return runCatching {
        SimpleDateFormat("$safePattern HH:mm", Locale.getDefault())
            .format(Date(millis))
    }.getOrElse {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(millis))
    }
}

