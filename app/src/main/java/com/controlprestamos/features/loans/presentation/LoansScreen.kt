package com.controlprestamos.features.loans.presentation

import com.controlprestamos.core.ui.components.AppBottomNavigationBar

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
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private enum class LoansTab(
    val label: String
) {
    ALL("Todos"),
    ACTIVE("Activos"),
    OVERDUE("Vencidos"),
    COMPLETED("Completados"),
    SINGLE_PAYMENT("Pago único"),
    INSTALLMENTS("Por cuotas")
}

private var screenCurrencySymbol = "$"

@Composable
fun LoansScreen(
    onNavigateBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenLoanDetail: (String) -> Unit,
    onCreateLoanFromClients: () -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)
    screenCurrencySymbol = preferences.currencySymbol

    var selectedTab by rememberSaveable {
        mutableStateOf(LoansTab.ALL)
    }

    val state = buildLoansScreenState()
    val visibleLoans = state.loans.filterByTab(selectedTab)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Préstamos",
                subtitle = "Control general de cartera",
                showBack = true,
                showMore = true,
                onBack = onNavigateBack,
                onMore = onOpenMore
            )
        },
                bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "loans",
                onOpenDashboard = onOpenDashboard,
                onOpenClients = onOpenClients,
                onOpenLoans = { },
                onOpenPayments = onOpenPayments
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
                LoansHeader(
                    state = state,
                    onCreateLoanFromClients = onCreateLoanFromClients
                )

                LoansTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                if (visibleLoans.isEmpty()) {
                    EmptyState(
                        title = emptyTitleForTab(selectedTab),
                        description = emptyDescriptionForTab(selectedTab),
                        action = {
                            SecondaryButton(
                                text = "Crear desde Clientes",
                                onClick = onCreateLoanFromClients
                            )
                        }
                    )
                } else {
                    visibleLoans.forEach { item ->
                        LoanListCard(
                            item = item,
                            onOpenLoanDetail = onOpenLoanDetail
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoansHeader(
    state: LoansScreenState,
    onCreateLoanFromClients: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Cartera de préstamos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Gray900
        )

        Text(
            text = "Consulta activos, vencidos, completados y separa pago único de cuotas.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoansMiniStatCard(
                title = "Total",
                value = state.totalLoans.toString(),
                subtitle = "Préstamos",
                modifier = Modifier.weight(1f)
            )

            LoansMiniStatCard(
                title = "Pendiente",
                value = formatMoney(state.totalPendingAmount),
                subtitle = "Por cobrar",
                modifier = Modifier.weight(1f),
                isWarning = state.totalPendingAmount > 0.0
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoansMiniStatCard(
                title = "Vencidos",
                value = state.overdueLoans.toString(),
                subtitle = "Revisar",
                modifier = Modifier.weight(1f),
                isWarning = state.overdueLoans > 0
            )

            LoansMiniStatCard(
                title = "Completados",
                value = state.completedLoans.toString(),
                subtitle = "Finalizados",
                modifier = Modifier.weight(1f)
            )
        }

        PrimaryButton(
            text = "Crear préstamo desde cliente",
            onClick = onCreateLoanFromClients
        )
    }
}

@Composable
private fun LoansMiniStatCard(
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
private fun LoansTabSelector(
    selectedTab: LoansTab,
    onTabSelected: (LoansTab) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanTabButton(
                tab = LoansTab.ALL,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            LoanTabButton(
                tab = LoansTab.ACTIVE,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanTabButton(
                tab = LoansTab.OVERDUE,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            LoanTabButton(
                tab = LoansTab.COMPLETED,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoanTabButton(
                tab = LoansTab.SINGLE_PAYMENT,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            LoanTabButton(
                tab = LoansTab.INSTALLMENTS,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoanTabButton(
    tab: LoansTab,
    selectedTab: LoansTab,
    onTabSelected: (LoansTab) -> Unit,
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
private fun LoanListCard(
    item: LoanUiItem,
    onOpenLoanDetail: (String) -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onOpenLoanDetail(item.loanId)
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
                ClientAvatar(fullName = item.clientName)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "${item.planLabel} · Creado ${item.createdDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )

                    Text(
                        text = "Pagado: ${formatMoney(item.totalPaid)} de ${formatMoney(item.totalExpected)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray600
                    )

                    if (!item.nextDueText.isNullOrBlank()) {
                        Text(
                            text = item.nextDueText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.status == AppStatus.OVERDUE) AppColors.Error else AppColors.Gray600
                        )
                    }
                }

                StatusChip(status = item.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                LoansMiniStatCard(
                    title = "Saldo",
                    value = formatMoney(item.remainingAmount),
                    subtitle = "Pendiente",
                    modifier = Modifier.weight(1f),
                    isWarning = item.remainingAmount > 0.0
                )

                LoansMiniStatCard(
                    title = "Cuotas",
                    value = item.installmentSummary,
                    subtitle = item.installmentStatusText,
                    modifier = Modifier.weight(1f),
                    isWarning = item.overdueInstallments > 0
                )
            }

            SecondaryButton(
                text = "Ver detalle",
                onClick = {
                    onOpenLoanDetail(item.loanId)
                }
            )
        }
    }
}

private data class LoansScreenState(
    val loans: List<LoanUiItem>,
    val totalLoans: Int,
    val completedLoans: Int,
    val overdueLoans: Int,
    val totalPendingAmount: Double
)

private data class LoanUiItem(
    val loanId: String,
    val clientName: String,
    val planType: RepaymentPlanType,
    val planLabel: String,
    val createdDate: String,
    val totalExpected: Double,
    val totalPaid: Double,
    val remainingAmount: Double,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val overdueInstallments: Int,
    val nextDueText: String?,
    val status: AppStatus
) {
    val installmentSummary: String
        get() = "$paidInstallments/$totalInstallments"

    val installmentStatusText: String
        get() = when {
            overdueInstallments > 0 -> "$overdueInstallments vencidas"
            remainingAmount <= 0.0 -> "Completado"
            totalInstallments <= 1 -> "Pago único"
            else -> "En curso"
        }
}

private fun buildLoansScreenState(): LoansScreenState {
    ensureInstallmentsForExistingLoans()

    val items = LocalLoanRepository.getAllLoans()
        .map { loan ->
            loan.toLoanUiItem()
        }

    return LoansScreenState(
        loans = items,
        totalLoans = items.size,
        completedLoans = items.count { it.status == AppStatus.COMPLETED },
        overdueLoans = items.count { it.status == AppStatus.OVERDUE },
        totalPendingAmount = items.sumOf { it.remainingAmount }
    )
}

private fun ensureInstallmentsForExistingLoans() {
    LocalLoanRepository.getAllLoans().forEach { loan ->
        LocalInstallmentRepository.generateInstallmentsForLoan(loan)
    }
}

private fun Loan.toLoanUiItem(): LoanUiItem {
    val client = LocalClientRepository.getClientById(clientId)
    val installments = LocalInstallmentRepository.getInstallmentsByLoan(id).filter { it.status != InstallmentStatus.CANCELLED }
    val totalPaid = LocalPaymentRepository.getTotalPaidByLoan(id)
    val remainingAmount = max(totalExpectedAmount - totalPaid, 0.0)
    val overdueInstallments = installments.count {
        it.status == InstallmentStatus.OVERDUE
    }
    val paidInstallments = installments.count {
        it.status == InstallmentStatus.PAID
    }
    val nextInstallment = LocalInstallmentRepository.getNextPendingInstallment(id)

    val status = when {
        status == LoanStatus.CANCELLED -> AppStatus.INACTIVE
        remainingAmount <= 0.0 -> AppStatus.COMPLETED
        overdueInstallments > 0 -> AppStatus.OVERDUE
        else -> AppStatus.ACTIVE
    }

    val nextDueText = nextInstallment?.let { installment ->
        "Próxima cuota #${installment.number}: ${formatMoney(installment.pendingAmount)} · ${formatDate(installment.dueDateMillis)}"
    }

    return LoanUiItem(
        loanId = id,
        clientName = client?.fullName ?: "Cliente no encontrado",
        planType = repaymentPlanType,
        planLabel = repaymentPlanType.label,
        createdDate = formatDate(createdAtMillis),
        totalExpected = totalExpectedAmount,
        totalPaid = totalPaid,
        remainingAmount = remainingAmount,
        totalInstallments = installments.size,
        paidInstallments = paidInstallments,
        overdueInstallments = overdueInstallments,
        nextDueText = nextDueText,
        status = status
    )
}

private fun List<LoanUiItem>.filterByTab(tab: LoansTab): List<LoanUiItem> {
    return when (tab) {
        LoansTab.ALL -> this
        LoansTab.ACTIVE -> filter { it.status == AppStatus.ACTIVE }
        LoansTab.OVERDUE -> filter { it.status == AppStatus.OVERDUE }
        LoansTab.COMPLETED -> filter { it.status == AppStatus.COMPLETED }
        LoansTab.SINGLE_PAYMENT -> filter { it.planType == RepaymentPlanType.SINGLE_PAYMENT }
        LoansTab.INSTALLMENTS -> filter { it.planType == RepaymentPlanType.INSTALLMENTS }
    }
}

private fun emptyTitleForTab(tab: LoansTab): String {
    return when (tab) {
        LoansTab.ALL -> "No hay préstamos"
        LoansTab.ACTIVE -> "No hay préstamos activos"
        LoansTab.OVERDUE -> "No hay préstamos vencidos"
        LoansTab.COMPLETED -> "No hay préstamos completados"
        LoansTab.SINGLE_PAYMENT -> "No hay préstamos de pago único"
        LoansTab.INSTALLMENTS -> "No hay préstamos por cuotas"
    }
}

private fun emptyDescriptionForTab(tab: LoansTab): String {
    return when (tab) {
        LoansTab.ALL -> "Crea un préstamo desde el detalle de un cliente."
        LoansTab.ACTIVE -> "Los préstamos con saldo pendiente aparecerán aquí."
        LoansTab.OVERDUE -> "Los préstamos con cuotas vencidas aparecerán aquí."
        LoansTab.COMPLETED -> "Los préstamos finalizados aparecerán aquí."
        LoansTab.SINGLE_PAYMENT -> "Los préstamos de una sola cuota aparecerán aquí."
        LoansTab.INSTALLMENTS -> "Los préstamos divididos en cuotas aparecerán aquí."
    }
}

private fun formatMoney(value: Double): String {
    return screenCurrencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}








