package com.controlprestamos.features.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppBottomNavigationBar
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Suppress("UNUSED_PARAMETER")
@Composable
fun DashboardScreen(
    onOpenMore: () -> Unit,
    onOpenDashboard: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenLoans: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
) {
    val preferences = LocalPreferencesRepository.getPreferences(
        context = androidx.compose.ui.platform.LocalContext.current
    )

    val state = buildDashboardState(
        currencySymbol = preferences.currencySymbol
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Inicio",
                subtitle = "Panel ejecutivo",
                showMore = true,
                onMore = onOpenMore
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "dashboard",
                onOpenDashboard = { },
                onOpenClients = onOpenClients,
                onOpenLoans = onOpenLoans,
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
                DashboardExecutiveCard(state = state)

                SectionTitle(
                    title = "Cobranza",
                    subtitle = "Pagos recibidos según fecha real registrada."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    DashboardMetricCard(
                        title = "Hoy",
                        value = state.collectedTodayFormatted,
                        subtitle = "${state.paymentsToday} pagos",
                        modifier = Modifier.weight(1f),
                        strong = true
                    )

                    DashboardMetricCard(
                        title = "Semana",
                        value = state.collectedWeekFormatted,
                        subtitle = "${state.paymentsWeek} pagos",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    DashboardMetricCard(
                        title = "Mes",
                        value = state.collectedMonthFormatted,
                        subtitle = "${state.paymentsMonth} pagos",
                        modifier = Modifier.weight(1f),
                        strong = true
                    )

                    DashboardMetricCard(
                        title = "Promedio",
                        value = state.averagePaymentMonthFormatted,
                        subtitle = "Pago mensual",
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionTitle(
                    title = "Cartera",
                    subtitle = "Estado general de préstamos, deuda y vencimientos."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    DashboardMetricCard(
                        title = "Prestado",
                        value = state.totalLentFormatted,
                        subtitle = "${state.activeLoans} activos",
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetricCard(
                        title = "Pendiente",
                        value = state.totalPendingFormatted,
                        subtitle = "Saldo vivo",
                        modifier = Modifier.weight(1f),
                        warning = state.totalPending > 0.0
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    DashboardMetricCard(
                        title = "Vencido",
                        value = state.overdueAmountFormatted,
                        subtitle = "${state.overdueInstallments} cuotas",
                        modifier = Modifier.weight(1f),
                        danger = state.overdueAmount > 0.0
                    )

                    DashboardMetricCard(
                        title = "Cobrar hoy",
                        value = state.todayDueFormatted,
                        subtitle = "${state.todayDueInstallments} cuotas",
                        modifier = Modifier.weight(1f),
                        warning = state.todayDue > 0.0
                    )
                }

                CollectionProgressCard(state = state)

                SevenDayCollectionCard(state = state)

                PortfolioHealthCard(state = state)

                DashboardAlertsCard(state = state)
            }
        }
    }
}

@Composable
private fun DashboardExecutiveCard(
    state: DashboardState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Panel ejecutivo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Resumen operativo de cartera, cobros y vencimientos. No incluye clientes archivados.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = state.collectedMonthFormatted,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )

            Text(
                text = "Cobrado este mes · ${state.paymentsMonth} pagos",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                CompactInfo(
                    title = "Pendiente",
                    value = state.totalPendingFormatted,
                    modifier = Modifier.weight(1f),
                    warning = state.totalPending > 0.0
                )

                CompactInfo(
                    title = "Vencido",
                    value = state.overdueAmountFormatted,
                    modifier = Modifier.weight(1f),
                    danger = state.overdueAmount > 0.0
                )
            }
        }
    }
}

