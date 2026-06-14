package com.controlprestamos.features.loans.presentation

import com.controlprestamos.core.ui.components.AppDatePickerField

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
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.validation.LoanFormValidator
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat

private var screenCurrencySymbol = "$"

@Composable
fun CreateLoanScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onLoanCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)
    screenCurrencySymbol = preferences.currencySymbol

    val client = LocalClientRepository.getClientById(clientId)

    var principalAmount by rememberSaveable { mutableStateOf("") }
    var interestRatePercent by rememberSaveable { mutableStateOf("") }
    var termInDays by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var repaymentPlanTypeName by rememberSaveable { mutableStateOf(RepaymentPlanType.INSTALLMENTS.name) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var startDateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

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
                title = "Crear préstamo",
                subtitle = client?.fullName ?: "Cliente no encontrado",
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
                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "No se puede crear un préstamo sin cliente asociado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )
                    return@Column
                }
                if (!FinancialOperationRules.canCreateLoanForClient(client)) {
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


                Text(
                    text = "Nuevo préstamo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Define monto, interés, vencimiento y forma de pago.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Forma de pago",
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
                                    onClick = {
                                        repaymentPlanTypeName = RepaymentPlanType.SINGLE_PAYMENT.name
                                        formError = null
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                SecondaryButton(
                                    text = "Pago único",
                                    onClick = {
                                        repaymentPlanTypeName = RepaymentPlanType.SINGLE_PAYMENT.name
                                        formError = null
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (repaymentPlanType == RepaymentPlanType.INSTALLMENTS) {
                                PrimaryButton(
                                    text = "Por cuotas",
                                    onClick = {
                                        repaymentPlanTypeName = RepaymentPlanType.INSTALLMENTS.name
                                        formError = null
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                SecondaryButton(
                                    text = "Por cuotas",
                                    onClick = {
                                        repaymentPlanTypeName = RepaymentPlanType.INSTALLMENTS.name
                                        formError = null
                                    },
                                    modifier = Modifier.weight(1f)
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

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AppTextField(
                            value = principalAmount,
                            onValueChange = {
                                principalAmount = it.replace(",", ".")
                                formError = null
                            },
                            label = "Monto prestado",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Decimal
                        )

                        AppTextField(
                            value = interestRatePercent,
                            onValueChange = {
                                interestRatePercent = it.replace(",", ".")
                                formError = null
                            },
                            label = "Interés (%)",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Decimal
                        )

                        AppTextField(
                            value = termInDays,
                            onValueChange = {
                                termInDays = it.filter { char -> char.isDigit() }
                                formError = null
                            },
                            label = if (repaymentPlanType == RepaymentPlanType.SINGLE_PAYMENT) {
                                "Vence en días"
                            } else {
                                "Duración en días"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Number
                        )

                        
AppDatePickerField(
                        
    label = "Fecha de inicio",
                        
    selectedDateMillis = startDateMillis,
                        
    onDateSelected = { selected ->
                        
        startDateMillis = selected
                        
        formError = null
                        
    },
                        
    helperText = "Las cuotas se generarán desde esta fecha."
                        
)

                        AppTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                formError = null
                            },
                            label = "Descripción o nota",
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!formError.isNullOrBlank()) {
                            Text(
                                text = formError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Error
                            )
                        }
                    }
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
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
                            text = "Forma de pago: ${repaymentPlanType.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Total esperado: ${formatMoney(totalExpected)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = when (repaymentPlanType) {
                                RepaymentPlanType.SINGLE_PAYMENT -> "Cuota única: ${formatMoney(installmentPreviewAmount)}"
                                RepaymentPlanType.INSTALLMENTS -> "Cuota diaria estimada: ${formatMoney(installmentPreviewAmount)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = when {
                                safeTerm <= 0 -> "Calendario: pendiente por plazo"
                                repaymentPlanType == RepaymentPlanType.SINGLE_PAYMENT -> "Calendario: 1 pago al día $safeTerm"
                                else -> "Calendario: $safeTerm cuotas diarias"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }
                }

                PrimaryButton(
                    text = "Guardar préstamo",
                    onClick = {
                        val input = CreateLoanInput(
                            clientId = clientId,
                            principalAmount = principalAmount,
                            interestRatePercent = interestRatePercent,
                            termInDays = termInDays,
                            description = description,
                            startDateMillis = startDateMillis,
                            repaymentPlanType = repaymentPlanType
                        )

                        val validation = LoanFormValidator.validate(input)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val createdLoan = LocalLoanRepository.createLoan(input)
                        LocalInstallmentRepository.generateInstallmentsForLoan(createdLoan)

                        onLoanCreated(createdLoan.id)
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

private fun formatMoney(value: Double): String {
    return screenCurrencySymbol + DecimalFormat("#,##0.00").format(value)
}








