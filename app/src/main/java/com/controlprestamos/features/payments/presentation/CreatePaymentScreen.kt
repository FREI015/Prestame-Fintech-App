package com.controlprestamos.features.payments.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
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
                subtitle = client?.fullName ?: "Centro de cobros",
                showBack = true,
                showMore = false,
                showMenu = false,
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

                if (client.status == ClientStatus.INACTIVE) {
                    EmptyState(
                        title = "Cliente archivado",
                        description = "No puedes registrar pagos operativos a un cliente archivado. Su historial se conserva para consulta.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                if (loan.status == LoanStatus.CANCELLED) {
                    EmptyState(
                        title = "Préstamo cancelado",
                        description = "No se pueden registrar pagos en un préstamo cancelado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                if (loan.status == LoanStatus.PAID || remainingAmount <= 0.0) {
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

                PaymentContextCard(
                    clientName = client.fullName.ifBlank { "Cliente" },
                    loanDescription = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
                    statusText = loan.status.name,
                    currencySymbol = preferences.currencySymbol,
                    totalExpected = loan.totalExpectedAmount,
                    totalPaid = totalPaid,
                    remainingAmount = remainingAmount
                )

                PaymentNextInstallmentCard(
                    installment = nextInstallment,
                    currencySymbol = preferences.currencySymbol,
                    onUseSuggestedAmount = {
                        nextInstallment?.let { installment ->
                            amount = formatAmountForInput(installment.pendingAmount)
                            formError = null
                        }
                    }
                )

                PaymentSuggestedAmountCard(
                    remainingAmount = remainingAmount,
                    nextInstallment = nextInstallment,
                    currencySymbol = preferences.currencySymbol,
                    onUseInstallment = {
                        nextInstallment?.let { installment ->
                            amount = formatAmountForInput(installment.pendingAmount)
                            formError = null
                        }
                    },
                    onUseFullBalance = {
                        amount = formatAmountForInput(remainingAmount)
                        formError = null
                    }
                )

                PaymentFormCard(
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
                    onPaymentDateChange = {
                        paymentDateMillis = it
                        formError = null
                    }
                )

                PaymentConfirmationCard(
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
                    text = "Registrar pago",
                    onClick = {
                        val input = CreatePaymentInput(
                            loanId = loan.id,
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

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun PaymentContextCard(
    clientName: String,
    loanDescription: String,
    statusText: String,
    currencySymbol: String,
    totalExpected: Double,
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
                text = "Resumen del cobro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = loanDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Estado: $statusText",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )

            Text(
                text = formatMoney(remainingAmount, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
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
                PaymentInfoBox(
                    title = "Total esperado",
                    value = formatMoney(totalExpected, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                PaymentInfoBox(
                    title = "Cobrado",
                    value = formatMoney(totalPaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    success = totalPaid > 0.0
                )
            }
        }
    }
}

@Composable
private fun PaymentNextInstallmentCard(
    installment: Installment?,
    currencySymbol: String,
    onUseSuggestedAmount: () -> Unit
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
                    text = "Cuota ${installment.number}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Vence: ${formatDate(installment.dueDateMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                Text(
                    text = "Pendiente: ${formatMoney(installment.pendingAmount, currencySymbol)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.status == InstallmentStatus.OVERDUE) {
                        AppColors.Error
                    } else {
                        AppColors.Warning
                    }
                )

                SecondaryButton(
                    text = "Usar monto de cuota",
                    onClick = onUseSuggestedAmount
                )
            }
        }
    }
}

@Composable
private fun PaymentSuggestedAmountCard(
    remainingAmount: Double,
    nextInstallment: Installment?,
    currencySymbol: String,
    onUseInstallment: () -> Unit,
    onUseFullBalance: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Monto sugerido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Puedes cobrar la próxima cuota o saldar el préstamo completo.",
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
                    onClick = onUseInstallment
                )

                SecondaryButton(
                    text = "Saldo ${formatMoney(remainingAmount, currencySymbol)}",
                    modifier = Modifier.weight(1f),
                    onClick = onUseFullBalance
                )
            }
        }
    }
}

@Composable
private fun PaymentFormCard(
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
                label = "Fecha real de pago",
                selectedDateMillis = paymentDateMillis,
                onDateSelected = onPaymentDateChange,
                helperText = "Esta fecha se usará en historial, cuotas, reportes y recibos."
            )

            AppTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = "Monto a registrar",
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
                    onClick = { onMethodChange("Efectivo") }
                )

                PaymentMethodButton(
                    text = "Transferencia",
                    selected = method == "Transferencia",
                    modifier = Modifier.weight(1f),
                    onClick = { onMethodChange("Transferencia") }
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
                    onClick = { onMethodChange("Pago móvil") }
                )

                PaymentMethodButton(
                    text = "Otro",
                    selected = method == "Otro",
                    modifier = Modifier.weight(1f),
                    onClick = { onMethodChange("Otro") }
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
                label = "Notas",
                singleLine = false
            )
        }
    }
}

@Composable
private fun PaymentConfirmationCard(
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Vas a registrar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = formatMoney(amount, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (amount > 0.0) AppColors.Success else AppColors.Gray500
            )

            Text(
                text = "Método: ${method.ifBlank { "Pago" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Fecha: ${formatDate(paymentDateMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Pendiente después del pago: ${formatMoney(remainingAfterPayment, currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (remainingAfterPayment <= 0.0) AppColors.Success else AppColors.Warning
            )
        }
    }
}

@Composable
private fun PaymentInfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    success: Boolean = false
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
                color = AppColors.Gray500
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (success) AppColors.Success else AppColors.Gray900
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

private fun parseAmount(value: String): Double? {
    return value
        .replace(",", ".")
        .trim()
        .toDoubleOrNull()
}

private fun formatAmountForInput(value: Double): String {
    return DecimalFormat("#.##").format(value)
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}
