package com.controlprestamos.features.dashboard.presentation

import androidx.compose.foundation.BorderStroke
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
import com.controlprestamos.core.ui.components.ClientAvatar
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
import com.controlprestamos.features.preferences.data.AppPreferences
import com.controlprestamos.features.preferences.data.AppVisualScale
import com.controlprestamos.features.preferences.data.AppVisualTheme
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
                PremiumVisualModeHeader(preferences = preferences)

                PremiumPortfolioTotalsCard(currencySymbol = preferences.currencySymbol)

                PremiumOperationalStatsCard(currencySymbol = preferences.currencySymbol)

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

                PremiumPortfolioHealthPanel(currencySymbol = preferences.currencySymbol)

                PremiumCollectionAlertsPanel(currencySymbol = preferences.currencySymbol)

                PremiumClientSnapshotCard(currencySymbol = preferences.currencySymbol)
            }
        }
    }
}



@Composable
private fun PremiumVisualModeHeader(
    preferences: AppPreferences
) {
    val theme = runCatching {
        AppVisualTheme.valueOf(preferences.visualTheme)
    }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)

    val scale = runCatching {
        AppVisualScale.valueOf(preferences.visualScale)
    }.getOrDefault(AppVisualScale.NORMAL)

    val accentColor = when (theme) {
        AppVisualTheme.EXECUTIVE_BLUE -> AppColors.AccentTeal
        AppVisualTheme.FINANCIAL_GREEN -> AppColors.Success
        AppVisualTheme.PREMIUM_GOLD -> AppColors.Warning
    }

    val scaleDescription = when (scale) {
        AppVisualScale.COMPACT -> "Vista compacta: más datos en menos espacio."
        AppVisualScale.NORMAL -> "Vista normal: equilibrio entre lectura y densidad."
        AppVisualScale.COMFORTABLE -> "Vista cómoda: más aire visual entre tarjetas."
        AppVisualScale.LARGE -> "Vista grande: lectura amplia y cómoda."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accentColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Inicio premium activo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "${theme.label} · ${scale.label}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )

            Text(
                text = scaleDescription,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PremiumPortfolioTotalsCard(
    currencySymbol: String
) {
    val activeLoans = LocalLoanRepository
        .getAllLoans()
        .filter { loan -> loan.status.name == "ACTIVE" }

    val capitalPlaced = activeLoans.sumOf { loan -> loan.principalAmount }
    val portfolioTarget = activeLoans.sumOf { loan -> loan.totalExpectedAmount }
    val expectedProfit = max(portfolioTarget - capitalPlaced, 0.0)
    val collected = activeLoans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }
    val pending = max(portfolioTarget - collected, 0.0)
    val progress = if (portfolioTarget > 0.0) {
        (collected / portfolioTarget).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "Cartera ejecutiva",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Resumen financiero real calculado desde préstamos activos y pagos vigentes.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PremiumMiniMetric(
                    title = "Capital colocado",
                    value = formatMoney(capitalPlaced, currencySymbol),
                    subtitle = "Principal activo",
                    modifier = Modifier.weight(1f)
                )

                PremiumMiniMetric(
                    title = "Utilidad pactada",
                    value = formatMoney(expectedProfit, currencySymbol),
                    subtitle = "Ganancia esperada",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PremiumMiniMetric(
                    title = "Cartera objetivo",
                    value = formatMoney(portfolioTarget, currencySymbol),
                    subtitle = "Capital + utilidad",
                    modifier = Modifier.weight(1f)
                )

                PremiumMiniMetric(
                    title = "Recaudado",
                    value = formatMoney(collected, currencySymbol),
                    subtitle = "Pagos activos",
                    modifier = Modifier.weight(1f)
                )
            }

            PremiumProgressLine(
                title = "Avance de recaudación",
                progress = progress,
                leftText = formatMoney(collected, currencySymbol),
                rightText = "Pendiente: ${formatMoney(pending, currencySymbol)}"
            )
        }
    }
}

