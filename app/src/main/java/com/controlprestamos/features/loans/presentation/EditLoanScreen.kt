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
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.model.UpdateLoanInput
import com.controlprestamos.features.loans.domain.validation.LoanFormValidator
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditLoanScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onLoanUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val loan = LocalLoanRepository.getLoanById(loanId)
    val paymentHistory = LocalPaymentRepository.getPaymentHistoryByLoan(loanId)
    val totalPaid = LocalPaymentRepository.getTotalPaidByLoan(loanId)

    var principalAmount by rememberSaveable(loanId) {
        mutableStateOf(loan?.principalAmount?.toString().orEmpty())
    }

    var interestRatePercent by rememberSaveable(loanId) {
        mutableStateOf(loan?.interestRatePercent?.toString().orEmpty())
    }

    var termInDays by rememberSaveable(loanId) {
        mutableStateOf(loan?.termInDays?.toString().orEmpty())
    }

    var description by rememberSaveable(loanId) {
        mutableStateOf(loan?.description.orEmpty())
    }

    var repaymentPlanTypeName by rememberSaveable(loanId) {
        mutableStateOf(loan?.repaymentPlanType?.name ?: RepaymentPlanType.INSTALLMENTS.name)
    }

    var formError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var actionMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var startDateMillis by rememberSaveable(loanId) {
        mutableStateOf(loan?.startDateMillis ?: loan?.createdAtMillis ?: System.currentTimeMillis())
    }

    val repaymentPlanType = runCatching {
        RepaymentPlanType.valueOf(repaymentPlanTypeName)
    }.getOrDefault(RepaymentPlanType.INSTALLMENTS)

    val principal = principalAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
    val interest = interestRatePercent.replace(",", ".").toDoubleOrNull() ?: 0.0
    val term = termInDays.toIntOrNull() ?: 0
    val safeTerm = if (term > 0) term else 0
    val totalExpected = principal + (principal * interest / 100.0)
    val installmentPreviewAmount = when (repaymentPlanType) {
        RepaymentPlanType.SINGLE_PAYMENT -> totalExpected
        RepaymentPlanType.INSTALLMENTS -> if (safeTerm > 0) totalExpected / safeTerm else 0.0
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar préstamo",
                subtitle = when {
                    loan == null -> "Préstamo no encontrado"
                    paymentHistory.isNotEmpty() -> "Modo protegido"
                    loan.status == LoanStatus.PAID -> "Préstamo pagado"
                    else -> "Sin movimientos financieros"
                },
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

                if (FinancialOperationRules.isClosedLoan(loan) && loan.status == LoanStatus.CANCELLED) {
                    EmptyState(
                        title = "Préstamo cancelado",
                        description = "No puedes editar un préstamo cancelado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                val isFinanciallyProtected = paymentHistory.isNotEmpty() || loan.status == LoanStatus.PAID

                if (isFinanciallyProtected) {
                    ProtectedLoanEditContent(
                        loan = loan,
                        description = description,
                        totalPaid = totalPaid,
                        paymentHistoryCount = paymentHistory.size,
                        currencySymbol = preferences.currencySymbol,
                        onDescriptionChange = {
                            description = it
                            formError = null
                            actionMessage = null
                        },
                        formError = formError,
                        actionMessage = actionMessage,
                        onSaveDescription = {
                            val updatedLoan = LocalLoanRepository.updateLoanDescription(
                                loanId = loan.id,
                                description = description
                            )

                            if (updatedLoan == null) {
                                formError = "No se pudo actualizar la nota del préstamo."
                                return@ProtectedLoanEditContent
                            }

                            formError = null
                            actionMessage = "Nota actualizada. Los datos financieros no fueron modificados."
                            onLoanUpdated(updatedLoan.id)
                        },
                        onNavigateBack = onNavigateBack
                    )

                    return@Column
                }

                EditableLoanFinancialForm(
                    principalAmount = principalAmount,
                    onPrincipalChange = {
                        principalAmount = it.replace(",", ".")
                        formError = null
                        actionMessage = null
                    },
                    interestRatePercent = interestRatePercent,
                    onInterestChange = {
                        interestRatePercent = it.replace(",", ".")
                        formError = null
                        actionMessage = null
                    },
                    termInDays = termInDays,
                    onTermChange = {
                        termInDays = it.filter { char -> char.isDigit() }
                        formError = null
                        actionMessage = null
                    },
                    description = description,
                    onDescriptionChange = {
                        description = it
                        formError = null
                        actionMessage = null
                    },
                    repaymentPlanType = repaymentPlanType,
                    onRepaymentPlanTypeChange = {
                        repaymentPlanTypeName = it.name
                        formError = null
                        actionMessage = null
                    },
                    startDateMillis = startDateMillis,
                    onStartDateChange = {
                        startDateMillis = it
                        formError = null
                        actionMessage = null
                    },
                    totalExpected = totalExpected,
                    installmentPreviewAmount = installmentPreviewAmount,
                    currencySymbol = preferences.currencySymbol
                )

                if (!formError.isNullOrBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = formError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Error
                        )
                    }
                }

                PrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        val validationInput = CreateLoanInput(
                            clientId = loan.clientId,
                            principalAmount = principalAmount,
                            interestRatePercent = interestRatePercent,
                            termInDays = termInDays,
                            description = description,
                            repaymentPlanType = repaymentPlanType,
                            startDateMillis = startDateMillis
                        )

                        val validation = LoanFormValidator.validate(validationInput)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val updatedLoan = LocalLoanRepository.updateLoan(
                            UpdateLoanInput(
                                loanId = loan.id,
                                principalAmount = principalAmount,
                                interestRatePercent = interestRatePercent,
                                termInDays = termInDays,
                                description = description,
                                repaymentPlanType = repaymentPlanType,
                                startDateMillis = startDateMillis
                            )
                        )

                        if (updatedLoan == null) {
                            formError = "No se pudo actualizar el préstamo. Si ya tiene pagos, solo puedes editar la nota."
                            return@PrimaryButton
                        }

                        LocalInstallmentRepository.removeInstallmentsForLoan(updatedLoan.id)
                        LocalInstallmentRepository.generateInstallmentsForLoan(updatedLoan)

                        onLoanUpdated(updatedLoan.id)
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
private fun ProtectedLoanEditContent(
    loan: Loan,
    description: String,
    totalPaid: Double,
    paymentHistoryCount: Int,
    currencySymbol: String,
    onDescriptionChange: (String) -> Unit,
    formError: String?,
    actionMessage: String?,
    onSaveDescription: () -> Unit,
    onNavigateBack: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Edición financiera bloqueada",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            Text(
                text = "Este préstamo ya tiene movimientos financieros o está saldado. Para proteger saldos, cuotas, recibos, reportes y backup, no se puede cambiar monto, interés, plazo, plan ni fecha de inicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Solo puedes actualizar la descripción o nota interna.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )
        }
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Datos financieros protegidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            ReadOnlyLoanField(
                title = "Monto prestado",
                value = formatMoney(loan.principalAmount, currencySymbol)
            )

            ReadOnlyLoanField(
                title = "Interés",
                value = "${loan.interestRatePercent}%"
            )

            ReadOnlyLoanField(
                title = "Total esperado",
                value = formatMoney(loan.totalExpectedAmount, currencySymbol)
            )

            ReadOnlyLoanField(
                title = "Pagado activo",
                value = formatMoney(totalPaid, currencySymbol)
            )

            ReadOnlyLoanField(
                title = "Plazo / cuotas",
                value = loan.termInDays.toString()
            )

            ReadOnlyLoanField(
                title = "Tipo de plan",
                value = loan.repaymentPlanType.description
            )

            ReadOnlyLoanField(
                title = "Fecha de inicio",
                value = formatDate(loan.startDateMillis)
            )

            ReadOnlyLoanField(
                title = "Movimientos en historial",
                value = paymentHistoryCount.toString()
            )
        }
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Nota editable",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            AppTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Descripción / nota",
                singleLine = false,
                supportingText = "Esta nota no afecta saldos, cuotas ni reportes financieros."
            )
        }
    }

    if (!formError.isNullOrBlank()) {
        AppCard(bordered = true) {
            Text(
                text = formError,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Error
            )
        }
    }

    if (!actionMessage.isNullOrBlank()) {
        AppCard(bordered = true) {
            Text(
                text = actionMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Success
            )
        }
    }

    PrimaryButton(
        text = "Guardar nota",
        onClick = onSaveDescription
    )

    SecondaryButton(
        text = "Volver",
        onClick = onNavigateBack
    )
}

