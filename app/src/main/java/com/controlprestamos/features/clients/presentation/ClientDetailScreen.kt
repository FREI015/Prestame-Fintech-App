package com.controlprestamos.features.clients.presentation

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
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
import com.controlprestamos.features.clients.domain.collection.ClientCollectionPdfGenerator
import com.controlprestamos.core.documents.PdfShareUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import java.text.DecimalFormat
import kotlin.math.max

private var screenCurrencySymbol = "$"

@Composable
fun ClientDetailScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onEditClient: () -> Unit = {},
    onCreateLoan: () -> Unit = {},
    onLoanClick: (String) -> Unit = {},
    onOpenLoanDetail: (String) -> Unit = {},
    onCreatePayment: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)
    screenCurrencySymbol = preferences.currencySymbol

    var archiveActionMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val client = LocalClientRepository.getClientById(clientId)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del cliente",
                subtitle = client?.fullName ?: "Cliente no encontrado",
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
                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "No pudimos encontrar el cliente solicitado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                val loans = LocalLoanRepository
                    .getLoansByClient(client.id)
                    .filter { it.status != LoanStatus.CANCELLED }

                val totalExpected = loans.sumOf { it.totalExpectedAmount }
                val totalPaid = loans.sumOf { loan ->
                    LocalPaymentRepository.getTotalPaidByLoan(loan.id)
                }
                val totalPending = max(totalExpected - totalPaid, 0.0)

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        InfoLine("Documento", client.documentId)
                        InfoLine("Teléfono", client.phone)
                        InfoLine("Dirección", client.address.ifBlank { "Sin dirección" })
                        InfoLine("Notas", client.notes.ifBlank { "Sin notas" })

                        SecondaryButton(
                            text = "Editar cliente",
                            onClick = onEditClient
                        )
                    }
                }

                ClientAdvancedInfoCard(clientId = client.id)

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Resumen financiero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            ClientMetricCard(
                                title = "Préstamos",
                                value = loans.size.toString(),
                                modifier = Modifier.weight(1f)
                            )

                            ClientMetricCard(
                                title = "Pendiente",
                                value = formatMoney(totalPending),
                                modifier = Modifier.weight(1f),
                                warning = totalPending > 0.0
                            )
                        }

                        Text(
                            text = "Los préstamos se crean desde el módulo de Préstamos y los pagos desde el módulo de Pagos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        if (FinancialOperationRules.canShowClientInOperationalLists(client)) {
                            SecondaryButton(
                                text = "Crear préstamo",
                                onClick = onCreateLoan
                            )

                            SecondaryButton(
                                text = "Registrar pago",
                                onClick = onCreatePayment
                            )
                        } else {
                            AppCard(bordered = true) {
                                Text(
                                    text = FinancialOperationRules.getClientOperationMessage(client),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.Warning
                                )
                            }
                        }

                        SecondaryButton(
                            text = "PDF de cobranza",
                            onClick = {
                                val file = ClientCollectionPdfGenerator.generate(
                                    context = context,
                                    client = client,
                                    loans = loans,
                                    businessName = preferences.businessName,
                                    currencySymbol = preferences.currencySymbol
                                )

                                PdfShareUtils.sharePdf(
                                    context = context,
                                    file = file,
                                    chooserTitle = "Compartir PDF de cobranza"
                                )
                            }
                        )

                        if (FinancialOperationRules.canShowClientInOperationalLists(client)) {
                            SecondaryButton(
                                text = "Archivar cliente",
                                onClick = {
                                    val archived = LocalClientRepository.archiveClientSafely(client.id)
                                    archiveActionMessage = if (archived) {
                                        "Cliente archivado. Su historial financiero se conserva."
                                    } else {
                                        "No se pudo archivar el cliente."
                                    }
                                }
                            )
                        } else {
                            PrimaryButton(
                                text = "Reactivar cliente",
                                onClick = {
                                    val reactivated = LocalClientRepository.reactivateClient(client.id)
                                    archiveActionMessage = if (reactivated) {
                                        "Cliente reactivado correctamente."
                                    } else {
                                        "No se pudo reactivar el cliente."
                                    }
                                }
                            )
                        }

                        if (!archiveActionMessage.isNullOrBlank()) {
                            AppCard(bordered = true) {
                                Text(
                                    text = archiveActionMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.Gray600
                                )
                            }
                        }
                    }
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Préstamos del cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        if (loans.isEmpty()) {
                            Text(
                                text = "Este cliente no tiene préstamos activos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Gray600
                            )
                        } else {
                            loans.forEach { loan ->
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    bordered = true
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Text(
                                            text = "Total: ${formatMoney(loan.totalExpectedAmount)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.Gray900
                                        )

                                        Text(
                                            text = "Saldo: ${formatMoney(max(loan.totalExpectedAmount - LocalPaymentRepository.getTotalPaidByLoan(loan.id), 0.0))}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.Gray600
                                        )

                                        SecondaryButton(
                                            text = "Ver detalle",
                                            onClick = {
                                                if (onOpenLoanDetail !== {}) {
                                                    onOpenLoanDetail(loan.id)
                                                } else {
                                                    onLoanClick(loan.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.AccentTeal
        )

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray900
        )
    }
}

@Composable
private fun ClientMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false
) {
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
                color = if (warning) AppColors.Warning else AppColors.AccentTeal
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )
        }
    }
}

private fun formatMoney(value: Double): String {
    return screenCurrencySymbol + DecimalFormat("#,##0.00").format(value)
}