@Composable
private fun PremiumMiniMetric(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AppColors.Background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Divider
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                color = AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PremiumProgressLine(
    title: String,
    progress: Float,
    leftText: String,
    rightText: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppColors.Divider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppColors.AccentTeal)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = leftText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )

            Text(
                text = rightText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PremiumClientSnapshotCard(
    currencySymbol: String
) {
    val rows = buildPremiumClientSnapshots()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Clientes activos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Vista rápida de clientes con cartera activa, saldo pendiente y próximo cobro.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            if (rows.isEmpty()) {
                Text(
                    text = "No hay clientes activos con cartera vigente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            } else {
                rows.forEach { row ->
                    PremiumClientSnapshotRow(
                        row = row,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumClientSnapshotRow(
    row: PremiumClientSnapshot,
    currencySymbol: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientAvatar(
                fullName = row.clientName,
                size = 42.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = row.clientName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Pendiente: ${formatMoney(row.pendingAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )

                Text(
                    text = "Próximo cobro: ${row.nextDueText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (row.overdueAmount > 0.0) AppColors.Error else AppColors.Gray600
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = formatMoney(row.overdueAmount, currencySymbol),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (row.overdueAmount > 0.0) AppColors.Error else AppColors.Success
                )

                Text(
                    text = if (row.overdueAmount > 0.0) "Vencido" else "Al día",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (row.overdueAmount > 0.0) AppColors.Error else AppColors.Success
                )
            }
        }
    }
}

private data class PremiumClientSnapshot(
    val clientName: String,
    val pendingAmount: Double,
    val overdueAmount: Double,
    val nextDueText: String
)

private fun buildPremiumClientSnapshots(): List<PremiumClientSnapshot> {
    return LocalClientRepository
        .getClients()
        .filter { client -> FinancialOperationRules.canShowClientInOperationalLists(client) }
        .mapNotNull { client ->
            val loans = LocalLoanRepository
                .getLoansByClient(client.id)
                .filter { loan -> loan.status.name == "ACTIVE" }

            if (loans.isEmpty()) {
                return@mapNotNull null
            }

            val totalExpected = loans.sumOf { loan -> loan.totalExpectedAmount }
            val totalPaid = loans.sumOf { loan ->
                LocalPaymentRepository.getTotalPaidByLoan(loan.id)
            }
            val pending = max(totalExpected - totalPaid, 0.0)

            val installments = loans.flatMap { loan ->
                LocalInstallmentRepository.getInstallmentsByLoan(loan.id)
            }

            val overdueAmount = installments
                .filter { installment -> installment.status == InstallmentStatus.OVERDUE }
                .sumOf { installment -> installment.pendingAmount }

            val nextDue = installments
                .filter { installment ->
                    installment.status != InstallmentStatus.PAID &&
                        installment.status != InstallmentStatus.CANCELLED
                }
                .minByOrNull { installment -> installment.dueDateMillis }

            PremiumClientSnapshot(
                clientName = client.fullName,
                pendingAmount = pending,
                overdueAmount = overdueAmount,
                nextDueText = nextDue?.let { installment -> formatDashboardDate(installment.dueDateMillis) } ?: "Sin cuota pendiente"
            )
        }
        .filter { row -> row.pendingAmount > 0.0 || row.overdueAmount > 0.0 }
        .sortedWith(
            compareByDescending<PremiumClientSnapshot> { row -> row.overdueAmount }
                .thenByDescending { row -> row.pendingAmount }
        )
        .take(6)
}


@Composable
private fun PremiumOperationalStatsCard(
    currencySymbol: String
) {
    val stats = buildPremiumDashboardStats(currencySymbol)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "Movimiento rápido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Cobros reales por día, semana y mes. Ideal para saber cómo va la operación.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PremiumDashboardStatPill(
                    title = "Cobrado hoy",
                    value = formatMoney(stats.collectedToday, currencySymbol),
                    subtitle = "${stats.dueTodayInstallments} cuotas para hoy",
                    modifier = Modifier.weight(1f)
                )

                PremiumDashboardStatPill(
                    title = "Esta semana",
                    value = formatMoney(stats.collectedWeek, currencySymbol),
                    subtitle = "Promedio: ${formatMoney(stats.dailyAverage, currencySymbol)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PremiumDashboardStatPill(
                    title = "Este mes",
                    value = formatMoney(stats.collectedMonth, currencySymbol),
                    subtitle = "Pagos activos",
                    modifier = Modifier.weight(1f)
                )

                PremiumDashboardStatPill(
                    title = "Cobro pendiente",
                    value = formatMoney(stats.todayDueAmount + stats.overdueAmount, currencySymbol),
                    subtitle = "Hoy + vencido",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumPortfolioHealthPanel(
    currencySymbol: String
) {
    val stats = buildPremiumDashboardStats(currencySymbol)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Salud de cartera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = stats.healthDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }

                Text(
                    text = stats.healthLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = stats.healthColor
                )
            }

            PremiumDashboardProgressBar(
                title = "Recaudación frente a riesgo",
                progress = stats.healthProgress,
                color = stats.healthColor,
                leftText = "Recaudado: ${stats.collectionRateText}",
                rightText = "Mora: ${formatMoney(stats.overdueAmount, currencySymbol)}"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PremiumDashboardStatPill(
                    title = "Clientes en mora",
                    value = stats.clientsInMora.toString(),
                    subtitle = "${stats.overdueInstallments} cuotas vencidas",
                    modifier = Modifier.weight(1f),
                    danger = stats.clientsInMora > 0
                )

                PremiumDashboardStatPill(
                    title = "Próximos a cerrar",
                    value = stats.closingSoonLoans.toString(),
                    subtitle = "Préstamos sobre 90%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumCollectionAlertsPanel(
    currencySymbol: String
) {
    val stats = buildPremiumDashboardStats(currencySymbol)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Alertas premium",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Prioridades reales de cobranza y actividad reciente.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            PremiumAlertLine(
                title = if (stats.todayDueAmount > 0.0) "Cobros de hoy" else "Sin cobros fuertes hoy",
                description = if (stats.todayDueAmount > 0.0) {
                    "${stats.dueTodayInstallments} cuotas por ${formatMoney(stats.todayDueAmount, currencySymbol)}."
                } else {
                    "No hay cuotas pendientes para cobrar hoy."
                },
                danger = false
            )

            PremiumAlertLine(
                title = if (stats.overdueAmount > 0.0) "Mora activa" else "Cartera sin mora crítica",
                description = if (stats.overdueAmount > 0.0) {
                    "${stats.overdueInstallments} cuotas vencidas por ${formatMoney(stats.overdueAmount, currencySymbol)}."
                } else {
                    "No hay cuotas vencidas activas en este momento."
                },
                danger = stats.overdueAmount > 0.0
            )

            PremiumAlertLine(
                title = if (stats.closingSoonLoans > 0) "Préstamos próximos a cerrar" else "Sin cierres próximos",
                description = if (stats.closingSoonLoans > 0) {
                    "${stats.closingSoonLoans} préstamos están sobre el 90% de recaudación."
                } else {
                    "Aún no hay préstamos cercanos a completarse."
                },
                danger = false
            )

            if (stats.recentPaymentLines.isNotEmpty()) {
                stats.recentPaymentLines.forEach { line ->
                    PremiumAlertLine(
                        title = "Pago reciente",
                        description = line,
                        danger = false
                    )
                }
            } else {
                PremiumAlertLine(
                    title = "Sin pagos recientes",
                    description = "No hay pagos activos registrados recientemente.",
                    danger = false
                )
            }
        }
    }
}

@Composable
private fun PremiumDashboardStatPill(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = AppColors.Background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (danger) AppColors.Error.copy(alpha = 0.45f) else AppColors.Divider
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                color = if (danger) AppColors.Error else AppColors.Gray900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PremiumDashboardProgressBar(
    title: String,
    progress: Float,
    color: Color,
    leftText: String,
    rightText: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppColors.Divider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = leftText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )

            Text(
                text = rightText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun PremiumAlertLine(
    title: String,
    description: String,
    danger: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (danger) AppColors.Error.copy(alpha = 0.08f) else AppColors.Background,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (danger) AppColors.Error.copy(alpha = 0.35f) else AppColors.Divider
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (danger) AppColors.Error else AppColors.Gray900
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }
    }
}

private data class PremiumDashboardStats(
    val collectedToday: Double,
    val collectedWeek: Double,
    val collectedMonth: Double,
    val dailyAverage: Double,
    val todayDueAmount: Double,
    val overdueAmount: Double,
    val dueTodayInstallments: Int,
    val overdueInstallments: Int,
    val clientsInMora: Int,
    val activeClients: Int,
    val activeLoans: Int,
    val closingSoonLoans: Int,
    val collectionRateText: String,
    val healthLabel: String,
    val healthDescription: String,
    val healthProgress: Float,
    val healthColor: Color,
    val recentPaymentLines: List<String>
)

private fun buildPremiumDashboardStats(
    currencySymbol: String
): PremiumDashboardStats {
    val todayStart = startOfTodayMillis()
    val todayEnd = endOfTodayMillis()
    val weekStart = startOfCurrentWeekMillis()
    val monthStart = startOfCurrentMonthMillis()
    val sevenDaysAgo = todayStart - (6L * 24L * 60L * 60L * 1000L)

    val activeClients = LocalClientRepository
        .getClients()
        .filter { client -> FinancialOperationRules.canShowClientInOperationalLists(client) }

    val activeLoans = LocalLoanRepository
        .getAllLoans()
        .filter { loan -> loan.status.name == "ACTIVE" }

    val activePayments = LocalPaymentRepository
        .getAllPayments()
        .filter { payment -> FinancialOperationRules.shouldCountPaymentFinancially(payment) }

    val activeLoanIds = activeLoans.map { loan -> loan.id }.toSet()

    val operationalPayments = activePayments
        .filter { payment -> payment.loanId in activeLoanIds }

    val collectedToday = operationalPayments
        .filter { payment -> payment.createdAtMillis in todayStart..todayEnd }
        .sumOf { payment -> payment.amount }

    val collectedWeek = operationalPayments
        .filter { payment -> payment.createdAtMillis >= weekStart }
        .sumOf { payment -> payment.amount }

    val collectedMonth = operationalPayments
        .filter { payment -> payment.createdAtMillis >= monthStart }
        .sumOf { payment -> payment.amount }

    val lastSevenCollected = operationalPayments
        .filter { payment -> payment.createdAtMillis >= sevenDaysAgo }
        .sumOf { payment -> payment.amount }

    val dailyAverage = lastSevenCollected / 7.0

    val installments = activeLoans.flatMap { loan ->
        LocalInstallmentRepository.getInstallmentsByLoan(loan.id)
    }

    val dueToday = installments.filter { installment ->
        installment.dueDateMillis in todayStart..todayEnd &&
            installment.status != InstallmentStatus.PAID &&
            installment.status != InstallmentStatus.CANCELLED
    }

    val overdue = installments.filter { installment ->
        installment.status == InstallmentStatus.OVERDUE
    }

    val todayDueAmount = dueToday.sumOf { installment -> installment.pendingAmount }
    val overdueAmount = overdue.sumOf { installment -> installment.pendingAmount }

    val overdueLoanIds = overdue.map { installment -> installment.loanId }.toSet()

    val clientsInMora = activeLoans
        .filter { loan -> loan.id in overdueLoanIds }
        .map { loan -> loan.clientId }
        .distinct()
        .size

    val portfolioTarget = activeLoans.sumOf { loan -> loan.totalExpectedAmount }
    val collected = activeLoans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }

    val collectionRate = if (portfolioTarget > 0.0) {
        (collected / portfolioTarget).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    val overdueRatio = if (portfolioTarget > 0.0) {
        (overdueAmount / portfolioTarget).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    val healthProgress = (collectionRate - overdueRatio).coerceIn(0.0, 1.0).toFloat()

    val healthLabel = when {
        overdueAmount <= 0.0 && collectionRate >= 0.60 -> "Saludable"
        overdueAmount <= 0.0 -> "En observación"
        overdueRatio < 0.10 -> "Riesgo moderado"
        else -> "Riesgo alto"
    }

    val healthDescription = when (healthLabel) {
        "Saludable" -> "La cartera mantiene buena recaudación y no muestra mora activa."
        "En observación" -> "No hay mora fuerte, pero todavía falta aumentar la recaudación."
        "Riesgo moderado" -> "Hay mora controlada. Conviene priorizar cobros vencidos."
        else -> "La mora pesa demasiado sobre la cartera. Requiere atención inmediata."
    }

    val healthColor = when (healthLabel) {
        "Saludable" -> AppColors.Success
        "En observación" -> AppColors.Warning
        "Riesgo moderado" -> AppColors.Warning
        else -> AppColors.Error
    }

    val closingSoonLoans = activeLoans.count { loan ->
        val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
        val progress = if (loan.totalExpectedAmount > 0.0) {
            paid / loan.totalExpectedAmount
        } else {
            0.0
        }

        progress >= 0.90 && paid < loan.totalExpectedAmount
    }

    val recentPaymentLines = operationalPayments
        .sortedByDescending { payment -> payment.createdAtMillis }
        .take(3)
        .map { payment ->
            val loan = LocalLoanRepository.getLoanById(payment.loanId)
            val clientName = loan?.let { currentLoan ->
                LocalClientRepository.getClientById(currentLoan.clientId)?.fullName
            }.orEmpty().ifBlank { "Cliente no disponible" }

            "$clientName · ${formatMoney(payment.amount, currencySymbol)} · ${formatDashboardDate(payment.createdAtMillis)}"
        }

    return PremiumDashboardStats(
        collectedToday = collectedToday,
        collectedWeek = collectedWeek,
        collectedMonth = collectedMonth,
        dailyAverage = dailyAverage,
        todayDueAmount = todayDueAmount,
        overdueAmount = overdueAmount,
        dueTodayInstallments = dueToday.size,
        overdueInstallments = overdue.size,
        clientsInMora = clientsInMora,
        activeClients = activeClients.size,
        activeLoans = activeLoans.size,
        closingSoonLoans = closingSoonLoans,
        collectionRateText = formatPercent(collectionRate),
        healthLabel = healthLabel,
        healthDescription = healthDescription,
        healthProgress = healthProgress,
        healthColor = healthColor,
        recentPaymentLines = recentPaymentLines
    )
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


private fun formatDashboardDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
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







