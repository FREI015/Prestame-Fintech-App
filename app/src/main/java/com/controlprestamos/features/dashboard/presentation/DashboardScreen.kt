package com.controlprestamos.features.dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppBottomNavigationBar
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.ClientAvatar
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun DashboardScreen(
    onOpenMore: () -> Unit,
    onOpenDashboard: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenLoans: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
) {
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )

    val state = buildReferenceDashboardState(
        currencySymbol = preferences.currencySymbol
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "¡Buenos días!",
                subtitle = "Resumen de tu negocio",
                showMenu = true,
                showNotifications = true,
                onMenu = onOpenMore,
                onNotifications = onOpenMore
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "dashboard",
                onOpenDashboard = onOpenDashboard,
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
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                ReferenceMetricGrid(state = state)

                ReferenceChartCard(state = state)

                ReferenceUpcomingPaymentsCard(state = state)

                ReferenceQuickActions(
                    onOpenLoans = onOpenLoans,
                    onOpenPayments = onOpenPayments,
                    onOpenClients = onOpenClients
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun ReferenceMetricGrid(
    state: ReferenceDashboardState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ReferenceMetricCard(
                icon = "▣",
                title = "Total prestado",
                value = state.totalLentText,
                subtitle = "Total acumulado",
                accentColor = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )

            ReferenceMetricCard(
                icon = "✓",
                title = "Cobrado hoy",
                value = state.collectedTodayText,
                subtitle = "${state.paymentsToday} pagos",
                accentColor = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ReferenceMetricCard(
                icon = "!",
                title = "Pagos pendientes",
                value = state.pendingAmountText,
                subtitle = "${state.pendingInstallments} cuotas",
                accentColor = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )

            ReferenceMetricCard(
                icon = "●",
                title = "Clientes activos",
                value = state.activeClients.toString(),
                subtitle = "Con préstamos vigentes",
                accentColor = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReferenceMetricCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 116.dp),
        color = AppColors.Surface,
        shape = RoundedCornerShape(AppRadius.card),
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(AppRadius.pill))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )
        }
    }
}

@Composable
private fun ReferenceChartCard(
    state: ReferenceDashboardState
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Cobros recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Últimos 7 días",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }

            Text(
                text = state.weekCollectedText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        ReferenceBarChart(
            days = state.lastSevenDays
        )
    }
}

@Composable
private fun ReferenceBarChart(
    days: List<ReferenceDailyCollection>
) {
    val maxAmount = days.maxOfOrNull { it.amount } ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val ratio = if (maxAmount > 0.0) {
                (day.amount / maxAmount).toFloat().coerceIn(0.08f, 1f)
            } else {
                0.08f
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(AppColors.AccentTeal)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Gray500
                )
            }
        }
    }
}

@Composable
private fun ReferenceUpcomingPaymentsCard(
    state: ReferenceDashboardState
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximos pagos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = if (state.upcomingPayments.isEmpty()) "Sin pendientes" else "Ver todos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (state.upcomingPayments.isEmpty()) {
            Text(
                text = "No hay cuotas pendientes por mostrar.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                state.upcomingPayments.forEach { item ->
                    ReferencePaymentRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun ReferencePaymentRow(
    item: ReferenceUpcomingPayment
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClientAvatar(
            fullName = item.clientName,
            size = 42.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = item.loanLabel,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.amountText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = item.dateText,
                style = MaterialTheme.typography.labelSmall,
                color = if (item.isOverdue) AppColors.Error else AppColors.Gray500
            )
        }
    }
}

@Composable
private fun ReferenceQuickActions(
    onOpenLoans: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenClients: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        ReferenceActionButton(
            title = "Nuevo préstamo",
            icon = "+",
            background = AppColors.AccentTeal,
            onClick = onOpenLoans,
            modifier = Modifier.weight(1f)
        )

        ReferenceActionButton(
            title = "Registrar pago",
            icon = "▣",
            background = AppColors.PrimaryDark,
            onClick = onOpenPayments,
            modifier = Modifier.weight(1f)
        )

        ReferenceActionButton(
            title = "Clientes",
            icon = "◉",
            background = AppColors.SecondaryDark,
            onClick = onOpenClients,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReferenceActionButton(
    title: String,
    icon: String,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable {
                onClick()
            },
        color = background,
        shape = RoundedCornerShape(AppRadius.card),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )
        }
    }
}

@Composable
private fun ReferenceCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Surface,
        shape = RoundedCornerShape(AppRadius.cardLarge),
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            content = content
        )
    }
}

private data class ReferenceDashboardState(
    val totalLentText: String,
    val collectedTodayText: String,
    val pendingAmountText: String,
    val weekCollectedText: String,
    val activeClients: Int,
    val paymentsToday: Int,
    val pendingInstallments: Int,
    val lastSevenDays: List<ReferenceDailyCollection>,
    val upcomingPayments: List<ReferenceUpcomingPayment>
)

private data class ReferenceDailyCollection(
    val label: String,
    val amount: Double
)

