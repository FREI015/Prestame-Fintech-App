package com.controlprestamos.features.preferences.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.preferences.data.AppPreferences
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository

@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit = {},
    onBack: () -> Unit = onNavigateBack
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
                subtitle = "Datos guardados del negocio",
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
                HeaderCard()

                BusinessDataCard(
                    businessName = businessName,
                    onBusinessNameChange = {
                        businessName = it
                        savedMessage = ""
                    }
                )

                MoneyAndFormatCard(
                    currencySymbol = currencySymbol,
                    onCurrencySymbolChange = {
                        currencySymbol = it.take(4)
                        savedMessage = ""
                    },
                    dateFormat = dateFormat,
                    onDateFormatChange = {
                        dateFormat = it
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
                                dateFormat = dateFormat,
                                visualTheme = storedPreferences.visualTheme,
                                visualScale = storedPreferences.visualScale
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

                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Datos configurables",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )

            Text(
                text = "Solo se muestran opciones que se guardan realmente: negocio, moneda y formato.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun BusinessDataCard(
    businessName: String,
    onBusinessNameChange: (String) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Datos del negocio",
                subtitle = "Información básica usada en reportes, recibos y pantallas principales."
            )

            OutlinedTextField(
                value = businessName,
                onValueChange = onBusinessNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre del negocio")
                },
                singleLine = true
            )
        }
    }
}

@Composable
private fun MoneyAndFormatCard(
    currencySymbol: String,
    onCurrencySymbolChange: (String) -> Unit,
    dateFormat: String,
    onDateFormatChange: (String) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                title = "Moneda y formato",
                subtitle = "Valores guardados para uso operativo."
            )

            OutlinedTextField(
                value = currencySymbol,
                onValueChange = onCurrencySymbolChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Símbolo de moneda")
                },
                singleLine = true
            )

            Text(
                text = "Formato de fecha",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                FormatButton(
                    text = "dd/MM/yyyy",
                    selected = dateFormat == "dd/MM/yyyy",
                    onClick = {
                        onDateFormatChange("dd/MM/yyyy")
                    },
                    modifier = Modifier.weight(1f)
                )

                FormatButton(
                    text = "MM/dd/yyyy",
                    selected = dateFormat == "MM/dd/yyyy",
                    onClick = {
                        onDateFormatChange("MM/dd/yyyy")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FormatButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) AppColors.AccentTeal else AppColors.Border
        )
    ) {
        Text(
            text = text,
            color = if (selected) AppColors.AccentTeal else AppColors.Gray700
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