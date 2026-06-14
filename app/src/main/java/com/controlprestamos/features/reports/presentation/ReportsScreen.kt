package com.controlprestamos.features.reports.presentation

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.documents.PdfShareUtils
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppDatePickerField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import com.controlprestamos.features.reports.domain.ReportPdfGenerator
import com.controlprestamos.features.reports.domain.ReportPdfLine
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    var selectedPeriod by remember {
        mutableStateOf(ReportPeriod.MONTH)
    }

    var customStartMillis by remember {
        mutableStateOf(startOfCurrentMonthMillis())
    }

    var customEndMillis by remember {
        mutableStateOf(endOfTodayMillis())
    }

    val state = remember(
        selectedPeriod,
        customStartMillis,
        customEndMillis,
        preferences.currencySymbol
    ) {
        buildReportsState(
            period = selectedPeriod,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis,
            currencySymbol = preferences.currencySymbol
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reportes",
                subtitle = "Resumen financiero y cartera",
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
                ExecutiveReportHeader(state = state)

                ReportFilterCard(
                    selectedPeriod = selectedPeriod,
                    customStartMillis = customStartMillis,
                    customEndMillis = customEndMillis,
                    onPeriodSelected = { selected ->
                        selectedPeriod = selected
                    },
                    onStartDateSelected = { selected ->
                        customStartMillis = selected

                        if (customStartMillis > customEndMillis) {
                            customEndMillis = selected
                        }
                    },
                    onEndDateSelected = { selected ->
                        customEndMillis = selected

                        if (customEndMillis < customStartMillis) {
                            customStartMillis = selected
                        }
                    }
                )

                SectionTitle(
                    title = "Resumen del período",
                    subtitle = "Pagos recibidos según el filtro seleccionado."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    ReportMetricCard(
                        title = "Cobrado",
                        value = state.collectedInPeriodFormatted,
                        subtitle = "${state.paymentsInPeriod} pagos",
                        modifier = Modifier.weight(1f),
                        strong = true
                    )

                    ReportMetricCard(
                        title = "Promedio",
                        value = state.averagePaymentFormatted,
                        subtitle = "Por pago",
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionTitle(
                    title = "Cartera general",
                    subtitle = "Vista operativa del dinero prestado, cobrado y pendiente. No incluye clientes archivados."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    ReportMetricCard(
                        title = "Prestado",
                        value = state.totalLentFormatted,
                        subtitle = "${state.totalLoans} préstamos",
                        modifier = Modifier.weight(1f)
                    )

                    ReportMetricCard(
                        title = "A cobrar",
                        value = state.totalExpectedFormatted,
                        subtitle = "Capital + interés",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    ReportMetricCard(
                        title = "Cobrado",
                        value = state.totalCollectedFormatted,
                        subtitle = "Histórico",
                        modifier = Modifier.weight(1f),
                        strong = true
                    )

                    ReportMetricCard(
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
                    ReportMetricCard(
                        title = "Vencido",
                        value = state.totalOverdueFormatted,
                        subtitle = "${state.overdueInstallments} cuotas",
                        modifier = Modifier.weight(1f),
                        danger = state.totalOverdue > 0.0
                    )

                    ReportMetricCard(
                        title = "Cobrar hoy",
                        value = state.todayDueFormatted,
                        subtitle = "${state.todayDueInstallments} cuotas",
                        modifier = Modifier.weight(1f),
                        warning = state.todayDue > 0.0
                    )
                }

                PortfolioIndicatorsCard(state = state)

                ShareReportCard(
                    context = context,
                    businessName = preferences.businessName,
                    state = state
                )
            }
        }
    }
}

@Composable
private fun ExecutiveReportHeader(
    state: AdvancedReportsState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Reporte de cartera",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = state.periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Text(
                text = state.collectedInPeriodFormatted,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentTeal
            )

            Text(
                text = "Cobrado en el período · ${state.paymentsInPeriod} pagos registrados",
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
                    value = state.totalOverdueFormatted,
                    modifier = Modifier.weight(1f),
                    danger = state.totalOverdue > 0.0
                )
            }
        }
    }
}

