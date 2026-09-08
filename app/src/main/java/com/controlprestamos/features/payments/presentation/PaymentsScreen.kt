package com.controlprestamos.features.payments.presentation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
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
private enum class PaymentsFilter {
    TODAY,
    PENDING,
    OVERDUE,
    COLLECTED,
    HISTORY
}
@Composable
fun PaymentsScreen(
    onNavigateBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenMore: () -> Unit,
    onRegisterPayment: (String) -> Unit,
    onOpenPayments: () -> Unit = {}
) {
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )
    var selectedFilter by rememberSaveable {
        mutableStateOf(PaymentsFilter.TODAY.name)
    }
    val filter = runCatching {
        PaymentsFilter.valueOf(selectedFilter)
    }.getOrDefault(PaymentsFilter.TODAY)
    val state = buildPaymentsState(
        currencySymbol = preferences.currencySymbol
    )
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pagos",
                subtitle = "Centro de cobros",
                showBack = false,
                showMore = false,
                showMenu = false,
                showNotifications = false,
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
                PaymentsSummaryCard(state = state)
                PaymentsPriorityCard(state = state)
                PaymentsFilterCard(
                    selectedFilter = filter,
                    onFilterChange = { selectedFilter = it.name }
                )
                when (filter) {
                    PaymentsFilter.TODAY -> {
                        InstallmentsCollectionCard(
                            title = "Cobros de hoy",
                            subtitle = "Vencidas y cuotas de hoy",
                            rows = state.todayRows,
                            emptyTitle = "Sin cobros urgentes",
                            emptyDescription = "No hay cuotas vencidas ni cuotas para hoy.",
                            onRegisterPayment = onRegisterPayment
                        )
                    }
                    PaymentsFilter.PENDING -> {
                        InstallmentsCollectionCard(
                            title = "Cuotas pendientes",
                            subtitle = "Cartera por cobrar",
                            rows = state.pendingRows,
                            emptyTitle = "Sin cuotas pendientes",
                            emptyDescription = "Las cuotas pendientes aparecerán aquí.",
                            onRegisterPayment = onRegisterPayment
                        )
                    }
                    PaymentsFilter.OVERDUE -> {
                        InstallmentsCollectionCard(
                            title = "Cuotas vencidas",
                            subtitle = "Prioridad de cobranza",
                            rows = state.overdueRows,
                            emptyTitle = "Sin cuotas vencidas",
                            emptyDescription = "No hay cuotas vencidas en este momento.",
                            onRegisterPayment = onRegisterPayment
                        )
                    }
                    PaymentsFilter.COLLECTED -> {
                        PaymentsHistoryCard(
                            title = "Cobrado hoy",
                            subtitle = "Pagos recibidos en el día",
                            rows = state.collectedTodayRows,
                            emptyTitle = "Sin pagos hoy",
                            emptyDescription = "Los pagos recibidos hoy aparecerán aquí."
                        )
                    }
                    PaymentsFilter.HISTORY -> {
                        PaymentsHistoryCard(
                            title = "Historial de pagos",
                            subtitle = "Últimos movimientos",
                            rows = state.historyRows,
                            emptyTitle = "Sin historial",
                            emptyDescription = "Cuando registres pagos aparecerán aquí."
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}
@Composable
private fun PaymentsSummaryCard(
    state: PaymentsState
) {
    ReferenceCard {
        Text(
            text = "Resumen de cobros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentMiniStat(
                title = "Cobrar hoy",
                value = state.todayAmountText,
                subtitle = "${state.todayRows.size} cuotas",
                color = if (state.todayAmount > 0.0) AppColors.Warning else AppColors.Success,
                modifier = Modifier.weight(1f)
            )
            PaymentMiniStat(
                title = "Vencido",
                value = state.overdueAmountText,
                subtitle = "${state.overdueRows.size} cuotas",
                color = if (state.overdueAmount > 0.0) AppColors.Error else AppColors.Success,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentMiniStat(
                title = "Cobrado hoy",
                value = state.collectedTodayAmountText,
                subtitle = "${state.collectedTodayRows.size} pagos",
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
            PaymentMiniStat(
                title = "Pendiente",
                value = state.pendingAmountText,
                subtitle = "${state.pendingRows.size} cuotas",
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
private fun PaymentMiniStat(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
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
private fun PaymentsPriorityCard(
    state: PaymentsState
) {
    val color = when {
        state.overdueRows.isNotEmpty() -> AppColors.Error
        state.todayRows.isNotEmpty() -> AppColors.Warning
        else -> AppColors.Success
    }
    val title = when {
        state.overdueRows.isNotEmpty() -> "Prioridad alta"
        state.todayRows.isNotEmpty() -> "Cobranza de hoy"
        else -> "Cartera al día"
    }
    val message = when {
        state.overdueRows.isNotEmpty() -> {
            "${state.overdueRows.size} cuotas vencidas requieren atención."
        }
        state.todayRows.isNotEmpty() -> {
            "${state.todayRows.size} cuotas deben cobrarse hoy."
        }
        else -> {
            "No hay cuotas vencidas ni cobros urgentes."
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.cardLarge),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
private fun PaymentsFilterCard(
    selectedFilter: PaymentsFilter,
    onFilterChange: (PaymentsFilter) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Vista de cobros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentFilterChip(
                text = "Hoy",
                selected = selectedFilter == PaymentsFilter.TODAY,
                onClick = { onFilterChange(PaymentsFilter.TODAY) },
                modifier = Modifier.weight(1f)
            )
            PaymentFilterChip(
                text = "Pendientes",
                selected = selectedFilter == PaymentsFilter.PENDING,
                onClick = { onFilterChange(PaymentsFilter.PENDING) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PaymentFilterChip(
                text = "Vencidas",
                selected = selectedFilter == PaymentsFilter.OVERDUE,
                onClick = { onFilterChange(PaymentsFilter.OVERDUE) },
                modifier = Modifier.weight(1f)
            )
            PaymentFilterChip(
                text = "Cobradas",
                selected = selectedFilter == PaymentsFilter.COLLECTED,
                onClick = { onFilterChange(PaymentsFilter.COLLECTED) },
                modifier = Modifier.weight(1f)
            )
            PaymentFilterChip(
                text = "Historial",
                selected = selectedFilter == PaymentsFilter.HISTORY,
                onClick = { onFilterChange(PaymentsFilter.HISTORY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
private fun PaymentFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) AppColors.AccentTeal else AppColors.Gray500
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .clickable { onClick() },
        color = if (selected) color.copy(alpha = 0.12f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.42f) else AppColors.Border
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
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
private fun InstallmentsCollectionCard(
    title: String,
    subtitle: String,
    rows: List<CollectionRow>,
    emptyTitle: String,
    emptyDescription: String,
    onRegisterPayment: (String) -> Unit,
    onOpenPayments: () -> Unit = {}
) {
    ReferenceCard {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            count = rows.size,
            color = AppColors.AccentTeal
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        if (rows.isEmpty()) {
            EmptyCompactState(
                title = emptyTitle,
                description = emptyDescription
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                rows.forEach { row ->
                    CollectionReferenceRow(
                        row = row,
                        onClick = { onRegisterPayment(row.loanId) }
                    )
                }
            }
        }
    }
}
@Composable
private fun CollectionReferenceRow(
    row: CollectionRow,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable { onClick() }
            .background(AppColors.SurfaceMuted)
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientAvatar(
                fullName = row.clientName,
                size = 42.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = row.clientName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )
                Text(
                    text = row.loanLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }
            PaymentStatusPill(
                text = row.statusText,
                color = row.statusColor
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = row.installmentText,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )
            Text(
                text = row.pendingText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (row.pendingAmount > 0.0) AppColors.Warning else AppColors.Success
            )
        }
        Text(
            text = row.dueDateText,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray500
        )
        ProgressBar(progress = row.progress)
    }
}
@Composable
private fun PaymentsHistoryCard(
    title: String,
    subtitle: String,
    rows: List<PaymentHistoryRow>,
    emptyTitle: String,
    emptyDescription: String
) {
    ReferenceCard {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            count = rows.size,
            color = AppColors.Success
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        if (rows.isEmpty()) {
            EmptyCompactState(
                title = emptyTitle,
                description = emptyDescription
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                rows.forEach { row ->
                    PaymentHistoryReferenceRow(row = row)
                }
            }
        }
    }
}
@Composable
private fun PaymentHistoryReferenceRow(
    row: PaymentHistoryRow
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppColors.SurfaceMuted)
            .padding(AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(row.statusColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = row.iconText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = row.statusColor
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = row.clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = row.amountText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = row.statusColor
            )
            Text(
                text = row.dateText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )
        }
    }
}
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
@Composable
private fun EmptyCompactState(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )
    }
}
@Composable
private fun PaymentStatusPill(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(AppRadius.pill),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.28f)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = AppSpacing.sm,
                vertical = AppSpacing.xs
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
@Composable
private fun ProgressBar(
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(AppRadius.pill))
            .background(AppColors.Gray100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(AppColors.AccentTeal)
        )
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
private data class PaymentsState(
    val todayRows: List<CollectionRow>,
    val pendingRows: List<CollectionRow>,
    val overdueRows: List<CollectionRow>,
    val collectedTodayRows: List<PaymentHistoryRow>,
    val historyRows: List<PaymentHistoryRow>,
    val todayAmount: Double,
    val overdueAmount: Double,
    val pendingAmount: Double,
    val collectedTodayAmount: Double,
    val todayAmountText: String,
    val overdueAmountText: String,
    val pendingAmountText: String,
    val collectedTodayAmountText: String
)
private data class CollectionRow(
    val loanId: String,
    val clientName: String,
    val loanLabel: String,
    val installmentText: String,
    val pendingText: String,
    val pendingAmount: Double,
    val dueDateText: String,
    val progress: Float,
    val statusText: String,
    val statusColor: Color
)
private data class PaymentHistoryRow(
    val clientName: String,
    val subtitle: String,
    val amountText: String,
    val dateText: String,
    val iconText: String,
    val statusColor: Color
)
private fun buildPaymentsState(
    currencySymbol: String
): PaymentsState {
    val allInstallments = LocalInstallmentRepository
        .getAllInstallments()
        .filter { installment -> installment.status != InstallmentStatus.CANCELLED }
    val activeCollectionRows = allInstallments
        .filter { installment ->
            installment.status == InstallmentStatus.PENDING ||
                installment.status == InstallmentStatus.PARTIAL ||
                installment.status == InstallmentStatus.OVERDUE
        }
        .map { installment ->
            buildCollectionRow(
                installment = installment,
                currencySymbol = currencySymbol
            )
        }
        .sortedWith(
            compareBy<CollectionRow> { row ->
                when (row.statusText) {
                    "Vencida" -> 0
                    "Parcial" -> 1
                    else -> 2
                }
            }.thenBy { row -> row.dueDateText }
        )
    val todayRows = activeCollectionRows.filter { row ->
        row.statusText == "Vencida" || isToday(rowRawDateFromText = row.dueDateText)
    }
    val overdueRows = activeCollectionRows.filter { row ->
        row.statusText == "Vencida"
    }
    val pendingRows = activeCollectionRows
    val allHistory = LocalPaymentRepository
        .getAllPaymentHistory()
        .map { payment ->
            buildPaymentHistoryRow(
                payment = payment,
                currencySymbol = currencySymbol
            )
        }
    val collectedTodayRows = allHistory.filter { row ->
        row.iconText == "✓" && isToday(rowRawDateFromText = row.dateText)
    }
    val todayAmount = todayRows.sumOf { row -> row.pendingAmount }
    val overdueAmount = overdueRows.sumOf { row -> row.pendingAmount }
    val pendingAmount = pendingRows.sumOf { row -> row.pendingAmount }
    val collectedTodayAmount = LocalPaymentRepository
        .getAllPayments()
        .filter { payment -> isToday(payment.createdAtMillis) }
        .sumOf { payment -> payment.amount }
    return PaymentsState(
        todayRows = todayRows,
        pendingRows = pendingRows,
        overdueRows = overdueRows,
        collectedTodayRows = collectedTodayRows,
        historyRows = allHistory.take(30),
        todayAmount = todayAmount,
        overdueAmount = overdueAmount,
        pendingAmount = pendingAmount,
        collectedTodayAmount = collectedTodayAmount,
        todayAmountText = formatMoney(todayAmount, currencySymbol),
        overdueAmountText = formatMoney(overdueAmount, currencySymbol),
        pendingAmountText = formatMoney(pendingAmount, currencySymbol),
        collectedTodayAmountText = formatMoney(collectedTodayAmount, currencySymbol)
    )
}
private fun buildCollectionRow(
    installment: Installment,
    currencySymbol: String
): CollectionRow {
    val loan = LocalLoanRepository.getLoanById(installment.loanId)
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }
    val paid = installment.paidAmount
    val expected = installment.expectedAmount
    val pending = max(installment.pendingAmount, 0.0)
    val progress = if (expected > 0.0) {
        (paid / expected).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val statusText = when (installment.status) {
        InstallmentStatus.PENDING -> "Pendiente"
        InstallmentStatus.PARTIAL -> "Parcial"
        InstallmentStatus.PAID -> "Pagada"
        InstallmentStatus.OVERDUE -> "Vencida"
        InstallmentStatus.CANCELLED -> "Cancelada"
    }
    val statusColor = when (installment.status) {
        InstallmentStatus.PENDING -> AppColors.Warning
        InstallmentStatus.PARTIAL -> AppColors.AccentTeal
        InstallmentStatus.PAID -> AppColors.Success
        InstallmentStatus.OVERDUE -> AppColors.Error
        InstallmentStatus.CANCELLED -> AppColors.Gray500
    }
    return CollectionRow(
        loanId = installment.loanId,
        clientName = client?.fullName.orEmpty().ifBlank { "Cliente" },
        loanLabel = loan?.description.orEmpty().ifBlank { "Préstamo #${installment.loanId.takeLast(4)}" },
        installmentText = "Cuota ${installment.number}",
        pendingText = formatMoney(pending, currencySymbol),
        pendingAmount = pending,
        dueDateText = formatDate(installment.dueDateMillis),
        progress = progress,
        statusText = statusText,
        statusColor = statusColor
    )
}
private fun buildPaymentHistoryRow(
    payment: Payment,
    currencySymbol: String
): PaymentHistoryRow {
    val loan = LocalLoanRepository.getLoanById(payment.loanId)
    val client = loan?.let { LocalClientRepository.getClientById(it.clientId) }
    val active = payment.status == PaymentStatus.ACTIVE
    val subtitle = buildString {
        append(loan?.description?.ifBlank { "Préstamo" } ?: "Préstamo")
        if (payment.method.isNotBlank()) {
            append(" · ")
            append(payment.method)
        }
    }
    return PaymentHistoryRow(
        clientName = client?.fullName.orEmpty().ifBlank { "Cliente" },
        subtitle = subtitle,
        amountText = formatMoney(payment.amount, currencySymbol),
        dateText = formatDate(payment.createdAtMillis),
        iconText = if (active) "✓" else "!",
        statusColor = if (active) AppColors.Success else AppColors.Error
    )
}
private fun isToday(millis: Long): Boolean {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        timeInMillis = millis
    }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}
private fun isToday(rowRawDateFromText: String): Boolean {
    return runCatching {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(rowRawDateFromText)
        date != null && isToday(date.time)
    }.getOrDefault(false)
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

