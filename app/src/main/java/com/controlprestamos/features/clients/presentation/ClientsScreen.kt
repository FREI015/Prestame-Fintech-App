package com.controlprestamos.features.clients.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.ClientAvatar
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import java.text.DecimalFormat
import kotlin.math.max

@Composable
fun ClientsScreen(
    onNavigateBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenMore: () -> Unit,
    onCreateClient: () -> Unit,
    onOpenClient: (String) -> Unit
) {
    val preferences = LocalPreferencesRepository.getPreferences(
        context = LocalContext.current
    )

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(ClientListFilter.ACTIVE.name) }

    val filter = runCatching {
        ClientListFilter.valueOf(selectedFilter)
    }.getOrDefault(ClientListFilter.ACTIVE)

    val allClients = LocalClientRepository.getClients()

    val filteredClients = allClients
        .filter { client ->
            when (filter) {
                ClientListFilter.ACTIVE -> client.status == ClientStatus.ACTIVE
                ClientListFilter.INACTIVE -> client.status == ClientStatus.INACTIVE
                ClientListFilter.ALL -> true
            }
        }
        .filter { client ->
            val query = searchQuery.trim()

            query.isBlank() ||
                client.fullName.contains(query, ignoreCase = true) ||
                client.documentId.contains(query, ignoreCase = true) ||
                client.phone.contains(query, ignoreCase = true)
        }
        .sortedWith(
            compareBy<Client> { client -> client.status != ClientStatus.ACTIVE }
                .thenBy { client -> client.fullName.lowercase() }
        )

    val summary = buildClientSummary(
        clients = allClients,
        currencySymbol = preferences.currencySymbol
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clientes",
                subtitle = "Cartera y contactos",
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
                currentRoute = "clients",
                onOpenDashboard = onOpenDashboard,
                onOpenClients = { },
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

                ClientsSummaryCard(summary = summary)

                ClientsSearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedFilter = filter,
                    onFilterChange = { selectedFilter = it.name },
                    onCreateClient = onCreateClient
                )

                ClientsListSection(
                    clients = filteredClients,
                    currencySymbol = preferences.currencySymbol,
                    filter = filter,
                    searchQuery = searchQuery,
                    onOpenClient = onOpenClient,
                    onCreateClient = onCreateClient,
                    onClearSearch = { searchQuery = "" }
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun ClientsSummaryCard(
    summary: ClientsSummary
) {
    ReferenceCard {
        Text(
            text = "Resumen de clientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientsMiniStat(
                title = "Activos",
                value = summary.activeClients.toString(),
                subtitle = "Operativos",
                color = AppColors.AccentTeal,
                modifier = Modifier.weight(1f)
            )

            ClientsMiniStat(
                title = "Archivados",
                value = summary.inactiveClients.toString(),
                subtitle = "Historial",
                color = AppColors.Gray600,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientsMiniStat(
                title = "Préstamos",
                value = summary.activeLoans.toString(),
                subtitle = "Activos",
                color = AppColors.PrimaryDark,
                modifier = Modifier.weight(1f)
            )

            ClientsMiniStat(
                title = "Por cobrar",
                value = summary.pendingAmountText,
                subtitle = "Cartera",
                color = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClientsMiniStat(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
private fun ClientsSearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: ClientListFilter,
    onFilterChange: (ClientListFilter) -> Unit,
    onCreateClient: () -> Unit
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
                    label = "Buscar cliente",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CompactCreateClientButton(
                onClick = onCreateClient
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientFilterChip(
                text = "Activos",
                selected = selectedFilter == ClientListFilter.ACTIVE,
                onClick = { onFilterChange(ClientListFilter.ACTIVE) },
                modifier = Modifier.weight(1f)
            )

            ClientFilterChip(
                text = "Archivados",
                selected = selectedFilter == ClientListFilter.INACTIVE,
                onClick = { onFilterChange(ClientListFilter.INACTIVE) },
                modifier = Modifier.weight(1f)
            )

            ClientFilterChip(
                text = "Todos",
                selected = selectedFilter == ClientListFilter.ALL,
                onClick = { onFilterChange(ClientListFilter.ALL) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactCreateClientButton(
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
private fun ClientFilterChip(
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
private fun ClientsListSection(
    clients: List<Client>,
    currencySymbol: String,
    filter: ClientListFilter,
    searchQuery: String,
    onOpenClient: (String) -> Unit,
    onCreateClient: () -> Unit,
    onClearSearch: () -> Unit
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
                        ClientListFilter.ACTIVE -> "Clientes activos"
                        ClientListFilter.INACTIVE -> "Clientes archivados"
                        ClientListFilter.ALL -> "Todos los clientes"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "${clients.size} resultados",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        when {
            clients.isEmpty() && searchQuery.isBlank() -> {
                EmptyClientsState(
                    title = if (filter == ClientListFilter.INACTIVE) {
                        "Sin clientes archivados"
                    } else {
                        "Sin clientes registrados"
                    },
                    description = if (filter == ClientListFilter.INACTIVE) {
                        "Los clientes archivados aparecerán aquí."
                    } else {
                        "Agrega un cliente para iniciar su historial."
                    },
                    actionText = if (filter == ClientListFilter.INACTIVE) null else "Crear cliente",
                    onAction = onCreateClient
                )
            }

            clients.isEmpty() -> {
                EmptyClientsState(
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
                    clients.forEach { client ->
                        ClientReferenceRow(
                            client = client,
                            currencySymbol = currencySymbol,
                            onClick = { onOpenClient(client.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientReferenceRow(
    client: Client,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val state = buildClientRowState(
        client = client,
        currencySymbol = currencySymbol
    )

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
                fullName = client.fullName,
                size = 42.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = client.fullName.ifBlank { "Cliente" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = buildClientSubtitle(client),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray500
                )
            }

            ClientStatusPill(
                active = client.status == ClientStatus.ACTIVE
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Prestado ${state.loanedText}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray600
            )

            Text(
                text = "Por cobrar ${state.pendingText}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.pendingAmount > 0.0) AppColors.Warning else AppColors.Success
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(AppRadius.pill))
                .background(AppColors.Gray100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(AppRadius.pill))
                    .background(AppColors.AccentTeal)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.activeLoans} préstamos activos",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray500
            )

            Text(
                text = state.progressText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )
        }
    }
}

@Composable
private fun ClientStatusPill(
    active: Boolean
) {
    val color = if (active) AppColors.Success else AppColors.Gray500
    val text = if (active) "Activo" else "Archivado"

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
private fun EmptyClientsState(
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

private enum class ClientListFilter {
    ACTIVE,
    INACTIVE,
    ALL
}

private data class ClientsSummary(
    val activeClients: Int,
    val inactiveClients: Int,
    val activeLoans: Int,
    val pendingAmountText: String
)

private data class ClientRowState(
    val loanedText: String,
    val pendingText: String,
    val pendingAmount: Double,
    val activeLoans: Int,
    val progress: Float,
    val progressText: String
)

private fun buildClientSummary(
    clients: List<Client>,
    currencySymbol: String
): ClientsSummary {
    val activeLoans = LocalLoanRepository
        .getAllLoans()
        .filter { loan -> loan.status == LoanStatus.ACTIVE }

    val pending = activeLoans.sumOf { loan ->
        max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0)
    }

    return ClientsSummary(
        activeClients = clients.count { client -> client.status == ClientStatus.ACTIVE },
        inactiveClients = clients.count { client -> client.status == ClientStatus.INACTIVE },
        activeLoans = activeLoans.size,
        pendingAmountText = formatMoney(pending, currencySymbol)
    )
}

private fun buildClientRowState(
    client: Client,
    currencySymbol: String
): ClientRowState {
    val activeLoans = LocalLoanRepository
        .getLoansByClient(client.id)
        .filter { loan -> loan.status == LoanStatus.ACTIVE }

    val loaned = activeLoans.sumOf { loan -> loan.principalAmount }
    val totalToCollect = activeLoans.sumOf { loan -> loan.totalExpectedAmount }
    val collected = activeLoans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }

    val pending = max(totalToCollect - collected, 0.0)

    val progress = if (totalToCollect > 0.0) {
        (collected / totalToCollect).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    return ClientRowState(
        loanedText = formatMoney(loaned, currencySymbol),
        pendingText = formatMoney(pending, currencySymbol),
        pendingAmount = pending,
        activeLoans = activeLoans.size,
        progress = progress,
        progressText = formatPercent(progress.toDouble() * 100.0)
    )
}

private fun buildClientSubtitle(client: Client): String {
    return when {
        client.phone.isNotBlank() && client.documentId.isNotBlank() -> {
            "${client.phone} · ${client.documentId}"
        }

        client.phone.isNotBlank() -> client.phone
        client.documentId.isNotBlank() -> client.documentId
        else -> "Sin datos de contacto"
    }
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
