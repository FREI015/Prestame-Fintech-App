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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppDatePickerField
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.CreatePaymentInput
import com.controlprestamos.features.payments.domain.validation.PaymentFormValidator
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun CreatePaymentScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onPaymentCreated: () -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val loan = LocalLoanRepository.getLoanById(loanId)
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }

    val totalPaid = LocalPaymentRepository.getTotalPaidByLoan(loanId)
    val remainingAmount = if (loan == null) {
        0.0
    } else {
        max(loan.totalExpectedAmount - totalPaid, 0.0)
    }

    val nextInstallment = LocalInstallmentRepository.getNextPendingInstallment(loanId)

    var amount by rememberSaveable {
        mutableStateOf("")
    }

    var method by rememberSaveable {
        mutableStateOf("Efectivo")
    }

    var reference by rememberSaveable {
        mutableStateOf("")
    }

    var notes by rememberSaveable {
        mutableStateOf("")
    }

    var formError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var paymentDateMillis by rememberSaveable {
        mutableStateOf(System.currentTimeMillis())
    }

    val amountValue = parseAmount(amount)
    val amountPreview = amountValue ?: 0.0
    val remainingAfterPayment = max(remainingAmount - amountPreview, 0.0)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Registrar pago",
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
                        description = "No pudimos encontrar el préstamo asociado al pago.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }
                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "El préstamo no tiene un cliente válido asociado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                if (!FinancialOperationRules.canShowClientInOperationalLists(client)) {
                    EmptyState(
                        title = "Cliente archivado",
                        description = FinancialOperationRules.getClientOperationMessage(client),
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }


                if (FinancialOperationRules.isClosedLoan(loan) && loan.status == LoanStatus.CANCELLED) {
                    EmptyState(
                        title = "Préstamo cancelado",
                        description = FinancialOperationRules.getLoanOperationMessage(loan),
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                if (FinancialOperationRules.isClosedLoan(loan) && loan.status == LoanStatus.PAID) {
                    EmptyState(
                        title = "Préstamo pagado",
                        description = FinancialOperationRules.getLoanOperationMessage(loan),
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }
                PaymentLoanSummaryCard(
                    clientName = client.fullName,
                    totalExpected = loan.totalExpectedAmount,
                    totalPaid = totalPaid,
                    remainingAmount = remainingAmount,
                    currencySymbol = preferences.currencySymbol
                )

                if (remainingAmount <= 0.0) {
                    EmptyState(
                        title = "Préstamo saldado",
                        description = "Este préstamo no tiene saldo pendiente.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                NextInstallmentCard(
                    installment = nextInstallment,
                    currencySymbol = preferences.currencySymbol,
                    onPayInstallment = {
                        nextInstallment?.let { installment ->
                            amount = formatAmountForInput(installment.pendingAmount)
                            formError = null
                        }
                    }
                )

                QuickAmountCard(
                    remainingAmount = remainingAmount,
                    nextInstallment = nextInstallment,
                    currencySymbol = preferences.currencySymbol,
                    onPayInstallment = {
                        nextInstallment?.let { installment ->
                            amount = formatAmountForInput(installment.pendingAmount)
                            formError = null
                        }
                    },
                    onPayFullBalance = {
                        amount = formatAmountForInput(remainingAmount)
                        formError = null
                    }
                )

                PaymentDataCard(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        formError = null
                    },
                    method = method,
                    onMethodChange = {
                        method = it
                        formError = null
                    },
                    reference = reference,
                    onReferenceChange = {
                        reference = it
                        formError = null
                    },
                    notes = notes,
                    onNotesChange = {
                        notes = it
                        formError = null
                    },
                    paymentDateMillis = paymentDateMillis,
                    onPaymentDateChange = { selected ->
                        paymentDateMillis = selected
                        formError = null
                    }
                )

                PaymentPreviewCard(
                    amount = amountPreview,
                    remainingAfterPayment = remainingAfterPayment,
                    method = method,
                    paymentDateMillis = paymentDateMillis,
                    currencySymbol = preferences.currencySymbol
                )

                if (!formError.isNullOrBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = formError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Error
                        )
                    }
                }

                PrimaryButton(
                    text = "Guardar pago",
                    onClick = {
                        val input = CreatePaymentInput(
                            loanId = loanId,
                            amount = amount,
                            method = method,
                            reference = reference,
                            notes = notes,
                            paymentDateMillis = paymentDateMillis
                        )

                        val validation = PaymentFormValidator.validate(
                            input = input,
                            remainingAmount = remainingAmount
                        )

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        LocalPaymentRepository.createPayment(input)

                        LocalInstallmentRepository.rebuildInstallmentsForLoan(
                            loanId = loan.id
                        )

                        onPaymentCreated()
                    }
                )

                SecondaryButton(
                    text = "Cancelar",
                    onClick = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun PaymentLoanSummaryCard(
    clientName: String,
    totalExpected: Double,
    totalPaid: Double,
    remainingAmount: Double,
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
                text = "Resumen del préstamo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = clientName,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = formatMoney(remainingAmount, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (remainingAmount > 0.0) AppColors.Warning else AppColors.Success
            )

            Text(
                text = "Saldo pendiente",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                CompactInfo(
                    title = "Total",
                    value = formatMoney(totalExpected, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                CompactInfo(
                    title = "Pagado",
                    value = formatMoney(totalPaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    success = totalPaid > 0.0
                )
            }
        }
    }
}

@Composable
private fun NextInstallmentCard(
    installment: Installment?,
    currencySymbol: String,
    onPayInstallment: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Próxima cuota",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            if (installment == null) {
                Text(
                    text = "No hay cuotas pendientes registradas para este préstamo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            } else {
                Text(
                    text = "Cuota #${installment.number}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Vence: ${formatDateOnly(installment.dueDateMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                Text(
                    text = "Pendiente: ${formatMoney(installment.pendingAmount, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Warning
                )

                SecondaryButton(
                    text = "Pagar próxima cuota",
                    onClick = onPayInstallment
                )
            }
        }
    }
}

