package com.controlprestamos.features.loans.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppDatePickerField
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.loans.domain.model.UpdateLoanInput
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.validation.LoanFormValidator
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun EditLoanScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    onLoanUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val loan = LocalLoanRepository.getLoanById(loanId)
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }

    if (loan == null) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Editar préstamo",
                    subtitle = "Préstamo no encontrado",
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
                        .padding(AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        title = "Préstamo no encontrado",
                        description = "No pudimos encontrar el préstamo que quieres editar.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )
                }
            }
        }

        return
    }

    val safeLoan: Loan = loan ?: return

    val activePayments = LocalPaymentRepository.getPaymentsByLoan(safeLoan.id)
    val paymentHistory = LocalPaymentRepository.getPaymentHistoryByLoan(safeLoan.id)
    val installments = LocalInstallmentRepository.getInstallmentsByLoan(safeLoan.id)

    val hasPaymentHistory = activePayments.isNotEmpty() || paymentHistory.isNotEmpty()

    val hasTouchedInstallments = installments.any { installment ->
        installment.paidAmount > 0.0 ||
            installment.status == InstallmentStatus.PARTIAL ||
            installment.status == InstallmentStatus.PAID
    }

    val isClosedLoan = safeLoan.status == LoanStatus.PAID || safeLoan.status == LoanStatus.CANCELLED

    val financialLocked = hasPaymentHistory || hasTouchedInstallments || isClosedLoan

    var principalAmount by rememberSaveable {
        mutableStateOf(formatInputAmount(safeLoan.principalAmount))
    }

    var interestRatePercent by rememberSaveable {
        mutableStateOf(formatInputAmount(safeLoan.interestRatePercent))
    }

    var termInDays by rememberSaveable {
        mutableStateOf(safeLoan.termInDays.toString())
    }

    var description by rememberSaveable {
        mutableStateOf(safeLoan.description)
    }

    var repaymentPlanTypeName by rememberSaveable {
        mutableStateOf(safeLoan.repaymentPlanType.name)
    }

    val repaymentPlanType = runCatching {
        RepaymentPlanType.valueOf(repaymentPlanTypeName)
    }.getOrDefault(safeLoan.repaymentPlanType)

    var startDateMillis by rememberSaveable {
        mutableStateOf(safeLoan.startDateMillis)
    }

    var formError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val principal = parseAmount(principalAmount) ?: 0.0
    val interest = parseAmount(interestRatePercent) ?: 0.0
    val days = termInDays.trim().toIntOrNull() ?: 0

    val totalExpected = calculateTotalExpected(
        principal = principal,
        interestPercent = interest
    )

    val estimatedDaily = if (days > 0) {
        totalExpected / days
    } else {
        0.0
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar préstamo",
                subtitle = client?.fullName ?: "Préstamo",
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

                EditLoanHeaderCard(
                    loan = safeLoan,
                    clientName = client?.fullName ?: "Cliente no encontrado",
                    currencySymbol = preferences.currencySymbol,
                    paymentCount = activePayments.size + paymentHistory.size,
                    installmentCount = installments.size,
                    financialLocked = financialLocked
                )

                if (financialLocked) {
                    LockedFinancialRulesCard(
                        hasPaymentHistory = hasPaymentHistory,
                        hasTouchedInstallments = hasTouchedInstallments,
                        isClosedLoan = isClosedLoan
                    )

                    LockedFinancialSummaryCard(
                        loan = safeLoan,
                        currencySymbol = preferences.currencySymbol
                    )
                } else {
                    EditableFinancialCard(
                        principalAmount = principalAmount,
                        onPrincipalAmountChange = {
                            principalAmount = it
                            formError = null
                        },
                        interestRatePercent = interestRatePercent,
                        onInterestRatePercentChange = {
                            interestRatePercent = it
                            formError = null
                        },
                        termInDays = termInDays,
                        onTermInDaysChange = {
                            termInDays = it
                            formError = null
                        }
                    )

                    EditablePlanCard(
                        startDateMillis = startDateMillis,
                        onStartDateChange = {
                            startDateMillis = it
                            formError = null
                        },
                        repaymentPlanType = repaymentPlanType,
                        onPlanChange = {
                            repaymentPlanTypeName = it.name
                            formError = null
                        }
                    )

                    EditLoanPreviewCard(
                        currencySymbol = preferences.currencySymbol,
                        principal = principal,
                        interest = interest,
                        termInDays = days,
                        totalExpected = totalExpected,
                        estimatedDaily = estimatedDaily,
                        startDateMillis = startDateMillis,
                        planText = planLabel(repaymentPlanType)
                    )
                }

                EditDescriptionCard(
                    description = description,
                    onDescriptionChange = {
                        description = it
                        formError = null
                    }
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
                    text = if (financialLocked) {
                        "Guardar descripción"
                    } else {
                        "Guardar cambios"
                    },
                    onClick = {
                        if (financialLocked) {
                            val updatedLoan = LocalLoanRepository.updateLoanDescription(
                                loanId = safeLoan.id,
                                description = description.trim()
                            )

                            onLoanUpdated(updatedLoan?.id ?: safeLoan.id)

                            return@PrimaryButton
                        }

                        val input = CreateLoanInput(
                            clientId = safeLoan.clientId,
                            principalAmount = principalAmount,
                            interestRatePercent = interestRatePercent,
                            termInDays = termInDays,
                            description = description,
                            repaymentPlanType = repaymentPlanType,
                            startDateMillis = startDateMillis
                        )

                        val validation = LoanFormValidator.validate(input)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val updateInput = UpdateLoanInput(
                            loanId = safeLoan.id,
                            principalAmount = principalAmount,
                            interestRatePercent = interestRatePercent,
                            termInDays = termInDays,
                            description = description,
                            repaymentPlanType = repaymentPlanType,
                            startDateMillis = startDateMillis
                        )

                        val updatedLoan = LocalLoanRepository.updateLoan(updateInput)

                        LocalInstallmentRepository.rebuildInstallmentsForLoan(
                            loanId = updatedLoan?.id ?: safeLoan.id
                        )

                        onLoanUpdated(updatedLoan?.id ?: safeLoan.id)
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
private fun EditLoanHeaderCard(
    loan: Loan,
    clientName: String,
    currencySymbol: String,
    paymentCount: Int,
    installmentCount: Int,
    financialLocked: Boolean
) {
    ReferenceCard {
        Text(
            text = "Resumen del préstamo",
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
            text = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Text(
            text = formatMoney(loan.totalExpectedAmount, currencySymbol),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )

        Text(
            text = "Total esperado actual",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Estado",
                value = statusLabel(loan.status),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Protección",
                value = if (financialLocked) "Activa" else "Editable",
                modifier = Modifier.weight(1f),
                highlight = financialLocked
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Pagos",
                value = paymentCount.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Cuotas",
                value = installmentCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LockedFinancialRulesCard(
    hasPaymentHistory: Boolean,
    hasTouchedInstallments: Boolean,
    isClosedLoan: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Warning.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.cardLarge),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Warning.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Edición financiera protegida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            Text(
                text = "Este préstamo ya tiene movimiento financiero o está cerrado. Para evitar descuadres, solo puedes editar la descripción.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray700
            )

            if (hasPaymentHistory) {
                Text(
                    text = "• Tiene pagos registrados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }

            if (hasTouchedInstallments) {
                Text(
                    text = "• Tiene cuotas abonadas o pagadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }

            if (isClosedLoan) {
                Text(
                    text = "• El préstamo está pagado o cancelado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun LockedFinancialSummaryCard(
    loan: Loan,
    currencySymbol: String
) {
    ReferenceCard {
        Text(
            text = "Condiciones bloqueadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Capital",
                value = formatMoney(loan.principalAmount, currencySymbol),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Interés",
                value = "${DecimalFormat("#,##0.##").format(loan.interestRatePercent)}%",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Plazo",
                value = "${loan.termInDays} días",
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Plan",
                value = planLabel(loan.repaymentPlanType),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Inicio",
                value = formatDate(loan.startDateMillis),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Estimado diario",
                value = formatMoney(loan.estimatedDailyAmount, currencySymbol),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EditableFinancialCard(
    principalAmount: String,
    onPrincipalAmountChange: (String) -> Unit,
    interestRatePercent: String,
    onInterestRatePercentChange: (String) -> Unit,
    termInDays: String,
    onTermInDaysChange: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Condiciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "Este préstamo aún no tiene pagos aplicados. Puedes ajustar sus condiciones y se recalcularán las cuotas.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        AppTextField(
            value = principalAmount,
            onValueChange = onPrincipalAmountChange,
            label = "Capital",
            keyboardType = KeyboardType.Number
        )

        AppTextField(
            value = interestRatePercent,
            onValueChange = onInterestRatePercentChange,
            label = "Interés %",
            keyboardType = KeyboardType.Number
        )

        AppTextField(
            value = termInDays,
            onValueChange = onTermInDaysChange,
            label = "Plazo en días",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
private fun EditablePlanCard(
    startDateMillis: Long,
    onStartDateChange: (Long) -> Unit,
    repaymentPlanType: RepaymentPlanType,
    onPlanChange: (RepaymentPlanType) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Inicio y plan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        AppDatePickerField(
            label = "Fecha de inicio",
            selectedDateMillis = startDateMillis,
            onDateSelected = onStartDateChange,
            helperText = "Las cuotas se recalcularán con esta fecha."
        )

        Text(
            text = "Tipo de plan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Gray900
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PlanButton(
                text = "Cuotas",
                selected = repaymentPlanType == RepaymentPlanType.INSTALLMENTS,
                modifier = Modifier.weight(1f),
                onClick = { onPlanChange(RepaymentPlanType.INSTALLMENTS) }
            )

            PlanButton(
                text = "Pago único",
                selected = repaymentPlanType == RepaymentPlanType.SINGLE_PAYMENT,
                modifier = Modifier.weight(1f),
                onClick = { onPlanChange(RepaymentPlanType.SINGLE_PAYMENT) }
            )
        }
    }
}

@Composable
private fun EditDescriptionCard(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        AppTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descripción del préstamo",
            singleLine = false
        )
    }
}

@Composable
private fun EditLoanPreviewCard(
    currencySymbol: String,
    principal: Double,
    interest: Double,
    termInDays: Int,
    totalExpected: Double,
    estimatedDaily: Double,
    startDateMillis: Long,
    planText: String
) {
    ReferenceCard {
        Text(
            text = "Resumen antes de guardar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = formatMoney(totalExpected, currencySymbol),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (totalExpected > 0.0) AppColors.PrimaryDark else AppColors.Gray500
        )

        Text(
            text = "Nuevo total esperado",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Capital",
                value = formatMoney(principal, currencySymbol),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Interés",
                value = "${DecimalFormat("#,##0.##").format(interest)}%",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Plazo",
                value = "$termInDays días",
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Estimado diario",
                value = formatMoney(estimatedDaily, currencySymbol),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Inicio",
                value = formatDate(startDateMillis),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Plan",
                value = planText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = if (highlight) AppColors.Warning.copy(alpha = 0.08f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) AppColors.Warning.copy(alpha = 0.25f) else AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
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
                color = if (highlight) AppColors.Warning else AppColors.Gray900
            )
        }
    }
}

@Composable
private fun PlanButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (selected) AppColors.AccentTeal else AppColors.Gray500

    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .clickable { onClick() },
        color = if (selected) color.copy(alpha = 0.12f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.38f) else AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.xs),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun ReferenceCard(
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            content = content
        )
    }
}

private fun calculateTotalExpected(
    principal: Double,
    interestPercent: Double
): Double {
    if (principal <= 0.0) return 0.0

    val interestAmount = principal * (max(interestPercent, 0.0) / 100.0)

    return principal + interestAmount
}

private fun planLabel(value: RepaymentPlanType): String {
    return when (value) {
        RepaymentPlanType.SINGLE_PAYMENT -> "Pago único"
        RepaymentPlanType.INSTALLMENTS -> "Cuotas"
    }
}

private fun statusLabel(value: LoanStatus): String {
    return when (value) {
        LoanStatus.ACTIVE -> "Activo"
        LoanStatus.PAID -> "Pagado"
        LoanStatus.CANCELLED -> "Cancelado"
    }
}

private fun parseAmount(value: String): Double? {
    return value
        .replace(",", ".")
        .trim()
        .toDoubleOrNull()
}

private fun formatInputAmount(value: Double): String {
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




