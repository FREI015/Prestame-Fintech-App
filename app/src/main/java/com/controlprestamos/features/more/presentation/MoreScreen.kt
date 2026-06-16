package com.controlprestamos.features.more.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
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
    onOpenSupport: () -> Unit = {},
    onOpenHelp: () -> Unit = onOpenSupport,
    onOpenAbout: () -> Unit = onOpenHelp,
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
                subtitle = "Herramientas y configuración",
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

                MoreSection(
                    title = "Operación",
                    subtitle = "Herramientas para revisar y controlar la cartera."
                ) {
                    MoreOptionCard(
                        icon = "📊",
                        title = "Reportes",
                        description = "Resumen de clientes, préstamos, cobros, cuotas y cartera.",
                        accentColor = AppColors.AccentTeal,
                        onClick = onOpenReports
                    )

                    MoreOptionCard(
                        icon = "🧾",
                        title = "Auditoría financiera",
                        description = "Verifica saldos, pagos activos, cuotas y posibles diferencias.",
                        accentColor = AppColors.Warning,
                        onClick = onOpenFinancialAudit
                    )
                }

                MoreSection(
                    title = "Configuración",
                    subtitle = "Ajusta cómo se comporta y se muestra la aplicación."
                ) {
                    MoreOptionCard(
                        icon = "⚙️",
                        title = "Preferencias",
                        description = "Nombre del negocio, moneda, formato de fecha y apariencia.",
                        accentColor = AppColors.PrimaryDark,
                        onClick = onOpenPreferences
                    )

                    MoreOptionCard(
                        icon = "🔐",
                        title = "Seguridad",
                        description = "Administra PIN, huella y bloqueo automático de la app.",
                        accentColor = AppColors.Success,
                        onClick = onOpenSecurity
                    )
                }

                MoreSection(
                    title = "Datos",
                    subtitle = "Opciones para proteger y conservar tu información."
                ) {
                    MoreOptionCard(
                        icon = "☁️",
                        title = "Respaldo",
                        description = "Crea o revisa una copia local para proteger los datos.",
                        accentColor = AppColors.AccentTeal,
                        onClick = onOpenBackup
                    )
                }

                MoreSection(
                    title = "Ayuda",
                    subtitle = "Información básica para usar mejor la aplicación."
                ) {
                    MoreOptionCard(
                        icon = "❔",
                        title = "Ayuda operativa",
                        description = "Guía rápida sobre clientes, préstamos, pagos y respaldos.",
                        accentColor = AppColors.PrimaryDark,
                        onClick = onOpenHelp
                    )

                    MoreOptionCard(
                        icon = "ℹ️",
                        title = "Acerca de Control Préstamos",
                        description = "Consulta el propósito, alcance y uso recomendado de la app.",
                        accentColor = AppColors.Gray600,
                        onClick = onOpenAbout
                    )
                }

                MoreSection(
                    title = "Sesión",
                    subtitle = "Control de acceso actual."
                ) {
                    MoreOptionCard(
                        icon = "↩",
                        title = "Cerrar sesión",
                        description = "Finaliza la sesión actual y vuelve a la pantalla de acceso.",
                        accentColor = AppColors.Error,
                        onClick = onLogout
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }

    keepMoreCallbacksCompatible(
        onNavigateBack,
        onOpenHome,
        onOpenClients,
        onOpenLoans,
        onOpenPayments,
        onOpenReport,
        onOpenSettings,
        onOpenExport,
        onOpenImport,
        onOpenData,
        onOpenSupport,
        onOpenBusinessSettings,
        onOpenCurrencySettings,
        onOpenAppearanceSettings,
        onOpenMore
    )
}

@Composable
private fun MoreHeaderCard() {
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
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.AccentTeal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Centro de herramientas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Gestiona reportes, seguridad, respaldo y preferencias desde un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun MoreSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        SectionHeader(
            title = title,
            subtitle = subtitle
        )

        content()
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
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
private fun MoreOptionCard(
    icon: String,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        bordered = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OptionIcon(
                icon = icon,
                color = accentColor
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
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

            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun OptionIcon(
    icon: String,
    color: Color
) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = RoundedCornerShape(AppRadius.card),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.22f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun keepMoreCallbacksCompatible(
    vararg callbacks: () -> Unit
) {
    callbacks.forEach { callback ->
        callback.hashCode()
    }
}
