package com.controlprestamos.features.security.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.security.data.LocalSecurityRepository

@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var refreshVersion by rememberSaveable {
        mutableIntStateOf(0)
    }

    val settings = remember(refreshVersion) {
        LocalSecurityRepository.getSettings(context)
    }

    val biometricAvailability = remember(refreshVersion) {
        biometricAvailabilityLabel(context)
    }

    val biometricAvailable = remember(refreshVersion) {
        isBiometricAvailable(context)
    }

    var currentPin by rememberSaveable { mutableStateOf("") }
    var newPin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var testPin by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    fun clearPinFields() {
        currentPin = ""
        newPin = ""
        confirmPin = ""
        testPin = ""
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Seguridad",
                subtitle = "PIN, huella y bloqueo automático",
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
                SecurityHeaderCard(
                    hasPin = settings.hasPin,
                    biometricEnabled = settings.biometricEnabled,
                    biometricAvailable = biometricAvailable,
                    autoLockMinutes = settings.autoLockMinutes
                )

                if (message.isNotBlank()) {
                    SecurityMessageCard(message = message)
                }

                PinSettingsCard(
                    hasPin = settings.hasPin,
                    currentPin = currentPin,
                    newPin = newPin,
                    confirmPin = confirmPin,
                    testPin = testPin,
                    onCurrentPinChange = {
                        currentPin = it.filter { char -> char.isDigit() }.take(6)
                        message = ""
                    },
                    onNewPinChange = {
                        newPin = it.filter { char -> char.isDigit() }.take(6)
                        message = ""
                    },
                    onConfirmPinChange = {
                        confirmPin = it.filter { char -> char.isDigit() }.take(6)
                        message = ""
                    },
                    onTestPinChange = {
                        testPin = it.filter { char -> char.isDigit() }.take(6)
                        message = ""
                    },
                    onSavePin = {
                        if (settings.hasPin && !LocalSecurityRepository.validatePin(context, currentPin)) {
                            message = "PIN actual incorrecto."
                            return@PinSettingsCard
                        }

                        if (!LocalSecurityRepository.isPinValid(newPin)) {
                            message = "El PIN debe tener entre 4 y 6 números."
                            return@PinSettingsCard
                        }

                        if (newPin != confirmPin) {
                            message = "La confirmación del PIN no coincide."
                            return@PinSettingsCard
                        }

                        val saved = LocalSecurityRepository.setPin(
                            context = context,
                            pin = newPin
                        )

                        if (saved) {
                            clearPinFields()
                            refreshVersion++
                            message = "PIN guardado correctamente."
                        } else {
                            message = "No se pudo guardar el PIN."
                        }
                    },
                    onDisablePin = {
                        if (!LocalSecurityRepository.validatePin(context, currentPin)) {
                            message = "Para desactivar el PIN, escribe primero el PIN actual."
                            return@PinSettingsCard
                        }

                        LocalSecurityRepository.disablePin(context)
                        clearPinFields()
                        refreshVersion++
                        message = "PIN desactivado correctamente."
                    },
                    onValidatePin = {
                        message = if (LocalSecurityRepository.validatePin(context, testPin)) {
                            "PIN válido."
                        } else {
                            "PIN incorrecto."
                        }
                    }
                )

                BiometricSettingsCard(
                    hasPin = settings.hasPin,
                    biometricEnabled = settings.biometricEnabled,
                    biometricAvailable = biometricAvailable,
                    biometricAvailability = biometricAvailability,
                    onEnable = {
                        if (!settings.hasPin) {
                            message = "Primero activa un PIN para poder usar huella."
                            return@BiometricSettingsCard
                        }

                        if (!biometricAvailable) {
                            message = biometricAvailability
                            return@BiometricSettingsCard
                        }

                        val updated = LocalSecurityRepository.setBiometricEnabled(
                            context = context,
                            enabled = true
                        )

                        if (updated) {
                            refreshVersion++
                            message = "Huella activada correctamente."
                        } else {
                            message = "No se pudo activar la huella."
                        }
                    },
                    onDisable = {
                        LocalSecurityRepository.setBiometricEnabled(
                            context = context,
                            enabled = false
                        )

                        refreshVersion++
                        message = "Huella desactivada correctamente."
                    }
                )

                AutoLockSettingsCard(
                    selectedMinutes = settings.autoLockMinutes,
                    onSelected = { minutes ->
                        LocalSecurityRepository.setAutoLockMinutes(context, minutes)
                        refreshVersion++
                        message = "Bloqueo automático actualizado."
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun SecurityHeaderCard(
    hasPin: Boolean,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    autoLockMinutes: Int
) {
    AppCard(bordered = true) {
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
                        text = "🔐",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Protección de acceso",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Controla cómo se desbloquea la aplicación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SecurityStatusChip(
                    modifier = Modifier.weight(1f),
                    title = "PIN",
                    value = if (hasPin) "Activo" else "Inactivo",
                    active = hasPin
                )

                SecurityStatusChip(
                    modifier = Modifier.weight(1f),
                    title = "Huella",
                    value = if (biometricEnabled && biometricAvailable) "Activa" else "Inactiva",
                    active = biometricEnabled && biometricAvailable
                )

                SecurityStatusChip(
                    modifier = Modifier.weight(1f),
                    title = "Bloqueo",
                    value = "${autoLockMinutes} min",
                    active = true
                )
            }
        }
    }
}

