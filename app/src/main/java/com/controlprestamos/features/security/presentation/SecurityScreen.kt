package com.controlprestamos.features.security.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.security.data.LocalSecurityRepository
import com.controlprestamos.features.security.presentation.biometricAvailabilityLabel
import com.controlprestamos.features.security.presentation.isBiometricAvailable

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

    var currentPin by rememberSaveable {
        mutableStateOf("")
    }

    var newPin by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPin by rememberSaveable {
        mutableStateOf("")
    }

    var testPin by rememberSaveable {
        mutableStateOf("")
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Seguridad",
                subtitle = "PIN, biometría y bloqueo",
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
                    text = "Protección de acceso",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = if (settings.hasPin) {
                        "PIN activo. Puedes cambiarlo o desactivarlo."
                    } else {
                        "Aún no tienes PIN activo."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                if (message.isNotBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                message.contains("correctamente", ignoreCase = true) -> AppColors.Success
                                message.contains("válido", ignoreCase = true) -> AppColors.Success
                                message.contains("incorrecto", ignoreCase = true) -> AppColors.Error
                                message.contains("No", ignoreCase = true) -> AppColors.Error
                                else -> AppColors.Gray900
                            }
                        )
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = if (settings.hasPin) "Cambiar PIN" else "Activar PIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        if (settings.hasPin) {
                            AppTextField(
                                value = currentPin,
                                onValueChange = {
                                    currentPin = it.take(6)
                                    message = ""
                                },
                                label = "PIN actual",
                                keyboardType = KeyboardType.NumberPassword,
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        AppTextField(
                            value = newPin,
                            onValueChange = {
                                newPin = it.take(6)
                                message = ""
                            },
                            label = "Nuevo PIN",
                            keyboardType = KeyboardType.NumberPassword,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        AppTextField(
                            value = confirmPin,
                            onValueChange = {
                                confirmPin = it.take(6)
                                message = ""
                            },
                            label = "Confirmar PIN",
                            keyboardType = KeyboardType.NumberPassword,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        PrimaryButton(
                            text = if (settings.hasPin) "Cambiar PIN" else "Activar PIN",
                            onClick = {
                                if (settings.hasPin && !LocalSecurityRepository.validatePin(context, currentPin)) {
                                    message = "PIN actual incorrecto."
                                    return@PrimaryButton
                                }

                                if (!LocalSecurityRepository.isPinValid(newPin)) {
                                    message = "El PIN debe tener entre 4 y 6 números."
                                    return@PrimaryButton
                                }

                                if (newPin != confirmPin) {
                                    message = "La confirmación del PIN no coincide."
                                    return@PrimaryButton
                                }

                                val saved = LocalSecurityRepository.setPin(
                                    context = context,
                                    pin = newPin
                                )

                                if (saved) {
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                    refreshVersion++
                                    message = "PIN guardado correctamente."
                                } else {
                                    message = "No se pudo guardar el PIN."
                                }
                            }
                        )

                        if (settings.hasPin) {
                            SecondaryButton(
                                text = "Desactivar PIN",
                                onClick = {
                                    if (!LocalSecurityRepository.validatePin(context, currentPin)) {
                                        message = "Para desactivar, escribe primero el PIN actual."
                                        return@SecondaryButton
                                    }

                                    LocalSecurityRepository.disablePin(context)
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                    refreshVersion++
                                    message = "PIN desactivado correctamente."
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
                            text = "Validar PIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Esta prueba confirma si el PIN configurado funciona.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        AppTextField(
                            value = testPin,
                            onValueChange = {
                                testPin = it.take(6)
                                message = ""
                            },
                            label = "Probar PIN",
                            keyboardType = KeyboardType.NumberPassword,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        SecondaryButton(
                            text = "Validar PIN",
                            onClick = {
                                message = if (LocalSecurityRepository.validatePin(context, testPin)) {
                                    "PIN válido."
                                } else {
                                    "PIN incorrecto."
                                }
                            }
                        )
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Biometría",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = if (settings.biometricEnabled) {
                                "Biometría activada. $biometricAvailability"
                            } else {
                                "Biometría desactivada. $biometricAvailability"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (biometricAvailable) AppColors.Gray600 else AppColors.Warning
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            SecondaryButton(
                                text = "Activar",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val updated = if (biometricAvailable) {
                                        LocalSecurityRepository.setBiometricEnabled(
                                            context = context,
                                            enabled = true
                                        )
                                    } else {
                                        false
                                    }

                                    if (updated) {
                                        refreshVersion++
                                        message = "Biometría activada correctamente."
                                    } else {
                                        message = if (!biometricAvailable) {
                                        biometricAvailability
                                    } else {
                                        "No puedes activar biometría sin tener un PIN activo."
                                    }
                                    }
                                }
                            )

                            SecondaryButton(
                                text = "Desactivar",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    LocalSecurityRepository.setBiometricEnabled(
                                        context = context,
                                        enabled = false
                                    )

                                    refreshVersion++
                                    message = "Biometría desactivada correctamente."
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
                            text = "Bloqueo automático",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Tiempo actual: ${settings.autoLockMinutes} minutos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            AutoLockButton(
                                modifier = Modifier.weight(1f),
                                text = "1 min",
                                minutes = 1,
                                onSelected = {
                                    LocalSecurityRepository.setAutoLockMinutes(context, it)
                                    refreshVersion++
                                    message = "Bloqueo automático actualizado."
                                }
                            )

                            AutoLockButton(
                                modifier = Modifier.weight(1f),
                                text = "5 min",
                                minutes = 5,
                                onSelected = {
                                    LocalSecurityRepository.setAutoLockMinutes(context, it)
                                    refreshVersion++
                                    message = "Bloqueo automático actualizado."
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            AutoLockButton(
                                modifier = Modifier.weight(1f),
                                text = "15 min",
                                minutes = 15,
                                onSelected = {
                                    LocalSecurityRepository.setAutoLockMinutes(context, it)
                                    refreshVersion++
                                    message = "Bloqueo automático actualizado."
                                }
                            )

                            AutoLockButton(
                                modifier = Modifier.weight(1f),
                                text = "30 min",
                                minutes = 30,
                                onSelected = {
                                    LocalSecurityRepository.setAutoLockMinutes(context, it)
                                    refreshVersion++
                                    message = "Bloqueo automático actualizado."
                                }
                            )
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Text(
                            text = "Siguiente paso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "En el próximo bloque conectaremos este PIN al arranque de la app para que realmente bloquee el acceso.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoLockButton(
    modifier: Modifier = Modifier,
    text: String,
    minutes: Int,
    onSelected: (Int) -> Unit
) {
    SecondaryButton(
        text = text,
        modifier = modifier,
        onClick = {
            onSelected(minutes)
        }
    )
}


