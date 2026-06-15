package com.controlprestamos.features.more.presentation

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

@Composable
fun MoreScreen(
    onNavigateBack: () -> Unit = {},
    onBack: () -> Unit = onNavigateBack,
    onOpenHome: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenLoans: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenFinancialAudit: () -> Unit = onOpenReports,
    onOpenReport: () -> Unit = onOpenReports,
    onOpenSettings: () -> Unit = {},
    onOpenPreferences: () -> Unit = onOpenSettings,
    onOpenSecurity: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    onOpenData: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenHelp: () -> Unit = onOpenSupport,
    onOpenBusinessSettings: () -> Unit = {},
    onOpenCurrencySettings: () -> Unit = {},
    onOpenAppearanceSettings: () -> Unit = {},
    onOpenMore: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Más",
                subtitle = "Ajustes y herramientas",
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

                MoreHeaderCard()

                PreferencesCard(
                    onOpenPreferences = onOpenPreferences,
                    onOpenBusinessSettings = onOpenBusinessSettings,
                    onOpenCurrencySettings = onOpenCurrencySettings,
                    onOpenAppearanceSettings = onOpenAppearanceSettings
                )

                DataSecurityCard(
                    onOpenBackup = onOpenBackup,
                    onOpenExport = onOpenExport,
                    onOpenImport = onOpenImport,
                    onOpenSecurity = onOpenSecurity
                )

                SupportCard(
                onOpenAbout = onOpenHelp,
                onOpenSupport = onOpenHelp
            )

                SecondaryButton(
                    text = "Volver",
                    onClick = onBack
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }

    keepMoreCallbacksCompatible(
        onOpenHome,
        onOpenReport,
        onOpenSettings,
        onOpenData,
        onOpenMore,
        onOpenFinancialAudit,
        onOpenHelp,
        onLogout
    )
}

@Composable
private fun MoreHeaderCard() {
    ReferenceCard {
        Text(
            text = "Centro de control",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "Gestiona respaldo, seguridad, preferencias y ayuda desde un solo lugar.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "App",
                value = "Control Préstamos",
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Versión",
                value = "1.1.0-dev",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MainAccessCard(
    onOpenClients: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenReports: () -> Unit
) {
    keepMoreCallbacksCompatible(
        onOpenClients,
        onOpenLoans,
        onOpenPayments,
        onOpenReports
    )
}



@Composable
private fun PreferencesCard(
    onOpenPreferences: () -> Unit,
    onOpenBusinessSettings: () -> Unit,
    onOpenCurrencySettings: () -> Unit,
    onOpenAppearanceSettings: () -> Unit
) {
    keepMoreCallbacksCompatible(
        onOpenBusinessSettings,
        onOpenCurrencySettings,
        onOpenAppearanceSettings
    )

    ReferenceCard {
        SectionTitle(
            title = "Preferencias",
            subtitle = "Ajustes guardados de forma real."
        )

        ActionRow(
            title = "Preferencias generales",
            description = "Editar datos básicos del negocio, moneda y formato.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenPreferences
        )
    }
}



@Composable
private fun DataSecurityCard(
    onOpenBackup: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenSecurity: () -> Unit
) {
    keepMoreCallbacksCompatible(
        onOpenExport,
        onOpenImport
    )

    ReferenceCard {
        SectionTitle(
            title = "Datos y seguridad",
            subtitle = "Respaldo real de la información y protección de acceso."
        )

        ActionRow(
            title = "Respaldo local",
            description = "Crear, compartir o restaurar una copia de seguridad.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenBackup
        )

        ActionRow(
            title = "Seguridad",
            description = "Configurar PIN, bloqueo de sesión y protección de entrada.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenSecurity
        )
    }
}



@Composable
private fun SupportCard(
    onOpenAbout: () -> Unit,
    onOpenSupport: () -> Unit
) {
    ReferenceCard {
        SectionTitle(
            title = "Acerca de y ayuda",
            subtitle = "Información de la app y guía operativa en un solo lugar."
        )

        ActionRow(
            title = "Acerca de Control Préstamos",
            description = "Ver propósito, versión, alcance y notas de uso.",
            primaryText = "Ver",
            onPrimaryClick = onOpenAbout
        )

        ActionRow(
            title = "Ayuda operativa",
            description = "Consultar guía rápida para clientes, préstamos, pagos y respaldos.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenSupport
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
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = if (highlight) AppColors.AccentTeal.copy(alpha = 0.10f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) AppColors.AccentTeal.copy(alpha = 0.30f) else AppColors.Border
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
                color = if (highlight) AppColors.AccentTeal else AppColors.Gray900
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
private fun keepMoreCallbacksCompatible(
    vararg callbacks: () -> Unit
) {
    callbacks.isNotEmpty()
}
