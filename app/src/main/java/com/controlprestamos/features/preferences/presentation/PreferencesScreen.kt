package com.controlprestamos.features.preferences.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.preferences.data.AppPreferences
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

    var savedMessage by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Preferencias",
                subtitle = "Personaliza la app",
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
                    text = "Estos datos se usarán luego en reportes, recibos, WhatsApp y pantallas principales.",
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

                        SecondaryButton(
                            text = "Usar dd/MM/yyyy",
                            onClick = {
                                dateFormat = "dd/MM/yyyy"
                                savedMessage = ""
                            }
                        )

                        SecondaryButton(
                            text = "Usar yyyy-MM-dd",
                            onClick = {
                                dateFormat = "yyyy-MM-dd"
                                savedMessage = ""
                            }
                        )

                        PrimaryButton(
                            text = "Guardar preferencias",
                            onClick = {
                                LocalPreferencesRepository.savePreferences(
                                    context = context,
                                    preferences = AppPreferences(
                                        businessName = businessName,
                                        currencySymbol = currencySymbol,
                                        dateFormat = dateFormat
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