@Composable
private fun SecurityStatusChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    active: Boolean
) {
    val backgroundColor = if (active) {
        AppColors.Success.copy(alpha = 0.10f)
    } else {
        AppColors.Warning.copy(alpha = 0.10f)
    }

    val borderColor = if (active) {
        AppColors.Success.copy(alpha = 0.35f)
    } else {
        AppColors.Warning.copy(alpha = 0.35f)
    }

    val valueColor = if (active) {
        AppColors.Success
    } else {
        AppColors.Warning
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Gray600
        )

        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun SecurityMessageCard(
    message: String
) {
    val isSuccess = message.contains("correctamente", ignoreCase = true) ||
        message.contains("válido", ignoreCase = true) ||
        message.contains("actualizado", ignoreCase = true)

    val isError = message.contains("incorrecto", ignoreCase = true) ||
        message.contains("No", ignoreCase = true) ||
        message.contains("Primero", ignoreCase = true)

    val color = when {
        isSuccess -> AppColors.Success
        isError -> AppColors.Error
        else -> AppColors.Gray900
    }

    AppCard(bordered = true) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun PinSettingsCard(
    hasPin: Boolean,
    currentPin: String,
    newPin: String,
    confirmPin: String,
    testPin: String,
    onCurrentPinChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onTestPinChange: (String) -> Unit,
    onSavePin: () -> Unit,
    onDisablePin: () -> Unit,
    onValidatePin: () -> Unit
) {
    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                icon = "🔢",
                title = if (hasPin) "Cambiar PIN" else "Activar PIN",
                subtitle = if (hasPin) {
                    "Actualiza tu PIN de acceso rápido."
                } else {
                    "Crea un PIN de 4 a 6 números para entrar más rápido."
                }
            )

            if (hasPin) {
                AppTextField(
                    value = currentPin,
                    onValueChange = onCurrentPinChange,
                    label = "PIN actual",
                    keyboardType = KeyboardType.NumberPassword,
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            AppTextField(
                value = newPin,
                onValueChange = onNewPinChange,
                label = "Nuevo PIN",
                keyboardType = KeyboardType.NumberPassword,
                visualTransformation = PasswordVisualTransformation()
            )

            AppTextField(
                value = confirmPin,
                onValueChange = onConfirmPinChange,
                label = "Confirmar PIN",
                keyboardType = KeyboardType.NumberPassword,
                visualTransformation = PasswordVisualTransformation()
            )

            PrimaryButton(
                text = if (hasPin) "Guardar nuevo PIN" else "Activar PIN",
                onClick = onSavePin
            )

            if (hasPin) {
                SecondaryButton(
                    text = "Desactivar PIN",
                    onClick = onDisablePin
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                Text(
                    text = "Comprobar PIN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Usa esta prueba para confirmar que el PIN funciona correctamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )

                AppTextField(
                    value = testPin,
                    onValueChange = onTestPinChange,
                    label = "Probar PIN",
                    keyboardType = KeyboardType.NumberPassword,
                    visualTransformation = PasswordVisualTransformation()
                )

                SecondaryButton(
                    text = "Validar PIN",
                    onClick = onValidatePin
                )
            }
        }
    }
}

@Composable
private fun BiometricSettingsCard(
    hasPin: Boolean,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    biometricAvailability: String,
    onEnable: () -> Unit,
    onDisable: () -> Unit
) {
    val statusText = when {
        biometricEnabled && biometricAvailable -> "Huella activa"
        biometricAvailable -> "Huella disponible"
        else -> "Huella no disponible"
    }

    val description = when {
        !hasPin -> "Para usar huella, primero debes activar un PIN de seguridad."
        biometricEnabled && biometricAvailable -> "Puedes desbloquear la app usando la biometría del teléfono."
        biometricAvailable -> "Puedes activar la huella como método rápido de acceso."
        else -> biometricAvailability
    }

    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                icon = "👆",
                title = "Huella / Biometría",
                subtitle = description
            )

            SecurityStateRow(
                label = "Estado",
                value = statusText,
                active = biometricEnabled && biometricAvailable
            )

            Text(
                text = biometricAvailability,
                style = MaterialTheme.typography.bodySmall,
                color = if (biometricAvailable) AppColors.Gray600 else AppColors.Warning
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                SecondaryButton(
                    text = "Activar",
                    modifier = Modifier.weight(1f),
                    onClick = onEnable
                )

                SecondaryButton(
                    text = "Desactivar",
                    modifier = Modifier.weight(1f),
                    onClick = onDisable
                )
            }
        }
    }
}