@Composable
private fun ReportFilterCard(
    selectedPeriod: ReportPeriod,
    customStartMillis: Long,
    customEndMillis: Long,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Filtro de período",
                subtitle = "Selecciona el rango que quieres analizar."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PeriodButton(
                    text = "Hoy",
                    selected = selectedPeriod == ReportPeriod.TODAY,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPeriodSelected(ReportPeriod.TODAY)
                    }
                )

                PeriodButton(
                    text = "Semana",
                    selected = selectedPeriod == ReportPeriod.WEEK,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPeriodSelected(ReportPeriod.WEEK)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PeriodButton(
                    text = "Mes",
                    selected = selectedPeriod == ReportPeriod.MONTH,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPeriodSelected(ReportPeriod.MONTH)
                    }
                )

                PeriodButton(
                    text = "General",
                    selected = selectedPeriod == ReportPeriod.ALL,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPeriodSelected(ReportPeriod.ALL)
                    }
                )
            }

            PeriodButton(
                text = "Personalizado",
                selected = selectedPeriod == ReportPeriod.CUSTOM,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onPeriodSelected(ReportPeriod.CUSTOM)
                }
            )

            if (selectedPeriod == ReportPeriod.CUSTOM) {
                AppDatePickerField(
                    label = "Fecha desde",
                    selectedDateMillis = customStartMillis,
                    onDateSelected = onStartDateSelected,
                    helperText = "El reporte incluirá pagos desde esta fecha."
                )

                AppDatePickerField(
                    label = "Fecha hasta",
                    selectedDateMillis = customEndMillis,
                    onDateSelected = onEndDateSelected,
                    helperText = "El reporte incluirá pagos hasta esta fecha."
                )
            }
        }
    }
}

@Composable
private fun PortfolioIndicatorsCard(
    state: AdvancedReportsState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Indicadores de cartera",
                subtitle = "Lectura rápida del estado general del negocio."
            )

            IndicatorRow("Clientes activos", state.totalClients.toString())
            IndicatorRow("Clientes con deuda", state.clientsWithDebt.toString())
            IndicatorRow("Préstamos activos", state.activeLoans.toString())
            IndicatorRow("Préstamos completados", state.completedLoans.toString())
            IndicatorRow("Cuotas pendientes/parciales", state.pendingInstallments.toString())
            IndicatorRow("Cuotas pagadas", state.paidInstallments.toString())
        }
    }
}

