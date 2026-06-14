package com.controlprestamos.features.payments.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.documents.PdfShareUtils
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.core.rules.FinancialOperationRules
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
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }

    val payments = LocalPaymentRepository
        .getPaymentHistoryByLoan(loanId)
        .let { if (refreshVersion >= 0) it else it }

    val activePayments = payments.filter { FinancialOperationRules.shouldCountPaymentFinancially(it) }
    val cancelledPayments = payments.filter { !FinancialOperationRules.shouldCountPaymentFinancially(it) }

    val activePaid = activePayments.sumOf { it.amount }
    val cancelledAmount = cancelledPayments.sumOf { it.amount }

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

                if (!actionMessage.isNullOrBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = actionMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (actionMessage.orEmpty().contains("No se pudo")) {
                                AppColors.Error
                            } else {
                                AppColors.Success
                            }
                        )
                    }
                }

                PaymentHistoryFilterSelector(
                    selectedFilter = selectedFilter,
                    onSelected = {
                        selectedFilter = it
                        cancellingPaymentId = null
                        cancellationReason = ""
                        actionMessage = null
                    }
                )

                Text(
                    text = "Movimientos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
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
                                cancellationReason = it
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
                                if (cancellationReason.isBlank()) {
                                    actionMessage = "No se pudo anular: debes escribir un motivo."
                                    return@PaymentHistoryCard
                                }

                                val cancelled = LocalPaymentRepository.cancelPayment(
                                    paymentId = payment.id,
                                    reason = cancellationReason
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

            Text(
                text = formatMoney(remainingAmount, currencySymbol),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (remainingAmount > 0.0) AppColors.Warning else AppColors.Success
            )

            Text(
                text = if (remainingAmount > 0.0) {
                    "Saldo pendiente"
                } else {
                    "Préstamo saldado"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
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
                    title = "Pagado activo",
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
                    title = "Pagos activos",
                    value = activeCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                CompactPaymentStat(
                    title = "Anulados",
                    value = "$cancelledCount / ${formatMoney(cancelledAmount, currencySymbol)}",
                    modifier = Modifier.weight(1f),
                    danger = cancelledCount > 0
                )
            }
        }
    }
}

@Composable
private fun CompactPaymentStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    success: Boolean = false,
    danger: Boolean = false
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
                    success -> AppColors.Success
                    else -> AppColors.Gray900
                }
            )
        }
    }
}

@Composable
private fun PaymentHistoryFilterSelector(
    selectedFilter: PaymentHistoryFilter,
    onSelected: (PaymentHistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        PaymentHistoryFilter.entries.forEach { filter ->
            if (selectedFilter == filter) {
                PrimaryButton(
                    text = filter.label,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSelected(filter)
                    }
                )
            } else {
                SecondaryButton(
                    text = filter.label,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSelected(filter)
                    }
                )
            }
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
                Text(
                    text = if (isCancelled) "Pago anulado" else "Pago activo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) AppColors.Error else AppColors.Gray900
                )

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

            Text(
                text = "Fecha real: ${formatDate(payment.createdAtMillis, dateFormat)}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Método: ${payment.method.ifBlank { "No registrado" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Referencia: ${payment.reference.ifBlank { "Sin referencia" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Nota: ${payment.notes.ifBlank { "Sin nota" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            if (!isCancelled && balanceAfterPayment != null) {
                Text(
                    text = "Saldo luego del pago: ${formatMoney(balanceAfterPayment, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (balanceAfterPayment > 0.0) AppColors.Warning else AppColors.Success
                )
            }

            if (isCancelled) {
                Text(
                    text = "Motivo: ${payment.cancellationReason ?: "Sin motivo registrado"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Error
                )

                payment.cancelledAtMillis?.let { cancelledAt ->
                    Text(
                        text = "Anulado el: ${formatDate(cancelledAt, dateFormat)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }

                Text(
                    text = "Este registro queda en el historial, pero no suma en saldos ni reportes financieros.",
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
                        supportingText = "Este pago no se borrará: quedará como anulado en el historial."
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
    val safePattern = dateFormat.ifBlank { "dd/MM/yyyy" }

    return runCatching {
        SimpleDateFormat("$safePattern HH:mm", Locale.getDefault())
            .format(Date(millis))
    }.getOrElse {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(millis))
    }
}


