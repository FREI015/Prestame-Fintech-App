package com.controlprestamos.features.preferences.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
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

    val cleanBusinessName = businessName.trim()
    val cleanCurrencySymbol = currencySymbol.trim().ifBlank { "$" }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Preferencias",
                subtitle = "Negocio, moneda y formato",
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

                PreferencesHeaderCard(
                    businessName = cleanBusinessName,
                    currencySymbol = cleanCurrencySymbol,
                    dateFormat = dateFormat
                )

                if (savedMessage.isNotBlank()) {
                    PreferenceMessageCard(
                        message = savedMessage
                    )
                }

                PreferenceSectionCard(
                    icon = "🏢",
                    title = "Datos del negocio",
                    subtitle = "Información que identifica tu cartera y aparece en reportes."
                ) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = {
                            businessName = it
                            savedMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Nombre del negocio")
                        },
                        singleLine = true
                    )

                    Text(
                        text = "Ejemplo: Control Préstamos, Mi Financiera o el nombre comercial que uses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray500
                    )
                }

                PreferenceSectionCard(
                    icon = "💵",
                    title = "Moneda",
                    subtitle = "Define el símbolo que se usará para mostrar montos."
                ) {
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = {
                            currencySymbol = it.take(4)
                            savedMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Símbolo de moneda")
                        },
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        QuickCurrencyButton(
                            modifier = Modifier.weight(1f),
                            text = "$",
                            selected = currencySymbol == "$",
                            onClick = {
                                currencySymbol = "$"
                                savedMessage = ""
                            }
                        )

                        QuickCurrencyButton(
                            modifier = Modifier.weight(1f),
                            text = "Bs",
                            selected = currencySymbol.equals("Bs", ignoreCase = true),
                            onClick = {
                                currencySymbol = "Bs"
                                savedMessage = ""
                            }
                        )

                        QuickCurrencyButton(
                            modifier = Modifier.weight(1f),
                            text = "€",
                            selected = currencySymbol == "€",
                            onClick = {
                                currencySymbol = "€"
                                savedMessage = ""
                            }
                        )
                    }
                }

                PreferenceSectionCard(
                    icon = "📅",
                    title = "Formato de fecha",
                    subtitle = "Selecciona cómo se mostrarán las fechas dentro de la app."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        FormatButton(
                            text = "dd/MM/yyyy",
                            description = "Día / Mes / Año",
                            selected = dateFormat == "dd/MM/yyyy",
                            onClick = {
                                dateFormat = "dd/MM/yyyy"
                                savedMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        )

                        FormatButton(
                            text = "MM/dd/yyyy",
                            description = "Mes / Día / Año",
                            selected = dateFormat == "MM/dd/yyyy",
                            onClick = {
                                dateFormat = "MM/dd/yyyy"
                                savedMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Guardar cambios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Los cambios se aplicarán en las pantallas que usan estos datos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Gray600
                        )

                        PrimaryButton(
                            text = "Guardar preferencias",
                            onClick = {
                                LocalPreferencesRepository.savePreferences(
                                    context = context,
                                    preferences = AppPreferences(
                                        businessName = cleanBusinessName,
                                        currencySymbol = cleanCurrencySymbol,
                                        dateFormat = dateFormat,
                                        visualTheme = storedPreferences.visualTheme,
                                        visualScale = storedPreferences.visualScale
                                    )
                                )

                                businessName = cleanBusinessName
                                currencySymbol = cleanCurrencySymbol
                                savedMessage = "Preferencias guardadas correctamente."
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }
        }
    }
}

@Composable
private fun PreferencesHeaderCard(
    businessName: String,
    currencySymbol: String,
    dateFormat: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColors.AccentTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Configuración del negocio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Ajusta los datos principales de operación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PreferenceSummaryChip(
                    modifier = Modifier.weight(1f),
                    title = "Negocio",
                    value = businessName.ifBlank { "Sin nombre" },
                    active = businessName.isNotBlank()
                )

                PreferenceSummaryChip(
                    modifier = Modifier.weight(1f),
                    title = "Moneda",
                    value = currencySymbol.ifBlank { "$" },
                    active = currencySymbol.isNotBlank()
                )
            }

            PreferenceSummaryChip(
                modifier = Modifier.fillMaxWidth(),
                title = "Formato de fecha",
                value = dateFormat,
                active = dateFormat.isNotBlank()
            )
        }
    }
}

@Composable
private fun PreferenceSummaryChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    active: Boolean
) {
    val backgroundColor = if (active) {
        AppColors.AccentTeal.copy(alpha = 0.10f)
    } else {
        AppColors.Warning.copy(alpha = 0.10f)
    }

    val borderColor = if (active) {
        AppColors.AccentTeal.copy(alpha = 0.32f)
    } else {
        AppColors.Warning.copy(alpha = 0.32f)
    }

    val valueColor = if (active) {
        AppColors.AccentTeal
    } else {
        AppColors.Warning
    }

    Column(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(AppRadius.card)
            )
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Gray600
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun PreferenceMessageCard(
    message: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Success
        )
    }
}

@Composable
private fun PreferenceSectionCard(
    icon: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                icon = icon,
                title = title,
                subtitle = subtitle
            )

            content()
        }
    }
}

@Composable
private fun SectionTitle(
    icon: String,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppColors.PrimaryDark.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            modifier = Modifier.weight(1f),
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
}

@Composable
private fun QuickCurrencyButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectablePreferenceButton(
        modifier = modifier,
        title = text,
        subtitle = "Moneda",
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun FormatButton(
    text: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectablePreferenceButton(
        modifier = modifier,
        title = text,
        subtitle = description,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun SelectablePreferenceButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        AppColors.AccentTeal.copy(alpha = 0.10f)
    } else {
        AppColors.SurfaceMuted
    }

    val borderColor = if (selected) {
        AppColors.AccentTeal
    } else {
        AppColors.Border
    }

    val titleColor = if (selected) {
        AppColors.AccentTeal
    } else {
        AppColors.Gray900
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.card))
            .clickable {
                onClick()
            },
        color = backgroundColor,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Gray600
            )
        }
    }
}