@Composable
private fun ShareReportCard(
    context: Context,
    businessName: String,
    state: AdvancedReportsState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Compartir reporte",
                subtitle = "Exporta el resumen como texto o como documento PDF."
            )

            PrimaryButton(
                text = "Compartir texto",
                onClick = {
                    shareReport(
                        context = context,
                        reportText = buildReportText(
                            businessName = businessName,
                            state = state
                        )
                    )
                }
            )

            SecondaryButton(
                text = "Compartir PDF",
                onClick = {
                    val file = ReportPdfGenerator.generate(
                        context = context,
                        title = businessName,
                        subtitle = "Reporte de cartera - ${state.periodLabel}",
                        lines = buildReportPdfLines(state)
                    )

                    PdfShareUtils.sharePdf(
                        context = context,
                        file = file,
                        chooserTitle = "Compartir reporte PDF"
                    )
                }
            )
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
private fun PeriodButton(
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

@Composable
private fun ReportMetricCard(
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

private enum class ReportPeriod(
    val label: String
) {
    TODAY("Hoy"),
    WEEK("Esta semana"),
    MONTH("Este mes"),
    ALL("General"),
    CUSTOM("Personalizado")
}

private data class AdvancedReportsState(
    val periodLabel: String,
    val currencySymbol: String,
    val totalClients: Int,
    val clientsWithDebt: Int,
    val totalLoans: Int,
    val activeLoans: Int,
    val completedLoans: Int,
    val totalLent: Double,
    val totalExpected: Double,
    val totalCollected: Double,
    val totalPending: Double,
    val totalOverdue: Double,
    val todayDue: Double,
    val collectedInPeriod: Double,
    val paymentsInPeriod: Int,
    val overdueInstallments: Int,
    val todayDueInstallments: Int,
    val pendingInstallments: Int,
    val paidInstallments: Int
) {
    val totalLentFormatted: String
        get() = formatMoney(totalLent, currencySymbol)

    val totalExpectedFormatted: String
        get() = formatMoney(totalExpected, currencySymbol)

    val totalCollectedFormatted: String
        get() = formatMoney(totalCollected, currencySymbol)

    val totalPendingFormatted: String
        get() = formatMoney(totalPending, currencySymbol)

    val totalOverdueFormatted: String
        get() = formatMoney(totalOverdue, currencySymbol)

    val todayDueFormatted: String
        get() = formatMoney(todayDue, currencySymbol)

    val collectedInPeriodFormatted: String
        get() = formatMoney(collectedInPeriod, currencySymbol)

    val averagePaymentFormatted: String
        get() = if (paymentsInPeriod <= 0) {
            formatMoney(0.0, currencySymbol)
        } else {
            formatMoney(collectedInPeriod / paymentsInPeriod, currencySymbol)
        }
}

private fun buildReportsState(
    period: ReportPeriod,
    customStartMillis: Long,
    customEndMillis: Long,
    currencySymbol: String
): AdvancedReportsState {
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

    val totalLent = loans.sumOf { it.principalAmount }
    val totalExpected = loans.sumOf { it.totalExpectedAmount }
    val totalCollected = payments.sumOf { it.amount }
    val totalPending = max(totalExpected - totalCollected, 0.0)

    val activeLoans = loans.count { loan ->
        max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0) > 0.0
    }

    val completedLoans = loans.count { loan ->
        max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0) <= 0.0
    }

    val clientsWithDebt = loans
        .groupBy { it.clientId }
        .count { (_, clientLoans) ->
            clientLoans.any { loan ->
                max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0) > 0.0
            }
        }

    val overdueInstallments = installments.filter {
        it.status == InstallmentStatus.OVERDUE
    }

    val todayDueInstallments = installments.filter {
        (it.status == InstallmentStatus.PENDING || it.status == InstallmentStatus.PARTIAL) &&
            isToday(it.dueDateMillis)
    }

    val periodRange = selectedPeriodRange(
        period = period,
        customStartMillis = customStartMillis,
        customEndMillis = customEndMillis
    )

    val paymentsInPeriod = payments.filter { payment ->
        periodRange == null || payment.createdAtMillis in periodRange.first..periodRange.second
    }

    return AdvancedReportsState(
        periodLabel = selectedPeriodLabel(
            period = period,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis
        ),
        currencySymbol = currencySymbol,
        totalClients = clients.size,
        clientsWithDebt = clientsWithDebt,
        totalLoans = loans.size,
        activeLoans = activeLoans,
        completedLoans = completedLoans,
        totalLent = totalLent,
        totalExpected = totalExpected,
        totalCollected = totalCollected,
        totalPending = totalPending,
        totalOverdue = overdueInstallments.sumOf { it.pendingAmount },
        todayDue = todayDueInstallments.sumOf { it.pendingAmount },
        collectedInPeriod = paymentsInPeriod.sumOf { it.amount },
        paymentsInPeriod = paymentsInPeriod.size,
        overdueInstallments = overdueInstallments.size,
        todayDueInstallments = todayDueInstallments.size,
        pendingInstallments = installments.count {
            it.status == InstallmentStatus.PENDING || it.status == InstallmentStatus.PARTIAL
        },
        paidInstallments = installments.count {
            it.status == InstallmentStatus.PAID
        }
    )
}