@Composable
private fun CollectionProgressCard(
    state: DashboardState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Rendimiento de cobranza",
                subtitle = "${state.collectionRateFormatted} del total a cobrar ha sido cobrado."
            )

            ProgressBarLine(
                label = "Cobrado",
                value = state.totalCollected,
                total = state.totalExpected,
                color = AppColors.Success,
                currencySymbol = state.currencySymbol
            )

            ProgressBarLine(
                label = "Pendiente",
                value = state.totalPending,
                total = state.totalExpected,
                color = AppColors.Warning,
                currencySymbol = state.currencySymbol
            )

            ProgressBarLine(
                label = "Vencido",
                value = state.overdueAmount,
                total = state.totalExpected,
                color = AppColors.Error,
                currencySymbol = state.currencySymbol
            )
        }
    }
}

@Composable
private fun SevenDayCollectionCard(
    state: DashboardState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Cobranza últimos 7 días",
                subtitle = "Movimiento diario de pagos registrados."
            )

            if (state.lastSevenDays.all { it.amount <= 0.0 }) {
                Text(
                    text = "No hay pagos registrados en los últimos 7 días.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            } else {
                SevenDayBarChart(
                    days = state.lastSevenDays,
                    currencySymbol = state.currencySymbol
                )
            }
        }
    }
}

@Composable
private fun PortfolioHealthCard(
    state: DashboardState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Salud de cartera",
                subtitle = "Indicadores rápidos del negocio."
            )

            IndicatorRow("Clientes registrados", state.totalClients.toString())
            IndicatorRow("Clientes con deuda", state.clientsWithDebt.toString())
            IndicatorRow("Préstamos activos", state.activeLoans.toString())
            IndicatorRow("Préstamos completados", state.completedLoans.toString())
            IndicatorRow("Cuotas pendientes/parciales", state.pendingInstallments.toString())
            IndicatorRow("Cuotas pagadas", state.paidInstallments.toString())
        }
    }
}

