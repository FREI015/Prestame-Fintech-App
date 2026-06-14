package com.controlprestamos.features.preferences.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.preferences.data.AppPreferences
import com.controlprestamos.features.preferences.data.AppVisualScale
import com.controlprestamos.features.preferences.data.AppVisualTheme
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository

@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val storedPreferences = remember {
        LocalPreferencesRepository.getPreferences(context)
    }

    var businessName by remember {
        mutableStateOf(storedPreferences.businessName)
    }

    var currencySymbol by remember {
        mutableStateOf(storedPreferences.currencySymbol)
    }

    var dateFormat by remember {
        mutableStateOf(storedPreferences.dateFormat)
    }

    var visualTheme by remember {
        mutableStateOf(storedPreferences.visualTheme)
    }

    var visualScale by remember {
        mutableStateOf(storedPreferences.visualScale)
    }

    var savedMessage by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Preferencias",
                subtitle = "Personaliza moneda, reportes y apariencia",
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
                    text = "Datos generales",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Estos datos se usarán en reportes, recibos, WhatsApp y pantallas principales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AppTextField(
                            value = businessName,
                            onValueChange = {
                                businessName = it
                                savedMessage = ""
                            },
                            label = "Nombre del negocio"
                        )

                        AppTextField(
                            value = currencySymbol,
                            onValueChange = {
                                currencySymbol = it.take(4)
                                savedMessage = ""
                            },
                            label = "Símbolo de moneda"
                        )

                        Text(
                            text = "Formato de fecha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = dateFormat,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            SecondaryButton(
                                text = "dd/MM/yyyy",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    dateFormat = "dd/MM/yyyy"
                                    savedMessage = ""
                                }
                            )

                            SecondaryButton(
                                text = "yyyy-MM-dd",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    dateFormat = "yyyy-MM-dd"
                                    savedMessage = ""
                                }
                            )
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Estilo visual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Elige la apariencia que mejor represente tu forma de trabajar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Gray600
                        )

                        AppVisualTheme.values().forEach { theme ->
                            PreferenceChoiceCard(
                                title = theme.label,
                                description = theme.description,
                                selected = visualTheme == theme.name,
                                accentKind = theme.name,
                                onClick = {
                                    visualTheme = theme.name
                                    savedMessage = ""
                                }
                            )
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Escala visual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Ajusta la densidad visual según cómo quieras ver la información.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Gray600
                        )

                        AppVisualScale.values().forEach { scale ->
                            PreferenceChoiceCard(
                                title = scale.label,
                                description = scale.description,
                                selected = visualScale == scale.name,
                                accentKind = visualTheme,
                                onClick = {
                                    visualScale = scale.name
                                    savedMessage = ""
                                }
                            )
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Vista seleccionada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Tema: ${themeLabel(visualTheme)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Escala: ${scaleLabel(visualScale)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        PrimaryButton(
                            text = "Guardar preferencias",
                            onClick = {
                                LocalPreferencesRepository.savePreferences(
                                    context = context,
                                    preferences = AppPreferences(
                                        businessName = businessName,
                                        currencySymbol = currencySymbol,
                                        dateFormat = dateFormat,
                                        visualTheme = visualTheme,
                                        visualScale = visualScale
                                    )
                                )

                                savedMessage = "Preferencias guardadas correctamente."
                            }
                        )

                        if (savedMessage.isNotBlank()) {
                            Text(
                                text = savedMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Success
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceChoiceCard(
    title: String,
    description: String,
    selected: Boolean,
    accentKind: String,
    onClick: () -> Unit
) {
    val accentColor = when (accentKind) {
        AppVisualTheme.FINANCIAL_GREEN.name -> AppColors.Success
        AppVisualTheme.PREMIUM_GOLD.name -> AppColors.Warning
        else -> AppColors.AccentTeal
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick()
            },
        color = if (selected) accentColor.copy(alpha = 0.10f) else AppColors.Surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accentColor else AppColors.Divider
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) accentColor else AppColors.Gray900
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }
    }
}

private fun themeLabel(value: String): String {
    return runCatching {
        AppVisualTheme.valueOf(value).label
    }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE.label)
}

private fun scaleLabel(value: String): String {
    return runCatching {
        AppVisualScale.valueOf(value).label
    }.getOrDefault(AppVisualScale.NORMAL.label)
}


