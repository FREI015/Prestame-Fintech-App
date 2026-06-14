package com.controlprestamos.features.loans.presentation

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
import com.controlprestamos.core.ui.components.AppTextField
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
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun LoansScreen(
    onNavigateBack: () -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenLoans: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
    onOpenMore: () -> Unit = {},
    onCreateLoan: () -> Unit = {},
    onCreateLoanFromClients: () -> Unit = onCreateLoan,
    onOpenLoanDetail: (String) -> Unit = {},
    onLoanClick: (String) -> Unit = onOpenLoanDetail,
    onOpenLoan: (String) -> Unit = onLoanClick
) {
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(LoanListFilter.ACTIVE.name) }

    val filter = runCatching {
        LoanListFilter.valueOf(selectedFilter)
    }.getOrDefault(LoanListFilter.ACTIVE)

    val allLoans = LocalLoanRepository.getAllLoans()

    val rows = allLoans
        .map { loan ->
            buildLoanListRow(
                loan = loan,
                currencySymbol = preferences.currencySymbol
            )
        }
        .filter { row ->
            when (filter) {
                LoanListFilter.ACTIVE -> row.status == LoanListStatus.ACTIVE
                LoanListFilter.OVERDUE -> row.status == LoanListStatus.OVERDUE
                LoanListFilter.PAID -> row.status == LoanListStatus.PAID
                LoanListFilter.CANCELLED -> row.status == LoanListStatus.CANCELLED
                LoanListFilter.ALL -> true
            }
        }
        .filter { row ->
            val query = searchQuery.trim()

            query.isBlank() ||
                row.clientName.contains(query, ignoreCase = true) ||
                row.label.contains(query, ignoreCase = true) ||
                row.statusText.contains(query, ignoreCase = true)
        }
        .sortedWith(
            compareBy<LoanListRow> { row ->
                when (row.status) {
                    LoanListStatus.OVERDUE -> 0
                    LoanListStatus.ACTIVE -> 1
                    LoanListStatus.PAID -> 2
                    LoanListStatus.CANCELLED -> 3
                }
            }.thenByDescending { row -> row.pendingAmount }
        )

    val summary = buildLoansSummary(
        loans = allLoans,
        currencySymbol = preferences.currencySymbol
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Préstamos",
                subtitle = "Cartera colocada",
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
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                LoansSummaryCard(summary = summary)

                LoansSearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedFilter = filter,
                    onFilterChange = { selectedFilter = it.name },
                    onCreateLoan = onCreateLoanFromClients
                )

                LoansListCard(
                    rows = rows,
                    filter = filter,
                    searchQuery = searchQuery,
                    onCreateLoan = onCreateLoanFromClients,
                    onClearSearch = { searchQuery = "" },
                    onOpenLoan = onOpenLoan
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun LoansSummaryCard(
    summary: LoansSummary
) {
    ReferenceCard {
        Text(
            text = "Resumen de cartera",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoansMiniStat(
                title = "Activos",
                value = summary.activeLoans.toString(),
                subtitle = summary.activeCapitalText,
                color = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )

            LoansMiniStat(
                title = "Vencidos",
                value = summary.overdueLoans.toString(),
                subtitle = summary.overdueAmountText,
                color = AppColors.Error,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            LoansMiniStat(
                title = "A recaudar",
                value = summary.expectedText,
                subtitle = "Total",
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            LoansMiniStat(
                title = "Pendiente",
                value = summary.pendingText,
                subtitle = summary.progressText,
                color = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoansMiniStat(
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
private fun LoansSearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: LoanListFilter,
    onFilterChange: (LoanListFilter) -> Unit,
    onCreateLoan: () -> Unit
) {
    ReferenceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = "Buscar préstamo",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CompactAddLoanButton(
                onClick = onCreateLoan
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                LoanFilterChip(
                    text = "Activos",
                    selected = selectedFilter == LoanListFilter.ACTIVE,
                    onClick = { onFilterChange(LoanListFilter.ACTIVE) },
                    modifier = Modifier.weight(1f)
                )

                LoanFilterChip(
                    text = "Vencidos",
                    selected = selectedFilter == LoanListFilter.OVERDUE,
                    onClick = { onFilterChange(LoanListFilter.OVERDUE) },
                    modifier = Modifier.weight(1f)
                )

                LoanFilterChip(
                    text = "Pagados",
                    selected = selectedFilter == LoanListFilter.PAID,
                    onClick = { onFilterChange(LoanListFilter.PAID) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                LoanFilterChip(
                    text = "Cancelados",
                    selected = selectedFilter == LoanListFilter.CANCELLED,
                    onClick = { onFilterChange(LoanListFilter.CANCELLED) },
                    modifier = Modifier.weight(1f)
                )

                LoanFilterChip(
                    text = "Todos",
                    selected = selectedFilter == LoanListFilter.ALL,
                    onClick = { onFilterChange(LoanListFilter.ALL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactAddLoanButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(54.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable { onClick() },
        color = AppColors.PrimaryDark,
        shape = RoundedCornerShape(AppRadius.card),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = AppSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )
        }
    }
}

@Composable
private fun LoanFilterChip(
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
private fun LoansListCard(
    rows: List<LoanListRow>,
    filter: LoanListFilter,
    searchQuery: String,
    onCreateLoan: () -> Unit,
    onClearSearch: () -> Unit,
    onOpenLoan: (String) -> Unit
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
                    text = when (filter) {
                        LoanListFilter.ACTIVE -> "Préstamos activos"
                        LoanListFilter.OVERDUE -> "Préstamos vencidos"
                        LoanListFilter.PAID -> "Préstamos pagados"
                        LoanListFilter.CANCELLED -> "Préstamos cancelados"
                        LoanListFilter.ALL -> "Todos los préstamos"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "${rows.size} resultados",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        when {
            rows.isEmpty() && searchQuery.isBlank() -> {
                EmptyLoansState(
                    title = when (filter) {
                        LoanListFilter.ACTIVE -> "Sin préstamos activos"
                        LoanListFilter.OVERDUE -> "Sin préstamos vencidos"
                        LoanListFilter.PAID -> "Sin préstamos pagados"
                        LoanListFilter.CANCELLED -> "Sin préstamos cancelados"
                        LoanListFilter.ALL -> "Sin préstamos registrados"
                    },
                    description = if (filter == LoanListFilter.ALL || filter == LoanListFilter.ACTIVE) {
                        "Crea un préstamo para comenzar a controlar la cartera."
                    } else {
                        "No hay elementos para este filtro."
                    },
                    actionText = if (filter == LoanListFilter.ALL || filter == LoanListFilter.ACTIVE) "Crear préstamo" else null,
                    onAction = onCreateLoan
                )
            }

            rows.isEmpty() -> {
                EmptyLoansState(
                    title = "Sin resultados",
                    description = "No hay coincidencias con la búsqueda actual.",
                    actionText = "Limpiar",
                    onAction = onClearSearch
                )
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    rows.forEach { row ->
                        LoanReferenceRow(
                            row = row,
                            onClick = { onOpenLoan(row.loanId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanReferenceRow(
    row: LoanListRow,
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
                    text = row.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }

            LoanStatusPill(
                text = row.statusText,
                color = row.statusColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Capital ${row.principalText}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Pendiente ${row.pendingText}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (row.pendingAmount > 0.0) AppColors.Warning else AppColors.Success
            )
        }

        Text(
            text = "Total ${row.totalText} · Interés ${row.interestText} · Diario ${row.dailyText}",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray500
        )

        ProgressBar(progress = row.progress)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cobrado ${row.paidText}",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )

            Text(
                text = "${row.progressText} · ${row.startDateText}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )
        }
    }
}

@Composable
private fun LoanStatusPill(
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
private fun EmptyLoansState(
    title: String,
    description: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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

        if (!actionText.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(AppRadius.card))
                    .clickable { onAction() },
                color = AppColors.AccentTeal,
                shape = RoundedCornerShape(AppRadius.card)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = AppSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.White
                    )
                }
            }
        }
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

private enum class LoanListFilter {
    ACTIVE,
    OVERDUE,
    PAID,
    CANCELLED,
    ALL
}

private enum class LoanListStatus {
    ACTIVE,
    OVERDUE,
    PAID,
    CANCELLED
}

private data class LoansSummary(
    val activeLoans: Int,
    val overdueLoans: Int,
    val activeCapitalText: String,
    val overdueAmountText: String,
    val expectedText: String,
    val pendingText: String,
    val progressText: String
)

private data class LoanListRow(
    val loanId: String,
    val clientName: String,
    val label: String,
    val principalText: String,
    val totalText: String,
    val paidText: String,
    val pendingText: String,
    val interestText: String,
    val dailyText: String,
    val startDateText: String,
    val pendingAmount: Double,
    val progress: Float,
    val progressText: String,
    val status: LoanListStatus,
    val statusText: String,
    val statusColor: Color
)

private fun buildLoansSummary(
    loans: List<Loan>,
    currencySymbol: String
): LoansSummary {
    val activeLoans = loans.filter { loan -> loan.status == LoanStatus.ACTIVE }

    val overdueLoanIds = activeLoans
        .filter { loan -> isLoanOverdue(loan.id) }
        .map { loan -> loan.id }
        .toSet()

    val activeCapital = activeLoans.sumOf { loan -> loan.principalAmount }
    val expected = activeLoans.sumOf { loan -> loan.totalExpectedAmount }

    val paid = activeLoans.sumOf { loan ->
        activePaidByLoan(loan.id)
    }

    val pending = max(expected - paid, 0.0)

    val overdueAmount = activeLoans
        .filter { loan -> loan.id in overdueLoanIds }
        .sumOf { loan ->
            max(loan.totalExpectedAmount - activePaidByLoan(loan.id), 0.0)
        }

    val progress = if (expected > 0.0) {
        (paid / expected) * 100.0
    } else {
        0.0
    }

    return LoansSummary(
        activeLoans = activeLoans.size,
        overdueLoans = overdueLoanIds.size,
        activeCapitalText = formatMoney(activeCapital, currencySymbol),
        overdueAmountText = formatMoney(overdueAmount, currencySymbol),
        expectedText = formatMoney(expected, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        progressText = formatPercent(progress)
    )
}

private fun buildLoanListRow(
    loan: Loan,
    currencySymbol: String
): LoanListRow {
    val client = LocalClientRepository.getClientById(loan.clientId)
    val clientName = client?.fullName.orEmpty().ifBlank { "Cliente" }

    val paid = activePaidByLoan(loan.id)
    val pending = max(loan.totalExpectedAmount - paid, 0.0)

    val progress = if (loan.totalExpectedAmount > 0.0) {
        (paid / loan.totalExpectedAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val overdue = loan.status == LoanStatus.ACTIVE && isLoanOverdue(loan.id)

    val rowStatus = when {
        loan.status == LoanStatus.CANCELLED -> LoanListStatus.CANCELLED
        loan.status == LoanStatus.PAID -> LoanListStatus.PAID
        overdue -> LoanListStatus.OVERDUE
        else -> LoanListStatus.ACTIVE
    }

    val statusText = when (rowStatus) {
        LoanListStatus.ACTIVE -> "Activo"
        LoanListStatus.OVERDUE -> "Vencido"
        LoanListStatus.PAID -> "Pagado"
        LoanListStatus.CANCELLED -> "Cancelado"
    }

    val statusColor = when (rowStatus) {
        LoanListStatus.ACTIVE -> AppColors.AccentTeal
        LoanListStatus.OVERDUE -> AppColors.Error
        LoanListStatus.PAID -> AppColors.Success
        LoanListStatus.CANCELLED -> AppColors.Gray500
    }

    return LoanListRow(
        loanId = loan.id,
        clientName = clientName,
        label = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
        principalText = formatMoney(loan.principalAmount, currencySymbol),
        totalText = formatMoney(loan.totalExpectedAmount, currencySymbol),
        paidText = formatMoney(paid, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        interestText = formatPercent(loan.interestRatePercent),
        dailyText = formatMoney(loan.estimatedDailyAmount, currencySymbol),
        startDateText = formatDate(loan.startDateMillis),
        pendingAmount = pending,
        progress = progress,
        progressText = formatPercent(progress.toDouble() * 100.0),
        status = rowStatus,
        statusText = statusText,
        statusColor = statusColor
    )
}

private fun activePaidByLoan(loanId: String): Double {
    return LocalPaymentRepository
        .getPaymentsByLoan(loanId)
        .filter { payment -> payment.status == PaymentStatus.ACTIVE }
        .sumOf { payment -> payment.amount }
}

private fun isLoanOverdue(loanId: String): Boolean {
    return LocalInstallmentRepository
        .getInstallmentsByLoan(loanId)
        .any { installment -> installment.status == InstallmentStatus.OVERDUE }
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0").format(value)
}

private fun formatPercent(value: Double): String {
    return DecimalFormat("#,##0.#").format(value) + "%"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

