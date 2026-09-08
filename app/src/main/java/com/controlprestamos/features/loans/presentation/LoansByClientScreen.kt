package com.controlprestamos.features.loans.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.core.ui.components.AppBottomNavigation
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
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
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    val client = LocalClientRepository.getClientById(clientId)
    val isClientActive = FinancialOperationRules.canShowClientInOperationalLists(client)

    val loans = LocalLoanRepository.getLoansByClient(clientId)
    val activeLoans = loans.filter { loan ->
        loan.status != LoanStatus.CANCELLED
    }

    val totalExpected = activeLoans.sumOf { loan ->
        loan.totalExpectedAmount
    }

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
                Spacer(modifier = Modifier.height(AppSpacing.xs))

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

                    return@Column
                }

                ClientLoanHeaderCard(
                    clientName = client.fullName.ifBlank { "Cliente" },
                    isClientActive = isClientActive
                )

                if (!isClientActive) {
                    ArchivedClientNotice()
                }

                ClientLoanSummaryCard(
                    totalExpected = totalExpected,
                    totalPaid = totalPaid,
                    totalPending = totalPending,
                    loanCount = loans.size,
                    activeLoanCount = activeLoans.size,
                    currencySymbol = preferences.currencySymbol
                )

                PrimaryButton(
                    text = "Crear préstamo",
                    onClick = onCreateLoan,
                    enabled = isClientActive
                )

                if (loans.isEmpty()) {
                    EmptyState(
                        title = "Sin préstamos registrados",
                        description = "Este cliente todavía no tiene préstamos asociados.",
                        action = {
                            PrimaryButton(
                                text = "Crear préstamo",
                                onClick = onCreateLoan,
                                enabled = isClientActive
                            )
                        }
                    )
                } else {
                    SectionTitle(
                        title = "Cartera del cliente",
                        subtitle = "${loans.size} préstamos registrados"
                    )

                    loans.forEach { loan ->
                        LoanClientCard(
                            loan = loan,
                            currencySymbol = preferences.currencySymbol,
                            dateFormat = preferences.dateFormat,
                            canRegisterPayment = isClientActive,
                            onClick = {
                                onOpenLoan(loan.id)
                            },
                            onCreatePayment = {
                                onCreatePayment(loan.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun ClientLoanHeaderCard(
    clientName: String,
    isClientActive: Boolean
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💼",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = if (isClientActive) {
                        "Cartera activa del cliente"
                    } else {
                        "Cliente archivado"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isClientActive) AppColors.Gray600 else AppColors.Warning
                )
            }
        }
    }
}

@Composable
private fun ArchivedClientNotice() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Text(
            text = "Cliente archivado: puedes consultar su cartera e historial. Reactívalo para crear préstamos o registrar pagos.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Warning
        )
    }
}

@Composable
private fun ClientLoanSummaryCard(
    totalExpected: Double,
    totalPaid: Double,
    totalPending: Double,
    loanCount: Int,
    activeLoanCount: Int,
    currencySymbol: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Resumen financiero",
                subtitle = "Totales de la cartera de este cliente."
            )

            Text(
                text = formatMoney(totalPending, currencySymbol),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (totalPending > 0.0) AppColors.Warning else AppColors.Success
            )

            Text(
                text = if (totalPending > 0.0) "Saldo pendiente" else "Cartera saldada",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SummaryBox(
                    title = "Total esperado",
                    value = formatMoney(totalExpected, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                SummaryBox(
                    title = "Pagado",
                    value = formatMoney(totalPaid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    color = if (totalPaid > 0.0) AppColors.Success else AppColors.Gray900
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SummaryBox(
                    title = "Préstamos",
                    value = loanCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryBox(
                    title = "Activos",
                    value = activeLoanCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = if (activeLoanCount > 0) AppColors.AccentTeal else AppColors.Gray900
                )
            }
        }
    }
}

@Composable
private fun LoanClientCard(
    loan: Loan,
    currencySymbol: String,
    dateFormat: String,
    canRegisterPayment: Boolean,
    onClick: () -> Unit,
    onCreatePayment: () -> Unit
) {
    val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
    val remaining = max(loan.totalExpectedAmount - paid, 0.0)

    val isClosed = FinancialOperationRules.isClosedLoan(loan)
    val progress = if (loan.totalExpectedAmount > 0.0) {
        (paid / loan.totalExpectedAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val statusText = when {
        loan.status == LoanStatus.CANCELLED -> "Cancelado"
        loan.status == LoanStatus.PAID || remaining <= 0.0 -> "Pagado"
        else -> "Activo"
    }

    val statusColor = when (statusText) {
        "Cancelado" -> AppColors.Gray500
        "Pagado" -> AppColors.Success
        else -> AppColors.AccentTeal
    }

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
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = loan.description.ifBlank { "Préstamo #${loan.id.takeLast(4)}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Creado: ${formatDate(loan.createdAtMillis, dateFormat)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Gray500
                    )
                }

                StatusPill(
                    text = statusText,
                    color = statusColor
                )
            }

            Text(
                text = formatMoney(remaining, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (remaining > 0.0) AppColors.Warning else AppColors.Success
            )

            Text(
                text = if (remaining > 0.0) "Saldo pendiente" else "Préstamo saldado",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SummaryBox(
                    title = "Capital",
                    value = formatMoney(loan.principalAmount, currencySymbol),
                    modifier = Modifier.weight(1f)
                )

                SummaryBox(
                    title = "Total",
                    value = formatMoney(loan.totalExpectedAmount, currencySymbol),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SummaryBox(
                    title = "Pagado",
                    value = formatMoney(paid, currencySymbol),
                    modifier = Modifier.weight(1f),
                    color = if (paid > 0.0) AppColors.Success else AppColors.Gray900
                )

                SummaryBox(
                    title = "Interés",
                    value = formatPercent(loan.interestRatePercent),
                    modifier = Modifier.weight(1f)
                )
            }

            ProgressBar(progress = progress)

            if (
                canRegisterPayment &&
                !isClosed &&
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

@Composable
private fun SummaryBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Gray900
) {
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StatusPill(
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
private fun SectionTitle(
    title: String,
    subtitle: String
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
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )
    }
}

private fun formatMoney(
    value: Double,
    currencySymbol: String
): String {
    return currencySymbol + DecimalFormat("#,##0.00").format(value)
}

private fun formatPercent(
    value: Double
): String {
    return DecimalFormat("#,##0.#").format(value) + "%"
}

private fun formatDate(
    millis: Long,
    dateFormat: String
): String {
    val safePattern = when (dateFormat) {
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd" -> dateFormat
        else -> "dd/MM/yyyy"
    }

    return runCatching {
        SimpleDateFormat(safePattern, Locale.getDefault()).format(Date(millis))
    }.getOrElse {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }
}

