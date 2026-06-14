package com.controlprestamos.features.help.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ayuda",
                subtitle = "Guía rápida de uso",
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
                    text = "Cómo usar Control Préstamos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                HelpCard(
                    title = "1. Crea clientes",
                    description = "Registra nombre, teléfono y datos básicos antes de crear préstamos."
                )

                HelpCard(
                    title = "2. Crea préstamos",
                    description = "Elige pago único o por cuotas, monto, interés, fecha y frecuencia."
                )

                HelpCard(
                    title = "3. Cobra desde Pagos",
                    description = "Usa la pantalla Pagos para ver cuotas pendientes, vencidas y cobradas hoy."
                )

                HelpCard(
                    title = "4. Usa WhatsApp",
                    description = "El botón de WhatsApp prepara el mensaje, pero tú decides si enviarlo."
                )

                HelpCard(
                    title = "5. Revisa reportes",
                    description = "Los reportes muestran el estado general de tu cartera."
                )
            }
        }
    }
}

@Composable
private fun HelpCard(
    title: String,
    description: String
) {
    AppCard(bordered = true) {
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
        }
    }
}
