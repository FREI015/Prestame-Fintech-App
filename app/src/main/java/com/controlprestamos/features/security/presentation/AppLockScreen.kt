package com.controlprestamos.features.security.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember {
        LocalSecurityRepository.getSettings(context)
    }

    val biometricLabel = remember {
        biometricAvailabilityLabel(context)
    }

    val biometricAvailable = remember {
        isBiometricAvailable(context)
    }

    var pin by rememberSaveable {
        mutableStateOf("")
    }

    var errorMessage by rememberSaveable {
        mutableStateOf("")
    }

    fun unlock() {
        LocalSecurityRepository.markUnlocked(context)
        pin = ""
        errorMessage = ""
        onUnlocked()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "App bloqueada",
                subtitle = "Ingresa tu PIN para continuar"
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
                    .padding(AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Control Préstamos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Tus datos están protegidos. Ingresa el PIN configurado en Seguridad.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        if (settings.biometricEnabled) {
                            Text(
                                text = biometricLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (biometricAvailable) {
                                    AppColors.Success
                                } else {
                                    AppColors.Warning
                                }
                            )

                            SecondaryButton(
                                text = "Desbloquear con biometría",
                                onClick = {
                                    launchBiometricAuthentication(
                                        context = context,
                                        onSuccess = {
                                            unlock()
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                        }
                                    )
                                }
                            )
                        }

                        AppTextField(
                            value = pin,
                            onValueChange = {
                                pin = it.take(6)
                                errorMessage = ""
                            },
                            label = "PIN",
                            keyboardType = KeyboardType.NumberPassword,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        if (errorMessage.isNotBlank()) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Error
                            )
                        }

                        PrimaryButton(
                            text = "Desbloquear",
                            onClick = {
                                if (LocalSecurityRepository.validatePin(context, pin)) {
                                    unlock()
                                } else {
                                    errorMessage = "PIN incorrecto."
                                }
                            }
                        )
                    }
                }

                Text(
                    text = "Si olvidaste el PIN, usa una copia de seguridad o una versión de mantenimiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )
            }
        }
    }
}
