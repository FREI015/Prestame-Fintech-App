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

                MainAccessCard(
                    onOpenClients = onOpenClients,
                    onOpenLoans = onOpenLoans,
                    onOpenPayments = onOpenPayments,
                    onOpenReports = onOpenReports
                )

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
                    onOpenAbout = onOpenAbout,
                    onOpenSupport = onOpenSupport
                )

                AppStatusCard()

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
            text = "Gestiona ajustes, herramientas, datos y accesos rápidos desde un solo lugar.",
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
    ReferenceCard {
        SectionTitle(
            title = "Accesos rápidos",
            subtitle = "Atajos principales para moverte sin perder contexto."
        )

        ActionRow(
            title = "Clientes",
            description = "Abrir cartera de clientes activos e inactivos.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenClients
        )

        ActionRow(
            title = "Préstamos",
            description = "Consultar préstamos, estados y detalle financiero.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenLoans
        )

        ActionRow(
            title = "Pagos",
            description = "Ir al centro de cobros y revisar pagos registrados.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenPayments
        )

        ActionRow(
            title = "Reportes",
            description = "Revisar rendimiento, rangos y resumen de cartera.",
            primaryText = "Ver reportes",
            onPrimaryClick = onOpenReports
        )
    }
}

@Composable
private fun PreferencesCard(
    onOpenPreferences: () -> Unit,
    onOpenBusinessSettings: () -> Unit,
    onOpenCurrencySettings: () -> Unit,
    onOpenAppearanceSettings: () -> Unit
) {
    ReferenceCard {
        SectionTitle(
            title = "Preferencias",
            subtitle = "Configuración visual y datos básicos del negocio."
        )

        ActionRow(
            title = "Preferencias generales",
            description = "Revisar configuración principal de la aplicación.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenPreferences
        )

        ActionRow(
            title = "Datos del negocio",
            description = "Nombre visual, lema y datos de operación.",
            primaryText = "Configurar",
            onPrimaryClick = onOpenBusinessSettings
        )

        ActionRow(
            title = "Moneda y formato",
            description = "Símbolo, formato numérico y presentación de montos.",
            primaryText = "Revisar",
            onPrimaryClick = onOpenCurrencySettings
        )

        ActionRow(
            title = "Apariencia",
            description = "Colores, estilo visual y lectura profesional.",
            primaryText = "Ver",
            onPrimaryClick = onOpenAppearanceSettings
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
    ReferenceCard {
        SectionTitle(
            title = "Datos y seguridad",
            subtitle = "Herramientas para proteger información y preparar respaldos."
        )

        ActionRow(
            title = "Respaldo local",
            description = "Crear una copia de seguridad antes de cambios importantes.",
            primaryText = "Respaldar",
            onPrimaryClick = onOpenBackup
        )

        ActionRow(
            title = "Exportar información",
            description = "Preparar datos para revisión, auditoría o reportes externos.",
            primaryText = "Exportar",
            onPrimaryClick = onOpenExport
        )

        ActionRow(
            title = "Importar o restaurar",
            description = "Restaurar datos desde una copia válida.",
            primaryText = "Importar",
            onPrimaryClick = onOpenImport
        )

        ActionRow(
            title = "Seguridad",
            description = "Opciones de acceso, privacidad y protección de datos.",
            primaryText = "Revisar",
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
            title = "Soporte",
            subtitle = "Información de la aplicación y ayuda operativa."
        )

        ActionRow(
            title = "Acerca de",
            description = "Versión instalada, firma local y datos de la app.",
            primaryText = "Ver",
            onPrimaryClick = onOpenAbout
        )

        ActionRow(
            title = "Ayuda",
            description = "Guía rápida para uso diario y revisión de flujos.",
            primaryText = "Abrir",
            onPrimaryClick = onOpenSupport
        )
    }
}

@Composable
private fun AppStatusCard() {
    ReferenceCard {
        SectionTitle(
            title = "Estado actual",
            subtitle = "Resumen de la versión instalada en desarrollo."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Modo",
                value = "Local",
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Compilación",
                value = "V1.1",
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Los datos se mantienen en el dispositivo. Antes de cambios mayores, realiza una copia de seguridad.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
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
