package com.controlprestamos.features.loans.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.loans.domain.validation.LoanFormValidator
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max


@Composable
fun CreateLoanScreen(
    clientId: String? = null,
    preselectedClientId: String? = clientId,
    onNavigateBack: () -> Unit,
    onLoanCreated: (String) -> Unit,
    onOpenClients: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val activeClients = LocalClientRepository.getActiveClients()

    var selectedClientId by rememberSaveable {
        mutableStateOf(preselectedClientId.orEmpty())
    }

    val selectedClient = selectedClientId
        .ifBlank { activeClients.firstOrNull()?.id.orEmpty() }
        .takeIf { it.isNotBlank() }
        ?.let { LocalClientRepository.getClientById(it) }

    if (selectedClient != null && selectedClientId.isBlank()) {
        selectedClientId = selectedClient.id
    }

    var principalAmount by rememberSaveable { mutableStateOf("") }
    var interestRatePercent by rememberSaveable { mutableStateOf("") }
    var termInDays by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var repaymentPlanTypeName by rememberSaveable {
        mutableStateOf(RepaymentPlanType.INSTALLMENTS.name)
    }

    val repaymentPlanType = runCatching {
        RepaymentPlanType.valueOf(repaymentPlanTypeName)
    }.getOrDefault(RepaymentPlanType.INSTALLMENTS)
    var startDateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

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

    val activeLoansForClient = selectedClient?.let { client ->
        LocalLoanRepository.getLoansByClient(client.id)
            .filter { loan -> loan.status == LoanStatus.ACTIVE }
    }.orEmpty()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Crear préstamo",
                subtitle = selectedClient?.fullName ?: "Nuevo préstamo",
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

                if (activeClients.isEmpty()) {
                    EmptyState(
                        title = "Sin clientes activos",
                        description = "Crea o reactiva un cliente antes de registrar un préstamo.",
                        action = {
                            PrimaryButton(
                                text = "Ir a clientes",
                                onClick = onOpenClients
                            )
                        }
                    )

                    SecondaryButton(
                        text = "Volver",
                        onClick = onNavigateBack
                    )

                    return@Column
                }

                ClientSelectionCard(
                    clients = activeClients,
                    selectedClientId = selectedClient?.id.orEmpty(),
                    locked = !preselectedClientId.isNullOrBlank(),
                    onClientSelected = {
                        selectedClientId = it
                        formError = null
                    }
                )

                if (activeLoansForClient.isNotEmpty()) {
                    ActiveLoanWarningCard(
                        count = activeLoansForClient.size
                    )
                }

                LoanAmountCard(
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

                LoanDateAndPlanCard(
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

                LoanDescriptionCard(
                    description = description,
                    onDescriptionChange = {
                        description = it
                        formError = null
                    }
                )

                LoanPreviewCard(
                    currencySymbol = preferences.currencySymbol,
                    principal = principal,
                    interest = interest,
                    termInDays = days,
                    totalExpected = totalExpected,
                    estimatedDaily = estimatedDaily,
                    startDateMillis = startDateMillis,
                    planText = planLabel(repaymentPlanType)
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
                    text = "Crear préstamo",
                    onClick = {
                        val client = selectedClient

                        if (client == null) {
                            formError = "Selecciona un cliente activo."
                            return@PrimaryButton
                        }

                        val input = CreateLoanInput(
                            clientId = client.id,
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

                        val createdLoan = LocalLoanRepository.createLoan(input)

                        LocalInstallmentRepository.generateInstallmentsForLoan(
                            createdLoan
                        )

                        onLoanCreated(createdLoan.id)
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
private fun ClientSelectionCard(
    clients: List<Client>,
    selectedClientId: String,
    locked: Boolean,
    onClientSelected: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Cliente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = if (locked) {
                "Cliente seleccionado desde su detalle."
            } else {
                "Selecciona a quién se le hará el préstamo."
            },
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        clients.take(8).forEach { client ->
            ClientOptionRow(
                client = client,
                selected = client.id == selectedClientId,
                enabled = !locked,
                onClick = { onClientSelected(client.id) }
            )
        }

        if (clients.size > 8) {
            Text(
                text = "Mostrando los primeros 8 clientes activos.",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun ClientOptionRow(
    client: Client,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) AppColors.AccentTeal else AppColors.Gray500

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable(enabled = enabled) { onClick() },
        color = if (selected) color.copy(alpha = 0.10f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.35f) else AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = client.fullName.ifBlank { "Cliente" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) AppColors.AccentTeal else AppColors.Gray900
            )

            Text(
                text = buildClientSubtitle(client),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun ActiveLoanWarningCard(
    count: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Warning.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.cardLarge),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Warning.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Cliente con préstamo activo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            Text(
                text = "Este cliente ya tiene $count préstamo(s) activo(s). Revisa antes de crear otro.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun LoanAmountCard(
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
private fun LoanDateAndPlanCard(
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
            helperText = "Las cuotas se calcularán desde esta fecha."
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

        Text(
            text = when (repaymentPlanType) {
                RepaymentPlanType.SINGLE_PAYMENT -> "Se generará una sola cuota en la fecha indicada."
                RepaymentPlanType.INSTALLMENTS -> "Se generará un plan de cuotas según el plazo definido."
            },
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )
    }
}

@Composable
private fun LoanDescriptionCard(
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
private fun LoanPreviewCard(
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
            text = "Total esperado",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanPreviewBox(
                title = "Capital",
                value = formatMoney(principal, currencySymbol),
                modifier = Modifier.weight(1f)
            )

            LoanPreviewBox(
                title = "Interés",
                value = "${DecimalFormat("#,##0.##").format(interest)}%",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanPreviewBox(
                title = "Plazo",
                value = "$termInDays días",
                modifier = Modifier.weight(1f)
            )

            LoanPreviewBox(
                title = "Estimado diario",
                value = formatMoney(estimatedDaily, currencySymbol),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanPreviewBox(
                title = "Inicio",
                value = formatDate(startDateMillis),
                modifier = Modifier.weight(1f)
            )

            LoanPreviewBox(
                title = "Plan",
                value = planText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoanPreviewBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
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
                color = AppColors.Gray900
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

private fun buildClientSubtitle(client: Client): String {
    return listOf(
        client.documentId,
        client.phone
    )
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { "Sin documento ni teléfono" }
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

private fun parseAmount(value: String): Double? {
    return value
        .replace(",", ".")
        .trim()
        .toDoubleOrNull()
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