@Composable
private fun DashboardAlertsCard(
    state: DashboardState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Alertas",
                subtitle = "Prioridades para revisar hoy."
            )

            when {
                state.isEmpty -> {
                    AlertText(
                        text = "No hay datos cargados. Puedes comenzar registrando clientes y préstamos, o restaurar una copia desde Más > Backup.",
                        color = AppColors.Warning
                    )
                }

                state.overdueInstallments > 0 -> {
                    AlertText(
                        text = "Hay ${state.overdueInstallments} cuotas vencidas por ${state.overdueAmountFormatted}.",
                        color = AppColors.Error
                    )
                }

                state.todayDueInstallments > 0 -> {
                    AlertText(
                        text = "Hay ${state.todayDueInstallments} cuotas para cobrar hoy por ${state.todayDueFormatted}.",
                        color = AppColors.Warning
                    )
                }

                else -> {
                    AlertText(
                        text = "No hay cuotas vencidas ni cobros pendientes para hoy.",
                        color = AppColors.Success
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
    danger: Boolean = false,
    strong: Boolean = false
) {
    val accentColor = when {
        danger -> AppColors.Error
        warning -> AppColors.Warning
        strong -> AppColors.AccentTeal
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
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
private fun CompactInfo(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
    danger: Boolean = false
) {
    val valueColor = when {
        danger -> AppColors.Error
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
private fun ProgressBarLine(
    label: String,
    value: Double,
    total: Double,
    color: Color,
    currencySymbol: String
) {
    val progress = if (total > 0.0) {
        (value / total).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
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
                text = "${formatMoney(value, currencySymbol)} · ${formatPercent(progress.toDouble())}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.Gray600.copy(alpha = 0.16f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun SevenDayBarChart(
    days: List<DailyCollection>,
    currencySymbol: String
) {
    val maxAmount = days.maxOfOrNull { it.amount } ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val ratio = if (maxAmount > 0.0) {
                (day.amount / maxAmount).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            val barHeight = (24 + (90 * ratio)).dp

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = formatShortMoney(day.amount, currencySymbol),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Gray600
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.AccentTeal.copy(alpha = if (day.amount > 0.0) 0.9f else 0.18f))
                )

                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun IndicatorRow(
    label: String,
    value: String
) {
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
            color = AppColors.Gray900
        )
    }
}

@Composable
private fun AlertText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

private data class DashboardState(
    val currencySymbol: String,
    val totalClients: Int,
    val activeLoans: Int,
    val completedLoans: Int,
    val clientsWithDebt: Int,
    val paymentsToday: Int,
    val paymentsWeek: Int,
    val paymentsMonth: Int,
    val pendingInstallments: Int,
    val paidInstallments: Int,
    val overdueInstallments: Int,
    val todayDueInstallments: Int,
    val totalLent: Double,
    val totalExpected: Double,
    val totalCollected: Double,
    val totalPending: Double,
    val overdueAmount: Double,
    val todayDue: Double,
    val collectedToday: Double,
    val collectedWeek: Double,
    val collectedMonth: Double,
    val lastSevenDays: List<DailyCollection>,
    val isEmpty: Boolean
) {
    val totalLentFormatted: String
        get() = formatMoney(totalLent, currencySymbol)

    val totalExpectedFormatted: String
        get() = formatMoney(totalExpected, currencySymbol)

    val totalCollectedFormatted: String
        get() = formatMoney(totalCollected, currencySymbol)

    val totalPendingFormatted: String
        get() = formatMoney(totalPending, currencySymbol)

    val overdueAmountFormatted: String
        get() = formatMoney(overdueAmount, currencySymbol)

    val todayDueFormatted: String
        get() = formatMoney(todayDue, currencySymbol)

    val collectedTodayFormatted: String
        get() = formatMoney(collectedToday, currencySymbol)

    val collectedWeekFormatted: String
        get() = formatMoney(collectedWeek, currencySymbol)

    val collectedMonthFormatted: String
        get() = formatMoney(collectedMonth, currencySymbol)

    val averagePaymentMonthFormatted: String
        get() = if (paymentsMonth <= 0) {
            formatMoney(0.0, currencySymbol)
        } else {
            formatMoney(collectedMonth / paymentsMonth, currencySymbol)
        }

    val collectionRateFormatted: String
        get() = if (totalExpected > 0.0) {
            formatPercent(totalCollected / totalExpected)
        } else {
            formatPercent(0.0)
        }
}

private data class DailyCollection(
    val label: String,
    val amount: Double
)

private fun buildDashboardState(
    currencySymbol: String
): DashboardState {
    val allClients = LocalClientRepository.getClients()
    val clients = allClients.filter { client ->
        FinancialOperationRules.canShowClientInOperationalLists(client)
    }

    val clientById = allClients.associateBy { it.id }

    val loans = LocalLoanRepository
        .getAllLoans()
        .filter { loan ->
            FinancialOperationRules.shouldCountLoanInOperationalReports(
                client = clientById[loan.clientId],
                loan = loan
            )
        }

    val operationalLoanIds = loans.map { it.id }.toSet()

    val payments = LocalPaymentRepository
        .getAllPayments()
        .filter { payment ->
            payment.loanId in operationalLoanIds &&
                FinancialOperationRules.shouldCountPaymentFinancially(payment)
        }

    val installments = LocalInstallmentRepository
        .getAllInstallments()
        .filter { installment ->
            installment.status != InstallmentStatus.CANCELLED &&
                installment.loanId in operationalLoanIds
        }

    val pendingInstallments = installments.filter {
        it.status == InstallmentStatus.PENDING || it.status == InstallmentStatus.PARTIAL
    }

    val paidInstallments = installments.filter {
        it.status == InstallmentStatus.PAID
    }

    val overdueInstallments = installments.filter {
        it.status == InstallmentStatus.OVERDUE
    }

    val todayInstallments = pendingInstallments.filter {
        isToday(it.dueDateMillis)
    }

    val totalLent = loans.sumOf { it.principalAmount }
    val totalExpected = loans.sumOf { it.totalExpectedAmount }
    val totalCollected = loans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }
    val totalPending = max(totalExpected - totalCollected, 0.0)

    val completedLoans = loans.count { loan ->
        val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
        max(loan.totalExpectedAmount - paid, 0.0) <= 0.0
    }

    val activeLoans = loans.count { loan ->
        val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
        max(loan.totalExpectedAmount - paid, 0.0) > 0.0
    }

    val clientsWithDebt = loans
        .groupBy { it.clientId }
        .count { (_, clientLoans) ->
            clientLoans.any { loan ->
                val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
                max(loan.totalExpectedAmount - paid, 0.0) > 0.0
            }
        }

    val todayRange = startOfTodayMillis() to endOfTodayMillis()
    val weekRange = startOfCurrentWeekMillis() to endOfTodayMillis()
    val monthRange = startOfCurrentMonthMillis() to endOfTodayMillis()

    val paymentsToday = payments.filter { it.createdAtMillis in todayRange.first..todayRange.second }
    val paymentsWeek = payments.filter { it.createdAtMillis in weekRange.first..weekRange.second }
    val paymentsMonth = payments.filter { it.createdAtMillis in monthRange.first..monthRange.second }

    return DashboardState(
        currencySymbol = currencySymbol,
        totalClients = clients.size,
        activeLoans = activeLoans,
        completedLoans = completedLoans,
        clientsWithDebt = clientsWithDebt,
        paymentsToday = paymentsToday.size,
        paymentsWeek = paymentsWeek.size,
        paymentsMonth = paymentsMonth.size,
        pendingInstallments = pendingInstallments.size,
        paidInstallments = paidInstallments.size,
        overdueInstallments = overdueInstallments.size,
        todayDueInstallments = todayInstallments.size,
        totalLent = totalLent,
        totalExpected = totalExpected,
        totalCollected = totalCollected,
        totalPending = totalPending,
        overdueAmount = overdueInstallments.sumOf { it.pendingAmount },
        todayDue = todayInstallments.sumOf { it.pendingAmount },
        collectedToday = paymentsToday.sumOf { it.amount },
        collectedWeek = paymentsWeek.sumOf { it.amount },
        collectedMonth = paymentsMonth.sumOf { it.amount },
        lastSevenDays = buildLastSevenDaysPayments(payments),
        isEmpty = clients.isEmpty() && loans.isEmpty() && payments.isEmpty() && installments.isEmpty()
    )
}

private fun buildLastSevenDaysPayments(
    payments: List<Payment>
): List<DailyCollection> {
    val result = mutableListOf<DailyCollection>()
    val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    for (offset in 6 downTo 0) {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -offset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val end = Calendar.getInstance().apply {
            timeInMillis = start.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val amount = payments
            .filter { payment ->
                payment.createdAtMillis >= start.timeInMillis &&
                    payment.createdAtMillis < end.timeInMillis
            }
            .sumOf { it.amount }

        result.add(
            DailyCollection(
                label = formatter.format(Date(start.timeInMillis)),
                amount = amount
            )
        )
    }

    return result
}

private fun startOfTodayMillis(): Long {
    return startOfDayMillis(System.currentTimeMillis())
}

private fun endOfTodayMillis(): Long {
    return endOfDayMillis(System.currentTimeMillis())
}

private fun startOfCurrentWeekMillis(): Long {
    val calendar = Calendar.getInstance()

    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

private fun startOfCurrentMonthMillis(): Long {
    val calendar = Calendar.getInstance()

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

private fun startOfDayMillis(millis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

private fun endOfDayMillis(millis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)

    return calendar.timeInMillis
}

private fun isToday(millis: Long): Boolean {
    val target = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    val today = Calendar.getInstance()

    return target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatShortMoney(
    value: Double,
    currencySymbol: String
): String {
    return when {
        value >= 1000000.0 -> currencySymbol + DecimalFormat("#,##0.0M").format(value / 1000000.0)
        value >= 1000.0 -> currencySymbol + DecimalFormat("#,##0.0K").format(value / 1000.0)
        value > 0.0 -> currencySymbol + DecimalFormat("#,##0").format(value)
        else -> currencySymbol + "0"
    }
}

private fun formatPercent(value: Double): String {
    return DecimalFormat("#,##0%").format(value.coerceIn(0.0, 1.0))
}



