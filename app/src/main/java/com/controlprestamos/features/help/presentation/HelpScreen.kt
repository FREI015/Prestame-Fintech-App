package com.controlprestamos.features.help.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit = {},
    onBack: () -> Unit = onNavigateBack
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Acerca de y ayuda",
                subtitle = "Control PrÃ©stamos",
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
                AboutCard()

                HelpSection(
                    title = "Flujo recomendado",
                    body = "Primero registra clientes, luego crea prÃ©stamos, despuÃ©s registra pagos y revisa la cartera desde Inicio, PrÃ©stamos, Pagos y Reportes."
                )

                HelpSection(
                    title = "Clientes",
                    body = "Usa Clientes para crear, editar, archivar o reactivar personas. Archivar no borra el historial financiero."
                )

                HelpSection(
                    title = "PrÃ©stamos",
                    body = "Usa PrÃ©stamos para revisar saldos, cuotas, estados y detalle financiero. Evita editar condiciones cuando ya existan pagos aplicados."
                )

                HelpSection(
                    title = "Pagos",
                    body = "Usa Pagos como centro de cobros. Desde allÃ­ puedes ver cobros de hoy, cuotas pendientes, vencidas e historial."
                )

                HelpSection(
                    title = "Respaldos",
                    body = "Usa Respaldo local para crear, compartir o restaurar una copia de seguridad. Esta es la ruta correcta para exportar informaciÃ³n completa de la app."
                )

                HelpSection(
                    title = "Seguridad",
                    body = "Configura PIN y bloqueo de sesiÃ³n para proteger el acceso. La seguridad no reemplaza los respaldos; ambas funciones deben usarse juntas."
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }
        }
    }
}

@Composable
private fun AboutCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Control PrÃ©stamos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Gestiona. Controla. Haz crecer tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.AccentTeal,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "VersiÃ³n 1.1.0-dev",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray700
            )

            Text(
                text = "AplicaciÃ³n local para control de clientes, prÃ©stamos, cuotas, pagos, reportes, respaldos y seguridad de acceso.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun HelpSection(
    title: String,
    body: String
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
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}
