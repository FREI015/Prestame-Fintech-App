package com.controlprestamos.features.loans.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppBottomNavigation
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppStatus
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.components.StatusChip
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun LoansByClientScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenMore: () -> Unit,
    onCreateLoan: () -> Unit,
    onOpenLoan: (String) -> Unit,
    onCreatePayment: (String) -> Unit = {}
) {
    val client = LocalClientRepository.getClientById(clientId)
    val isClientActive = FinancialOperationRules.canShowClientInOperationalLists(client)
    val loans = LocalLoanRepository.getLoansByClient(clientId)
    val activeLoans = loans.filter { it.status != LoanStatus.CANCELLED }

    val totalExpected = activeLoans.sumOf { it.totalExpectedAmount }
    val totalPaid = activeLoans.sumOf { loan ->
        LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    }
    val totalPending = max(totalExpected - totalPaid, 0.0)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Préstamos",
                subtitle = client?.fullName ?: "Cliente no encontrado",
                showBack = true,
                showMore = true,
                onBack = onNavigateBack,
                onMore = onOpenMore
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = "loans",
                onNavigate = { route ->
                    when (route) {
                        "dashboard" -> onOpenDashboard()
                        "clients" -> onOpenClients()
                        "loans" -> Unit
                        "payments" -> onOpenPayments()
                    }
                }
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
                    text = "Cartera del cliente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Consulta préstamos, saldo pendiente y estado de cobro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                if (client != null && !isClientActive) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        bordered = true
                    ) {
                        Text(
                            text = "Cliente archivado: puedes consultar su cartera e historial. Reactívalo para crear préstamos o registrar pagos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Warning
                        )
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
                            text = "Resumen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Total esperado: ${formatMoney(totalExpected)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Pagado: ${formatMoney(totalPaid)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Pendiente: ${formatMoney(totalPending)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.AccentTeal
                        )
                    }
                }

                PrimaryButton(
                    text = "Crear préstamo",
                    onClick = onCreateLoan,
                    enabled = client != null && isClientActive
                )

                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "No se puede cargar la cartera porque el cliente no existe.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )
                } else if (loans.isEmpty()) {
                    EmptyState(
                        title = "Sin préstamos registrados",
                        description = "Este cliente todavía no tiene préstamos asociados.",
                        action = {
                            PrimaryButton(
                                text = "Crear préstamo",
                                onClick = onCreateLoan
                            )
                        }
                    )
                } else {
                    loans.forEach { loan ->
                        LoanListItem(
                            loan = loan,
                            onClick = {
                                onOpenLoan(loan.id)
                            },
                            onCreatePayment = {
                                onCreatePayment(loan.id)
                            },
                            canRegisterPayment = isClientActive
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanListItem(
    loan: Loan,
    onClick: () -> Unit,
    onCreatePayment: () -> Unit = {},
    canRegisterPayment: Boolean = true
) {
    val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    val remaining = max(loan.totalExpectedAmount - paid, 0.0)
    val status = if (remaining <= 0.0) AppStatus.COMPLETED else AppStatus.ACTIVE

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMoney(loan.totalExpectedAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Capital: ${formatMoney(loan.principalAmount)} · Interés: ${loan.interestRatePercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )

                    Text(
                        text = "Creado: ${formatDate(loan.createdAtMillis)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray600
                    )
                }

                StatusChip(status = status)
            }

            Text(
                text = "Saldo pendiente: ${formatMoney(remaining)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.AccentTeal
            )

            if (
                canRegisterPayment &&
                !FinancialOperationRules.isClosedLoan(loan) &&
                remaining > 0.0
            ) {
                SecondaryButton(
                    text = "Registrar pago",
                    onClick = onCreatePayment
                )
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return "$" + DecimalFormat("#,##0.00").format(value)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(millis))
}