@Composable
private fun QuickAmountCard(
    remainingAmount: Double,
    nextInstallment: Installment?,
    currencySymbol: String,
    onPayInstallment: () -> Unit,
    onPayFullBalance: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Monto rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Usa una opción rápida o escribe el monto manualmente.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SecondaryButton(
                    text = if (nextInstallment == null) {
                        "Cuota"
                    } else {
                        "Cuota ${formatMoney(nextInstallment.pendingAmount, currencySymbol)}"
                    },
                    modifier = Modifier.weight(1f),
                    onClick = onPayInstallment
                )

                SecondaryButton(
                    text = "Saldo ${formatMoney(remainingAmount, currencySymbol)}",
                    modifier = Modifier.weight(1f),
                    onClick = onPayFullBalance
                )
            }
        }
    }
}

@Composable
private fun PaymentDataCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    method: String,
    onMethodChange: (String) -> Unit,
    reference: String,
    onReferenceChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    paymentDateMillis: Long,
    onPaymentDateChange: (Long) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Datos del pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            AppDatePickerField(
                label = "Fecha del pago",
                selectedDateMillis = paymentDateMillis,
                onDateSelected = onPaymentDateChange,
                helperText = "Esta fecha saldrá en recibo, historial, cuotas y reportes."
            )

            AppTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = "Monto pagado",
                keyboardType = KeyboardType.Number
            )

            Text(
                text = "Método de pago",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PaymentMethodButton(
                    text = "Efectivo",
                    selected = method == "Efectivo",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onMethodChange("Efectivo")
                    }
                )

                PaymentMethodButton(
                    text = "Transferencia",
                    selected = method == "Transferencia",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onMethodChange("Transferencia")
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PaymentMethodButton(
                    text = "Pago móvil",
                    selected = method == "Pago móvil",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onMethodChange("Pago móvil")
                    }
                )

                PaymentMethodButton(
                    text = "Otro",
                    selected = method == "Otro",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onMethodChange("Otro")
                    }
                )
            }

            AppTextField(
                value = method,
                onValueChange = onMethodChange,
                label = "Método seleccionado"
            )

            AppTextField(
                value = reference,
                onValueChange = onReferenceChange,
                label = "Referencia / comprobante"
            )

            AppTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = "Nota"
            )
        }
    }
}

@Composable
private fun PaymentPreviewCard(
    amount: Double,
    remainingAfterPayment: Double,
    method: String,
    paymentDateMillis: Long,
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
                text = "Vista previa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            PreviewRow(
                label = "Monto",
                value = formatMoney(amount, currencySymbol)
            )

            PreviewRow(
                label = "Método",
                value = method.ifBlank { "Sin método" }
            )

            PreviewRow(
                label = "Fecha",
                value = formatDateOnly(paymentDateMillis)
            )

            PreviewRow(
                label = "Saldo luego del pago",
                value = formatMoney(remainingAfterPayment, currencySymbol),
                warning = remainingAfterPayment > 0.0,
                success = remainingAfterPayment <= 0.0
            )
        }
    }
}

@Composable
private fun PaymentMethodButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        PrimaryButton(
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    } else {
        SecondaryButton(
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    }
}

@Composable
private fun CompactInfo(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
    success: Boolean = false
) {
    val valueColor = when {
        success -> AppColors.Success
        warning -> AppColors.Warning
        else -> AppColors.Gray900
    }

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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String,
    warning: Boolean = false,
    success: Boolean = false
) {
    val valueColor = when {
        success -> AppColors.Success
        warning -> AppColors.Warning
        else -> AppColors.Gray900
    }

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
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun parseAmount(value: String): Double? {
    return value
        .replace(",", ".")
        .trim()
        .toDoubleOrNull()
}

private fun formatAmountForInput(value: Double): String {
    return DecimalFormat("0.##").format(value)
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDateOnly(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}






