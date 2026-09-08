package com.controlprestamos.features.reports.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import java.util.Locale
import kotlin.math.max

@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit = {},
    onBack: () -> Unit = onNavigateBack,
    onOpenHome: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenLoans: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
    onOpenMore: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onExportReports: () -> Unit = {},
    onOpenExport: () -> Unit = onExportReports,
    onOpenSettings: () -> Unit = {},
    onOpenPreferences: () -> Unit = onOpenSettings
) {
    val clients = LocalClientRepository.getClients()
    val activeClients = LocalClientRepository.getActiveClients()
    val loans = LocalLoanRepository.getAllLoans()
    val activeLoans = LocalLoanRepository.getActiveLoans()
    val cancelledLoans = LocalLoanRepository.getCancelledLoans()
    val payments = LocalPaymentRepository.getAllPayments()
    val installments = LocalInstallmentRepository.getAllInstallments()

    val totalClients = clients.size
    val totalActiveClients = activeClients.size
    val inactiveClients = max(totalClients - totalActiveClients, 0)

    val totalLoans = loans.size
    val totalActiveLoans = activeLoans.size
    val totalCancelledLoans = cancelledLoans.size
    val paidLoans = loans.count { loan ->
        loan.status.name.equals("PAID", ignoreCase = true)
    }

    val principalInPortfolio = loans.sumOf { loan ->
        loan.principalAmount
    }

    val expectedPortfolio = loans.sumOf { loan ->
        loan.totalExpectedAmount
    }

    val activeExpectedPortfolio = activeLoans.sumOf { loan ->
        loan.totalExpectedAmount
    }

    val collectedTotal = payments.sumOf { payment ->
        payment.amount
    }

    val pendingFromInstallments = installments.sumOf { installment ->
        installment.pendingAmount
    }

    val overdueInstallments = installments.count { installment ->
        installment.status.name.equals("OVERDUE", ignoreCase = true)
    }

    val pendingInstallments = installments.count { installment ->
        installment.status.name.equals("PENDING", ignoreCase = true) ||
            installment.status.name.equals("PARTIAL", ignoreCase = true) ||
            installment.status.name.equals("OVERDUE", ignoreCase = true)
    }

    val paidInstallments = installments.count { installment ->
        installment.status.name.equals("PAID", ignoreCase = true)
    }

    val recoveryPercent = if (expectedPortfolio > 0.0) {
        (collectedTotal / expectedPortfolio) * 100.0
    } else {
        0.0
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reportes",
                subtitle = "Resumen profesional",
                showBack = true,
                showMore = false,
                showMenu = false,
                showNotifications = false,
                onBack = onBack
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

                ReportsHeaderCard(
                    collectedTotal = collectedTotal,
                    expectedPortfolio = expectedPortfolio,
                    recoveryPercent = recoveryPercent
                )

                PortfolioSummaryCard(
                    totalClients = totalClients,
                    activeClients = totalActiveClients,
                    inactiveClients = inactiveClients,
                    totalLoans = totalLoans,
                    activeLoans = totalActiveLoans,
                    paidLoans = paidLoans,
                    cancelledLoans = totalCancelledLoans
                )

                MoneySummaryCard(
                    principalInPortfolio = principalInPortfolio,
                    expectedPortfolio = expectedPortfolio,
                    activeExpectedPortfolio = activeExpectedPortfolio,
                    collectedTotal = collectedTotal,
                    pendingFromInstallments = pendingFromInstallments
                )

                InstallmentsSummaryCard(
                    totalInstallments = installments.size,
                    pendingInstallments = pendingInstallments,
                    overdueInstallments = overdueInstallments,
                    paidInstallments = paidInstallments
                )

                ExportAndAuditCard(
                    onOpenBackup = onOpenBackup,
                    onOpenExport = onOpenExport
                )

                QuickNavigationCard(
                    onOpenClients = onOpenClients,
                    onOpenLoans = onOpenLoans,
                    onOpenPayments = onOpenPayments,
                    onOpenMore = onOpenMore
                )

                SecondaryButton(
                    text = "Volver",
                    onClick = onBack
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }

    keepReportCallbacksCompatible(
        onOpenHome,
        onOpenSettings,
        onOpenPreferences
    )
}

@Composable
private fun ReportsHeaderCard(
    collectedTotal: Double,
    expectedPortfolio: Double,
    recoveryPercent: Double
) {
    ReferenceCard {
        Text(
            text = "Estado general",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "Lectura rápida de cartera, cobros y recuperación acumulada.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        InfoBox(
            title = "Cobrado acumulado",
            value = formatMoney(collectedTotal),
            modifier = Modifier.fillMaxWidth(),
            highlight = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Cartera esperada",
                value = formatMoney(expectedPortfolio),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Recuperación",
                value = formatPercent(recoveryPercent),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PortfolioSummaryCard(
    totalClients: Int,
    activeClients: Int,
    inactiveClients: Int,
    totalLoans: Int,
    activeLoans: Int,
    paidLoans: Int,
    cancelledLoans: Int
) {
    ReferenceCard {
        SectionTitle(
            title = "Cartera",
            subtitle = "Clientes y préstamos registrados en la aplicación."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Clientes",
                value = totalClients.toString(),
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Activos",
                value = activeClients.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Inactivos",
                value = inactiveClients.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Préstamos",
                value = totalLoans.toString(),
                modifier = Modifier.weight(1f),
                highlight = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Vigentes",
                value = activeLoans.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Pagados",
                value = paidLoans.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Cancelados",
                value = cancelledLoans.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoneySummaryCard(
    principalInPortfolio: Double,
    expectedPortfolio: Double,
    activeExpectedPortfolio: Double,
    collectedTotal: Double,
    pendingFromInstallments: Double
) {
    ReferenceCard {
        SectionTitle(
            title = "Montos",
            subtitle = "Capital, esperado, cobrado y pendiente por cuotas."
        )

        InfoBox(
            title = "Capital prestado",
            value = formatMoney(principalInPortfolio),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Esperado total",
                value = formatMoney(expectedPortfolio),
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Esperado activo",
                value = formatMoney(activeExpectedPortfolio),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Cobrado",
                value = formatMoney(collectedTotal),
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Pendiente",
                value = formatMoney(pendingFromInstallments),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InstallmentsSummaryCard(
    totalInstallments: Int,
    pendingInstallments: Int,
    overdueInstallments: Int,
    paidInstallments: Int
) {
    ReferenceCard {
        SectionTitle(
            title = "Cuotas",
            subtitle = "Seguimiento de cuotas pendientes, vencidas y pagadas."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Total",
                value = totalInstallments.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Pendientes",
                value = pendingInstallments.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Vencidas",
                value = overdueInstallments.toString(),
                modifier = Modifier.weight(1f),
                warning = overdueInstallments > 0
            )

            InfoBox(
                title = "Pagadas",
                value = paidInstallments.toString(),
                modifier = Modifier.weight(1f),
                highlight = true
            )
        }
    }
}

@Composable
private fun ExportAndAuditCard(
    onOpenBackup: () -> Unit,
    onOpenExport: () -> Unit
) {
    ReferenceCard {
        SectionTitle(
            title = "Exportación y respaldo",
            subtitle = "Prepara información para revisión o respaldo antes de cambios mayores."
        )

        ActionRow(
            title = "Exportar reporte",
            description = "Usar esta vista como base para revisar cartera y cobros.",
            primaryText = "Exportar",
            onPrimaryClick = onOpenExport
        )

        ActionRow(
            title = "Respaldo",
            description = "Crear o revisar copia local de seguridad.",
            primaryText = "Abrir respaldo",
            onPrimaryClick = onOpenBackup
        )
    }
}

@Composable
private fun QuickNavigationCard(
    onOpenClients: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenMore: () -> Unit
) {
    ReferenceCard {
        SectionTitle(
            title = "Navegación rápida",
            subtitle = "Accesos directos para validar los números del reporte."
        )

        ActionRow(
            title = "Clientes",
            description = "Revisar los clientes que alimentan la cartera.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenClients
        )

        ActionRow(
            title = "Préstamos",
            description = "Auditar préstamos vigentes, pagados y cancelados.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenLoans
        )

        ActionRow(
            title = "Pagos",
            description = "Consultar cobros y registros de pago.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenPayments
        )

        ActionRow(
            title = "Más",
            description = "Volver al centro de herramientas.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenMore
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

@Composable
private fun ActionRow(
    title: String,
    description: String,
    primaryText: String,
    onPrimaryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )

            PrimaryButton(
                text = primaryText,
                onClick = onPrimaryClick
            )
        }
    }
}

@Composable
private fun InfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    warning: Boolean = false
) {
    val boxColor = when {
        warning -> AppColors.Warning.copy(alpha = 0.08f)
        highlight -> AppColors.AccentTeal.copy(alpha = 0.10f)
        else -> AppColors.SurfaceMuted
    }

    val borderColor = when {
        warning -> AppColors.Warning.copy(alpha = 0.25f)
        highlight -> AppColors.AccentTeal.copy(alpha = 0.30f)
        else -> AppColors.Border
    }

    val valueColor = when {
        warning -> AppColors.Warning
        highlight -> AppColors.AccentTeal
        else -> AppColors.Gray900
    }

    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = boxColor,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
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
                color = valueColor
            )
        }
    }
}

@Composable
private fun ReferenceCard(
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            content = content
        )
    }
}

private fun formatMoney(value: Double): String {
    return "$" + String.format(Locale.US, "%,.2f", value)
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.1f%%", value)
}

private fun keepReportCallbacksCompatible(
    vararg callbacks: () -> Unit
) {
    callbacks.isNotEmpty()
}