@Composable
private fun AutoLockSettingsCard(
    selectedMinutes: Int,
    onSelected: (Int) -> Unit
) {
    AppCard(bordered = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SectionTitle(
                icon = "⏱️",
                title = "Bloqueo automático",
                subtitle = "Define cuándo se bloquea la app después de estar inactiva."
            )

            SecurityStateRow(
                label = "Tiempo actual",
                value = "$selectedMinutes minutos",
                active = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AutoLockOption(
                    modifier = Modifier.weight(1f),
                    text = "1 min",
                    minutes = 1,
                    selected = selectedMinutes == 1,
                    onSelected = onSelected
                )

                AutoLockOption(
                    modifier = Modifier.weight(1f),
                    text = "5 min",
                    minutes = 5,
                    selected = selectedMinutes == 5,
                    onSelected = onSelected
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AutoLockOption(
                    modifier = Modifier.weight(1f),
                    text = "15 min",
                    minutes = 15,
                    selected = selectedMinutes == 15,
                    onSelected = onSelected
                )

                AutoLockOption(
                    modifier = Modifier.weight(1f),
                    text = "30 min",
                    minutes = 30,
                    selected = selectedMinutes == 30,
                    onSelected = onSelected
                )
            }
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
private fun SecurityStateRow(
    label: String,
    value: String,
    active: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(
                width = 1.dp,
                color = AppColors.Divider,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (active) AppColors.Success else AppColors.Warning
        )
    }
}

@Composable
private fun AutoLockOption(
    modifier: Modifier = Modifier,
    text: String,
    minutes: Int,
    selected: Boolean,
    onSelected: (Int) -> Unit
) {
    val backgroundColor = if (selected) {
        AppColors.AccentTeal.copy(alpha = 0.10f)
    } else {
        AppColors.Surface
    }

    val borderColor = if (selected) {
        AppColors.AccentTeal
    } else {
        AppColors.Divider
    }

    val textColor = if (selected) {
        AppColors.AccentTeal
    } else {
        AppColors.Gray900
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onSelected(minutes)
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