@Composable
private fun ReadOnlyLoanField(
    title: String,
    value: String
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
            color = AppColors.Gray900
        )
    }
}

@Composable
private fun EditableLoanFinancialForm(
    principalAmount: String,
    onPrincipalChange: (String) -> Unit,
    interestRatePercent: String,
    onInterestChange: (String) -> Unit,
    termInDays: String,
    onTermChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    repaymentPlanType: RepaymentPlanType,
    onRepaymentPlanTypeChange: (RepaymentPlanType) -> Unit,
    startDateMillis: Long,
    onStartDateChange: (Long) -> Unit,
    totalExpected: Double,
    installmentPreviewAmount: Double,
    currencySymbol: String
) {
    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Tipo de pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                if (repaymentPlanType == RepaymentPlanType.SINGLE_PAYMENT) {
                    PrimaryButton(
                        text = "Pago único",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onRepaymentPlanTypeChange(RepaymentPlanType.SINGLE_PAYMENT)
                        }
                    )
                } else {
                    SecondaryButton(
                        text = "Pago único",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onRepaymentPlanTypeChange(RepaymentPlanType.SINGLE_PAYMENT)
                        }
                    )
                }

                if (repaymentPlanType == RepaymentPlanType.INSTALLMENTS) {
                    PrimaryButton(
                        text = "Por cuotas",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onRepaymentPlanTypeChange(RepaymentPlanType.INSTALLMENTS)
                        }
                    )
                } else {
                    SecondaryButton(
                        text = "Por cuotas",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onRepaymentPlanTypeChange(RepaymentPlanType.INSTALLMENTS)
                        }
                    )
                }
            }

            Text(
                text = repaymentPlanType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }

    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            AppTextField(
                value = principalAmount,
                onValueChange = onPrincipalChange,
                label = "Monto prestado",
                keyboardType = KeyboardType.Number
            )

            AppTextField(
                value = interestRatePercent,
                onValueChange = onInterestChange,
                label = "Interés (%)",
                keyboardType = KeyboardType.Number
            )

            AppTextField(
                value = termInDays,
                onValueChange = onTermChange,
                label = if (repaymentPlanType == RepaymentPlanType.SINGLE_PAYMENT) {
                    "Días hasta el pago"
                } else {
                    "Cantidad de cuotas diarias"
                },
                keyboardType = KeyboardType.Number
            )

            AppDatePickerField(
                label = "Fecha de inicio",
                selectedDateMillis = startDateMillis,
                onDateSelected = onStartDateChange,
                helperText = "Al guardar, las cuotas se recalcularán desde esta fecha."
            )

            AppTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Descripción / nota",
                singleLine = false
            )
        }
    }

    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Vista previa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = "Total a pagar: ${formatMoney(totalExpected, currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = when (repaymentPlanType) {
                    RepaymentPlanType.SINGLE_PAYMENT -> "Pago único: ${formatMoney(installmentPreviewAmount, currencySymbol)}"
                    RepaymentPlanType.INSTALLMENTS -> "Cuota diaria estimada: ${formatMoney(installmentPreviewAmount, currencySymbol)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}


