package com.controlprestamos.features.more.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun MoreScreen(
    onNavigateBack: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenFinancialAudit: () -> Unit,
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Más opciones",
                subtitle = "Configuración, seguridad y herramientas",
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
                Text(
                    text = "Centro de control",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Herramientas generales de la aplicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                MoreSectionCard(
                    title = "Preferencias",
                    description = "Nombre del negocio, moneda y formato de fecha.",
                    buttonText = "Abrir preferencias",
                    onClick = onOpenPreferences
                )

                MoreSectionCard(
                    title = "Reportes",
                    description = "Resumen general de clientes, préstamos, cobros y vencidos.",
                    buttonText = "Ver reportes",
                    onClick = onOpenReports
                )

                MoreSectionCard(
                    title = "Auditoría financiera",
                    description = "Herramienta interna para comparar préstamos, pagos, cuotas y saldos.",
                    buttonText = "Abrir auditoría",
                    onClick = onOpenFinancialAudit
                )

                MoreSectionCard(
                    title = "Copia de seguridad",
                    description = "Preparar exportación y restauración segura de datos.",
                    buttonText = "Abrir backup",
                    onClick = onOpenBackup
                )

                MoreSectionCard(
                    title = "Seguridad",
                    description = "PIN, biometría y bloqueo automático.",
                    buttonText = "Abrir seguridad",
                    onClick = onOpenSecurity
                )

                MoreSectionCard(
                    title = "Ayuda",
                    description = "Guía rápida de uso de la app.",
                    buttonText = "Abrir ayuda",
                    onClick = onOpenHelp
                )

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Cerrar sesión queda como acción explícita y segura.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        PrimaryButton(
                            text = "Cerrar sesión",
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreSectionCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            SecondaryButton(
                text = buttonText,
                onClick = onClick
            )
        }
    }
}