private data class ReferenceUpcomingPayment(
    val clientName: String,
    val loanLabel: String,
    val amountText: String,
    val dateText: String,
    val isOverdue: Boolean
)

private fun buildReferenceDashboardState(
    currencySymbol: String
): ReferenceDashboardState {
    val clients = LocalClientRepository.getClients()

    val activeLoans = LocalLoanRepository
        .getAllLoans()
        .filter { loan -> loan.status == LoanStatus.ACTIVE }

    val activePayments = LocalPaymentRepository
        .getAllPayments()
        .filter { payment -> payment.status == PaymentStatus.ACTIVE }

    val activeLoanIds = activeLoans.map { loan -> loan.id }.toSet()

    val operationalPayments = activePayments
        .filter { payment -> payment.loanId in activeLoanIds }

    val installments = activeLoans.flatMap { loan ->
        LocalInstallmentRepository.getInstallmentsByLoan(loan.id)
    }

    val pendingInstallments = installments.filter { installment ->
        installment.status == InstallmentStatus.PENDING ||
            installment.status == InstallmentStatus.PARTIAL ||
            installment.status == InstallmentStatus.OVERDUE
    }

    val totalLent = activeLoans.sumOf { loan -> loan.principalAmount }

    val totalExpected = activeLoans.sumOf { loan -> loan.totalExpectedAmount }

    val totalCollected = activeLoans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }

    val pendingAmount = max(totalExpected - totalCollected, 0.0)

    val todayStart = startOfTodayMillis()
    val todayEnd = endOfTodayMillis()
    val weekStart = todayStart - (6L * 24L * 60L * 60L * 1000L)

    val paymentsToday = operationalPayments.filter { payment ->
        payment.createdAtMillis in todayStart..todayEnd
    }

    val weekPayments = operationalPayments.filter { payment ->
        payment.createdAtMillis >= weekStart
    }

    val activeClients = activeLoans
        .map { loan -> loan.clientId }
        .distinct()
        .count()

    return ReferenceDashboardState(
        totalLentText = formatMoney(totalLent, currencySymbol),
        collectedTodayText = formatMoney(paymentsToday.sumOf { payment -> payment.amount }, currencySymbol),
        pendingAmountText = formatMoney(pendingAmount, currencySymbol),
        weekCollectedText = formatMoney(weekPayments.sumOf { payment -> payment.amount }, currencySymbol),
        activeClients = activeClients.coerceAtLeast(
            clients.count { client -> activeLoans.any { loan -> loan.clientId == client.id } }
        ),
        paymentsToday = paymentsToday.size,
        pendingInstallments = pendingInstallments.size,
        lastSevenDays = buildReferenceLastSevenDays(
            payments = operationalPayments
        ),
        upcomingPayments = buildReferenceUpcomingPayments(
            activeLoans = activeLoans,
            pendingInstallments = pendingInstallments,
            currencySymbol = currencySymbol
        )
    )
}

private fun buildReferenceLastSevenDays(
    payments: List<Payment>
): List<ReferenceDailyCollection> {
    val formatter = SimpleDateFormat("dd", Locale.getDefault())
    val result = mutableListOf<ReferenceDailyCollection>()

    for (offset in 6 downTo 0) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -offset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = calendar.timeInMillis
        val end = Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val amount = payments
            .filter { payment -> payment.createdAtMillis >= start && payment.createdAtMillis < end }
            .sumOf { payment -> payment.amount }

        result.add(
            ReferenceDailyCollection(
                label = formatter.format(Date(start)),
                amount = amount
            )
        )
    }

    return result
}

private fun buildReferenceUpcomingPayments(
    activeLoans: List<Loan>,
    pendingInstallments: List<com.controlprestamos.features.installments.domain.model.Installment>,
    currencySymbol: String
): List<ReferenceUpcomingPayment> {
    val loanById = activeLoans.associateBy { loan -> loan.id }

    return pendingInstallments
        .sortedBy { installment -> installment.dueDateMillis }
        .take(4)
        .map { installment ->
            val loan = loanById[installment.loanId]
            val client = loan?.let { currentLoan ->
                LocalClientRepository.getClientById(currentLoan.clientId)
            }

            val isOverdue = installment.status == InstallmentStatus.OVERDUE ||
                installment.dueDateMillis < startOfTodayMillis()

            ReferenceUpcomingPayment(
                clientName = client?.fullName.orEmpty().ifBlank { "Cliente" },
                loanLabel = loan?.let { currentLoan -> "Préstamo #${currentLoan.id.takeLast(4)}" }.orEmpty().ifBlank { "Préstamo" },
                amountText = formatMoney(installment.pendingAmount, currencySymbol),
                dateText = formatDate(installment.dueDateMillis),
                isOverdue = isOverdue
            )
        }
}

private fun startOfTodayMillis(): Long {
    return startOfDayMillis(System.currentTimeMillis())
}

private fun endOfTodayMillis(): Long {
    return endOfDayMillis(System.currentTimeMillis())
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

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