private fun buildReportText(
    businessName: String,
    state: AdvancedReportsState
): String {
    return """
$businessName

REPORTE OPERATIVO DE CARTERA
Periodo: ${state.periodLabel}
Fecha de generación: ${formatDate(System.currentTimeMillis())}

RESUMEN DEL PERÍODO
Cobrado: ${state.collectedInPeriodFormatted}
Pagos: ${state.paymentsInPeriod}
Promedio por pago: ${state.averagePaymentFormatted}

RESUMEN GENERAL
Total prestado: ${state.totalLentFormatted}
Total a cobrar: ${state.totalExpectedFormatted}
Total cobrado: ${state.totalCollectedFormatted}
Total pendiente: ${state.totalPendingFormatted}
Total vencido: ${state.totalOverdueFormatted}
Cobrar hoy: ${state.todayDueFormatted}

INDICADORES
Clientes activos: ${state.totalClients}
Clientes con deuda: ${state.clientsWithDebt}
Préstamos activos: ${state.activeLoans}
Préstamos completados: ${state.completedLoans}
Cuotas vencidas: ${state.overdueInstallments}
Cuotas pendientes/parciales: ${state.pendingInstallments}
Cuotas pagadas: ${state.paidInstallments}

Generado desde Control Préstamos.
""".trimIndent()
}

private fun buildReportPdfLines(
    state: AdvancedReportsState
): List<ReportPdfLine> {
    return listOf(
        ReportPdfLine("Periodo", state.periodLabel),
        ReportPdfLine("Fecha de generación", formatDate(System.currentTimeMillis())),
        ReportPdfLine("Cobrado periodo", state.collectedInPeriodFormatted),
        ReportPdfLine("Pagos periodo", state.paymentsInPeriod.toString()),
        ReportPdfLine("Promedio por pago", state.averagePaymentFormatted),
        ReportPdfLine("Total prestado", state.totalLentFormatted),
        ReportPdfLine("Total a cobrar", state.totalExpectedFormatted),
        ReportPdfLine("Total cobrado", state.totalCollectedFormatted),
        ReportPdfLine("Total pendiente", state.totalPendingFormatted),
        ReportPdfLine("Total vencido", state.totalOverdueFormatted),
        ReportPdfLine("Cobrar hoy", state.todayDueFormatted),
        ReportPdfLine("Clientes activos", state.totalClients.toString()),
        ReportPdfLine("Clientes con deuda", state.clientsWithDebt.toString()),
        ReportPdfLine("Préstamos activos", state.activeLoans.toString()),
        ReportPdfLine("Préstamos completados", state.completedLoans.toString()),
        ReportPdfLine("Cuotas vencidas", state.overdueInstallments.toString()),
        ReportPdfLine("Cuotas pendientes/parciales", state.pendingInstallments.toString()),
        ReportPdfLine("Cuotas pagadas", state.paidInstallments.toString())
    )
}

private fun shareReport(
    context: Context,
    reportText: String
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte Control Préstamos")
        putExtra(Intent.EXTRA_TEXT, reportText)
    }

    val chooser = Intent.createChooser(sendIntent, "Compartir reporte").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(chooser)
}

private fun selectedPeriodRange(
    period: ReportPeriod,
    customStartMillis: Long,
    customEndMillis: Long
): Pair<Long, Long>? {
    return when (period) {
        ReportPeriod.TODAY -> startOfTodayMillis() to endOfTodayMillis()
        ReportPeriod.WEEK -> startOfCurrentWeekMillis() to endOfTodayMillis()
        ReportPeriod.MONTH -> startOfCurrentMonthMillis() to endOfTodayMillis()
        ReportPeriod.ALL -> null
        ReportPeriod.CUSTOM -> {
            val start = startOfDayMillis(customStartMillis)
            val end = endOfDayMillis(customEndMillis)

            if (start <= end) {
                start to end
            } else {
                end to start
            }
        }
    }
}

private fun selectedPeriodLabel(
    period: ReportPeriod,
    customStartMillis: Long,
    customEndMillis: Long
): String {
    return when (period) {
        ReportPeriod.CUSTOM -> {
            val start = startOfDayMillis(customStartMillis)
            val end = endOfDayMillis(customEndMillis)

            if (start <= end) {
                "Personalizado: ${formatDateOnly(start)} - ${formatDateOnly(end)}"
            } else {
                "Personalizado: ${formatDateOnly(end)} - ${formatDateOnly(start)}"
            }
        }

        else -> period.label
    }
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

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(millis))
}

private fun formatDateOnly(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}


