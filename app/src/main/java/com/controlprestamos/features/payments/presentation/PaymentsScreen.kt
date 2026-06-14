package com.controlprestamos.features.payments.presentation

import com.controlprestamos.core.ui.components.AppBottomNavigationBar

import com.controlprestamos.features.loans.data.LocalLoanRepository

import com.controlprestamos.features.installments.domain.model.InstallmentStatus

import com.controlprestamos.features.installments.data.LocalInstallmentRepository

import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.clients.domain.model.ClientStatus





import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppBottomNavigation
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.ClientAvatar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.core.platform.openWhatsAppReminder
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class PaymentsTab(
    val label: String
) {
    TODAY("Hoy"),
    PENDING("Pendientes"),
    OVERDUE("Vencidas"),
    COLLECTED_TODAY("Cobrado hoy"),
    COMPLETED("Completadas")
}

private var screenCurrencySymbol = "$"

@Composable
fun PaymentsScreen(
    onNavigateBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenMore: () -> Unit,
    onRegisterPayment: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)
    screenCurrencySymbol = preferences.currencySymbol

    var selectedTab by rememberSaveable {
        mutableStateOf(PaymentsTab.TODAY)
    }

    val state = buildPaymentsScreenState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pagos",
                subtitle = "Modo cobranza diaria",
                showBack = true,
                showMore = true,
                onBack = onNavigateBack,
                onMore = onOpenMore
            )
        },
                bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "payments",
                onOpenDashboard = onOpenDashboard,
                onOpenClients = onOpenClients,
                onOpenLoans = onOpenLoans,
                onOpenPayments = { }
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
                PaymentsHeader(state = state)

                DailyCollectionAlert(state = state)

                PaymentsTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                when (selectedTab) {
                    PaymentsTab.TODAY -> {
                        InstallmentSection(
                            emptyTitle = "Nada urgente por cobrar hoy",
                            emptyDescription = "Cuando existan cuotas vencidas o con vencimiento de hoy, aparecerán aquí.",
                            installments = state.todayCollectionInstallments,
                            onRegisterPayment = onRegisterPayment,
                            showPriority = true
                        )
                    }

                    PaymentsTab.PENDING -> {
                        InstallmentSection(
                            emptyTitle = "Sin cuotas pendientes",
                            emptyDescription = "Cuando haya cuotas por cobrar aparecerán aquí.",
                            installments = state.pendingInstallments,
                            onRegisterPayment = onRegisterPayment
                        )
                    }

                    PaymentsTab.OVERDUE -> {
                        InstallmentSection(
                            emptyTitle = "Sin cuotas vencidas",
                            emptyDescription = "Las cuotas vencidas aparecerán aquí para priorizar la cobranza.",
                            installments = state.overdueInstallments,
                            onRegisterPayment = onRegisterPayment
                        )
                    }

                    PaymentsTab.COLLECTED_TODAY -> {
                        CollectedTodaySection(
                            payments = state.collectedTodayPayments
                        )
                    }

                    PaymentsTab.COMPLETED -> {
                        InstallmentSection(
                            emptyTitle = "Sin cuotas completadas",
                            emptyDescription = "Las cuotas pagadas aparecerán aquí.",
                            installments = state.completedInstallments,
                            onRegisterPayment = onRegisterPayment,
                            showCollectButton = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsHeader(
    state: PaymentsScreenState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Centro de cobranza",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Gray900
        )

        PaymentCollectionPdfButton()

        Text(
            text = "Prioriza vencidos, cobra lo de hoy y mantén tu cartera al día.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentsMiniStatCard(
                title = "Cobrar hoy",
                value = formatMoney(state.todayCollectionAmount),
                subtitle = "${state.todayCollectionInstallments.size} cuotas",
                modifier = Modifier.weight(1f),
                isWarning = state.todayCollectionAmount > 0.0
            )

            PaymentsMiniStatCard(
                title = "Vencido",
                value = formatMoney(state.overdueAmount),
                subtitle = "${state.overdueInstallments.size} cuotas",
                modifier = Modifier.weight(1f),
                isWarning = state.overdueAmount > 0.0
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentsMiniStatCard(
                title = "Cobrado hoy",
                value = formatMoney(state.collectedTodayAmount),
                subtitle = "${state.collectedTodayPayments.size} pagos",
                modifier = Modifier.weight(1f)
            )

            PaymentsMiniStatCard(
                title = "Pendiente",
                value = formatMoney(state.pendingAmount),
                subtitle = "${state.pendingInstallments.size} cuotas",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DailyCollectionAlert(
    state: PaymentsScreenState
) {
    val title: String
    val message: String
    val color = when {
        state.overdueInstallments.isNotEmpty() -> AppColors.Error
        state.todayDueInstallments.isNotEmpty() -> AppColors.Warning
        else -> AppColors.Success
    }

    when {
        state.overdueInstallments.isNotEmpty() -> {
            title = "Prioridad alta"
            message = "Tienes ${state.overdueInstallments.size} cuotas vencidas. Atiéndelas primero para recuperar control de cartera."
        }

        state.todayDueInstallments.isNotEmpty() -> {
            title = "Cobranza de hoy"
            message = "Tienes ${state.todayDueInstallments.size} cuotas con vencimiento de hoy."
        }

        else -> {
            title = "Sin urgencias hoy"
            message = "No hay cuotas vencidas ni vencimientos para hoy."
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PaymentsMiniStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
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
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isWarning) AppColors.Error else AppColors.AccentTeal
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PaymentsTabSelector(
    selectedTab: PaymentsTab,
    onTabSelected: (PaymentsTab) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentsTabButton(
                tab = PaymentsTab.TODAY,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            PaymentsTabButton(
                tab = PaymentsTab.PENDING,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentsTabButton(
                tab = PaymentsTab.OVERDUE,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            PaymentsTabButton(
                tab = PaymentsTab.COLLECTED_TODAY,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )
        }

        PaymentsTabButton(
            tab = PaymentsTab.COMPLETED,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PaymentsTabButton(
    tab: PaymentsTab,
    selectedTab: PaymentsTab,
    onTabSelected: (PaymentsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTab == tab) {
        PrimaryButton(
            text = tab.label,
            onClick = { onTabSelected(tab) },
            modifier = modifier
        )
    } else {
        SecondaryButton(
            text = tab.label,
            onClick = { onTabSelected(tab) },
            modifier = modifier
        )
    }
}

@Composable
private fun InstallmentSection(
    emptyTitle: String,
    emptyDescription: String,
    installments: List<Installment>,
    onRegisterPayment: (String) -> Unit,
    showCollectButton: Boolean = true,
    showPriority: Boolean = false
) {
    if (installments.isEmpty()) {
        EmptyState(
            title = emptyTitle,
            description = emptyDescription
        )
    } else {
        installments.forEach { installment ->
            InstallmentPaymentCard(
                installment = installment,
                showCollectButton = showCollectButton,
                showPriority = showPriority,
                onRegisterPayment = onRegisterPayment
            )
        }
    }
}

@Composable
private fun InstallmentPaymentCard(
    installment: Installment,
    showCollectButton: Boolean,
    showPriority: Boolean,
    onRegisterPayment: (String) -> Unit
) {
    val loan = LocalLoanRepository.getLoanById(installment.loanId)
    val client = loan?.let {
        LocalClientRepository.getClientById(it.clientId)
    }

    val clientName = client?.fullName ?: "Cliente no encontrado"
    val clientPhone = client?.phone.orEmpty()
    val status = installment.status.toAppStatus()
    val context = LocalContext.current
    val reminderMessage = buildInstallmentReminderMessage(
        clientName = clientName,
        installment = installment
    )

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (showCollectButton) {
                    onRegisterPayment(installment.loanId)
                }
            },
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                ClientAvatar(fullName = clientName)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Cuota #${installment.number} · ${formatDate(installment.dueDateMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )

                    Text(
                        text = "Pendiente: ${formatMoney(installment.pendingAmount)} de ${formatMoney(installment.expectedAmount)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray600
                    )

                    if (clientPhone.isNotBlank()) {
                        Text(
                            text = "Tel: $clientPhone",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.Gray600
                        )
                    }

                    if (showPriority) {
                        Text(
                            text = installment.priorityLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (installment.status == InstallmentStatus.OVERDUE) {
                                AppColors.Error
                            } else {
                                AppColors.Warning
                            }
                        )
                    }
                }

                StatusChip(status = status)
            }

            if (showCollectButton) {
                if (clientPhone.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        SecondaryButton(
                            text = "Cobrar",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onRegisterPayment(installment.loanId)
                            }
                        )

                        SecondaryButton(
                            text = "WhatsApp",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                openWhatsAppReminder(
                                    context = context,
                                    rawPhone = clientPhone,
                                    message = reminderMessage
                                )
                            }
                        )
                    }
                } else {
                    SecondaryButton(
                        text = "Cobrar",
                        onClick = {
                            onRegisterPayment(installment.loanId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectedTodaySection(
    payments: List<Payment>
) {
    if (payments.isEmpty()) {
        EmptyState(
            title = "Sin cobros hoy",
            description = "Los pagos registrados hoy aparecerán aquí."
        )
    } else {
        payments.forEach { payment ->
            CollectedPaymentCard(payment = payment)
        }
    }
}

@Composable
private fun CollectedPaymentCard(
    payment: Payment
) {
    val loan = LocalLoanRepository.getLoanById(payment.loanId)
    val client = loan?.let {
        LocalClientRepository.getClientById(it.clientId)
    }

    val clientName = client?.fullName ?: "Cliente no encontrado"

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientAvatar(fullName = clientName)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Monto: ${formatMoney(payment.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                Text(
                    text = "Método: ${payment.method.ifBlank { "No especificado" }} · ${formatDateTime(payment.createdAtMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray600
                )
            }

            StatusChip(status = AppStatus.COMPLETED)
        }
    }
}

private data class PaymentsScreenState(
    val todayCollectionInstallments: List<Installment>,
    val todayDueInstallments: List<Installment>,
    val pendingInstallments: List<Installment>,
    val overdueInstallments: List<Installment>,
    val completedInstallments: List<Installment>,
    val collectedTodayPayments: List<Payment>,
    val todayCollectionAmount: Double,
    val pendingAmount: Double,
    val overdueAmount: Double,
    val collectedTodayAmount: Double
)

private fun buildPaymentsScreenState(): PaymentsScreenState {
    ensureInstallmentsForExistingLoans()

    val installments = LocalInstallmentRepository.getAllInstallments()
    val allClientsForPayments = LocalClientRepository.getClients()

    val clientByIdForPayments = allClientsForPayments.associateBy { it.id }

    val operationalLoanIds = LocalLoanRepository

        .getAllLoans()

        .filter { loan ->

            FinancialOperationRules.shouldCountLoanInOperationalReports(

                client = clientByIdForPayments[loan.clientId],

                loan = loan

            )

        }

        .map { it.id }

        .toSet()


    val payments = LocalPaymentRepository.getAllPayments()

        .filter { payment ->

            payment.loanId in operationalLoanIds &&

                FinancialOperationRules.shouldCountPaymentFinancially(payment)

        }

    val pendingInstallments = installments
        .filter { installment ->
            installment.status == InstallmentStatus.PENDING ||
                installment.status == InstallmentStatus.PARTIAL
        }
        .sortedBy { it.dueDateMillis }

    val overdueInstallments = installments
        .filter { it.status == InstallmentStatus.OVERDUE }
        .sortedBy { it.dueDateMillis }

    val todayDueInstallments = pendingInstallments
        .filter { installment ->
            isToday(installment.dueDateMillis)
        }
        .sortedBy { it.dueDateMillis }

    val todayCollectionInstallments = (overdueInstallments + todayDueInstallments)
        .distinctBy { it.id }
        .sortedWith(
            compareBy<Installment> { if (it.status == InstallmentStatus.OVERDUE) 0 else 1 }
                .thenBy { it.dueDateMillis }
        )

    val completedInstallments = installments
        .filter { it.status == InstallmentStatus.PAID }
        .sortedByDescending { it.paidAtMillis ?: it.createdAtMillis }

    val collectedTodayPayments = payments
        .filter { isToday(it.createdAtMillis) }
        .sortedByDescending { it.createdAtMillis }

    return PaymentsScreenState(
        todayCollectionInstallments = todayCollectionInstallments,
        todayDueInstallments = todayDueInstallments,
        pendingInstallments = pendingInstallments,
        overdueInstallments = overdueInstallments,
        completedInstallments = completedInstallments,
        collectedTodayPayments = collectedTodayPayments,
        todayCollectionAmount = todayCollectionInstallments.sumOf { it.pendingAmount },
        pendingAmount = pendingInstallments.sumOf { it.pendingAmount },
        overdueAmount = overdueInstallments.sumOf { it.pendingAmount },
        collectedTodayAmount = collectedTodayPayments.sumOf { it.amount }
    )
}

private fun ensureInstallmentsForExistingLoans() {
    val activeClientIds = LocalClientRepository
        .getClients()
        .filter { it.status == ClientStatus.ACTIVE }
        .map { it.id }
        .toSet()

    LocalLoanRepository
        .getAllLoans()
        .filter { loan ->
            loan.status != LoanStatus.CANCELLED &&
                loan.clientId in activeClientIds
        }
        .forEach { loan: Loan ->
            LocalInstallmentRepository.generateInstallmentsForLoan(loan)
        }
}

private fun InstallmentStatus.toAppStatus(): AppStatus {
    return when (this) {
        InstallmentStatus.PENDING -> AppStatus.PENDING
        InstallmentStatus.PARTIAL -> AppStatus.PENDING
        InstallmentStatus.PAID -> AppStatus.COMPLETED
        InstallmentStatus.OVERDUE -> AppStatus.OVERDUE
        InstallmentStatus.CANCELLED -> AppStatus.INACTIVE
    }
}

private fun Installment.priorityLabel(): String {
    return when {
        status == InstallmentStatus.OVERDUE -> "Prioridad: vencida"
        isToday(dueDateMillis) -> "Prioridad: vence hoy"
        else -> "Prioridad normal"
    }
}

private fun isToday(millis: Long): Boolean {
    val target = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    val today = Calendar.getInstance()

    return target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun formatMoney(value: Double): String {
    return screenCurrencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}

private fun formatDateTime(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(millis))
}



private fun buildInstallmentReminderMessage(
    clientName: String,
    installment: Installment
): String {
    return "Hola $clientName, le recordamos que tiene una cuota pendiente de ${formatMoney(installment.pendingAmount)} con fecha ${formatDate(installment.dueDateMillis)}. Por favor, confirme cuándo puede realizar el pago. Gracias."
}














