package com.controlprestamos.features.clients.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppBottomNavigation
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.ClientAvatar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import kotlin.math.max

private enum class ClientListFilter(
    val label: String
) {
    ACTIVE("Activos"),
    INACTIVE("Archivados"),
    ALL("Todos")
}

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
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var selectedFilter by rememberSaveable {
        mutableStateOf(ClientListFilter.ACTIVE.name)
    }

    val selectedClientFilter = runCatching {
        ClientListFilter.valueOf(selectedFilter)
    }.getOrDefault(ClientListFilter.ACTIVE)

    val allClients = LocalClientRepository.getClients()

    val clients = when (selectedClientFilter) {
        ClientListFilter.ACTIVE -> allClients.filter { it.status == ClientStatus.ACTIVE }
        ClientListFilter.INACTIVE -> allClients.filter { it.status == ClientStatus.INACTIVE }
        ClientListFilter.ALL -> allClients
    }
    val filteredClients = clients.filter { client ->
        val query = searchQuery.trim()

        query.isBlank() ||
            client.fullName.contains(query, ignoreCase = true) ||
            client.documentId.contains(query, ignoreCase = true) ||
            client.phone.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clientes",
                subtitle = "${clients.size} clientes registrados",
                showBack = true,
                showMore = true,
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
                Text(
                    text = "Gestiona tus clientes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Busca, crea y consulta clientes antes de asociar préstamos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                PrimaryButton(
                    text = "Crear cliente",
                    onClick = onCreateClient
                )

                AppTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Buscar por nombre, documento o teléfono",
                    modifier = Modifier.fillMaxWidth()
                )

                when {
                    clients.isEmpty() -> {
                        EmptyState(
                            title = "Todavía no hay clientes",
                            description = "Crea el primer cliente para poder asociarle préstamos después.",
                            action = {
                                PrimaryButton(
                                    text = "Crear cliente",
                                    onClick = onCreateClient
                                )
                            }
                        )
                    }

                    filteredClients.isEmpty() -> {
                        EmptyState(
                            title = "Sin resultados",
                            description = "No encontramos clientes con ese criterio de búsqueda.",
                            action = {
                                SecondaryButton(
                                    text = "Limpiar búsqueda",
                                    onClick = { searchQuery = "" }
                                )
                            }
                        )
                    }

                    else -> {
                        filteredClients.forEach { client ->
                            ClientListItem(
                                client = client,
                                onClick = {
                                    onOpenClient(client.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientListItem(
    client: Client,
    onClick: () -> Unit
) {
    val loans = LocalLoanRepository.getLoansByClient(client.id).filter { it.status != LoanStatus.CANCELLED }
    val pendingAmount = loans.sumOf { loan ->
        max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0)
    }
    val status = if (pendingAmount > 0.0) AppStatus.PENDING else AppStatus.CURRENT

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        bordered = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            ClientAvatar(fullName = client.fullName)

            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Doc: ${client.documentId.ifBlank { "Sin documento" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Tel: ${client.phone.ifBlank { "Sin teléfono" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }

                    StatusChip(status = status)
                }

                Text(
                    text = if (pendingAmount > 0.0) {
                        "Saldo pendiente: ${formatMoney(pendingAmount)}"
                    } else {
                        "Sin saldo pendiente"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pendingAmount > 0.0) AppColors.Warning else AppColors.Success
                )
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return "$" + java.text.DecimalFormat("#,##0.00").format(value)
}








