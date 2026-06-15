package com.controlprestamos.features.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.controlprestamos.R
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppPrimaryButton
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.features.auth.data.LocalAuthRepository
import com.controlprestamos.features.auth.domain.AuthValidators
import com.controlprestamos.features.auth.google.GoogleAuthCoordinator
import com.controlprestamos.features.security.data.LocalSecurityRepository
import com.controlprestamos.features.security.presentation.biometricAvailabilityLabel
import com.controlprestamos.features.security.presentation.isBiometricAvailable
import com.controlprestamos.features.security.presentation.launchBiometricAuthentication

private enum class LoginAccessMode {
    PASSWORD,
    PIN
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val hasAccount = LocalAuthRepository.hasRegisteredUser(context)
    val registeredEmail = LocalAuthRepository.getRegisteredEmail(context)
    val securitySettings = LocalSecurityRepository.getSettings(context)
    val biometricAvailable = isBiometricAvailable(context)
    val biometricLabel = biometricAvailabilityLabel(context)

    var accessMode by rememberSaveable {
        mutableStateOf(
            if (hasAccount && securitySettings.hasPin) {
                LoginAccessMode.PIN
            } else {
                LoginAccessMode.PASSWORD
            }
        )
    }

    var email by rememberSaveable { mutableStateOf(registeredEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var formMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun completeTrustedAccess() {
        val result = LocalAuthRepository.unlockWithTrustedAuth(context)

        if (result.success) {
            LocalSecurityRepository.markUnlocked(context)
            pin = ""
            password = ""
            formMessage = null
            onLoginSuccess()
        } else {
            formMessage = result.message
        }
    }

    Scaffold(
        containerColor = AppColors.Background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = AppColors.Background
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_bg_profesional),
                    contentDescription = "Control Préstamos",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.40f
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Background.copy(alpha = 0.76f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryDark.copy(alpha = 0.10f))
                            .border(
                                width = 1.dp,
                                color = AppColors.AccentTeal.copy(alpha = 0.34f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_auth_hero),
                            contentDescription = "Control Préstamos",
                            modifier = Modifier.size(118.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Control Préstamos",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Gestiona. Controla. Haz crecer tu negocio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray600
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (hasAccount) {
                            "Acceso seguro a tu cartera"
                        } else {
                            "Crea tu cuenta principal para proteger tus datos"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.AccentTeal
                    )

                    if (hasAccount && registeredEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = registeredEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (hasAccount) {
                        ProfesionalAccessCard(
                            selectedMode = accessMode,
                            hasPin = securitySettings.hasPin,
                            biometricEnabled = securitySettings.biometricEnabled,
                            biometricAvailable = biometricAvailable,
                            biometricLabel = biometricLabel,
                            onSelectPassword = {
                                accessMode = LoginAccessMode.PASSWORD
                                formMessage = null
                            },
                            onSelectPin = {
                                accessMode = LoginAccessMode.PIN
                                formMessage = null
                            },
                            onBiometric = {
                                launchBiometricAuthentication(
                                    context = context,
                                    onSuccess = {
                                        completeTrustedAccess()
                                    },
                                    onError = { error ->
                                        formMessage = error
                                    }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        bordered = true
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (accessMode == LoginAccessMode.PIN && hasAccount) {
                                    "Desbloqueo con PIN"
                                } else {
                                    "Acceso con correo"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gray900
                            )

                            Text(
                                text = if (accessMode == LoginAccessMode.PIN && hasAccount) {
                                    "Ingresa tu PIN de seguridad para volver a la aplicación."
                                } else {
                                    "Ingresa tus credenciales principales para acceder."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Gray600
                            )

                            if (accessMode == LoginAccessMode.PASSWORD || !hasAccount) {
                                AppTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it.trim()
                                        formMessage = null
                                    },
                                    label = "Correo electrónico",
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardType = KeyboardType.Email,
                                    enabled = !hasAccount || registeredEmail.isBlank()
                                )

                                AppTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        formMessage = null
                                    },
                                    label = "Contraseña",
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardType = KeyboardType.Password,
                                    visualTransformation = PasswordVisualTransformation()
                                )

                                AppPrimaryButton(
                                    text = "Iniciar sesión",
                                    onClick = {
                                        val validation = AuthValidators.validateLogin(
                                            email = email,
                                            password = password
                                        )

                                        if (validation != null) {
                                            formMessage = validation
                                            return@AppPrimaryButton
                                        }

                                        val result = LocalAuthRepository.authenticate(
                                            context = context,
                                            email = email,
                                            password = password
                                        )

                                        if (result.success) {
                                            LocalSecurityRepository.markUnlocked(context)
                                            password = ""
                                            formMessage = null
                                            onLoginSuccess()
                                        } else {
                                            formMessage = result.message
                                        }
                                    }
                                )
                            } else {
                                AppTextField(
                                    value = pin,
                                    onValueChange = {
                                        pin = it.filter { char -> char.isDigit() }.take(6)
                                        formMessage = null
                                    },
                                    label = "PIN de seguridad",
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardType = KeyboardType.NumberPassword,
                                    visualTransformation = PasswordVisualTransformation()
                                )

                                AppPrimaryButton(
                                    text = "Entrar con PIN",
                                    onClick = {
                                        val validation = AuthValidators.validatePin(pin)

                                        if (validation != null) {
                                            formMessage = validation
                                            return@AppPrimaryButton
                                        }

                                        if (LocalSecurityRepository.validatePin(context, pin)) {
                                            completeTrustedAccess()
                                        } else {
                                            formMessage = "PIN incorrecto."
                                        }
                                    }
                                )
                            }

                            SecondaryButton(
                                text = "Continuar con Google",
                                onClick = {
                                    formMessage = GoogleAuthCoordinator.userFacingMessage()
                                }
                            )

                            if (!formMessage.isNullOrBlank()) {
                                Text(
                                    text = formMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (
                                        formMessage.orEmpty().contains("correcto", ignoreCase = true) ||
                                        formMessage.orEmpty().contains("autorizado", ignoreCase = true)
                                    ) {
                                        AppColors.Success
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (hasAccount) {
                                "Administrar / crear cuenta"
                            } else {
                                "Crear cuenta"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfesionalAccessCard(
    selectedMode: LoginAccessMode,
    hasPin: Boolean,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    biometricLabel: String,
    onSelectPassword: () -> Unit,
    onSelectPin: () -> Unit,
    onBiometric: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Método de acceso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900
                    )

                    Text(
                        text = "Elige cómo quieres desbloquear tu sesión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }

                Text(
                    text = "Seguro",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Success
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccessMethodButton(
                    icon = "\u2709\uFE0F",
                    title = "Correo",
                    subtitle = "Clave principal",
                    selected = selectedMode == LoginAccessMode.PASSWORD,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onClick = onSelectPassword
                )

                if (hasPin) {
                    AccessMethodButton(
                        icon = "\uD83D\uDD22",
                        title = "PIN",
                        subtitle = "Rápido",
                        selected = selectedMode == LoginAccessMode.PIN,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = onSelectPin
                    )
                }

                if (biometricEnabled) {
                    AccessMethodButton(
                        icon = "\uD83D\uDC46",
                        title = "Huella",
                        subtitle = "Biometría",
                        selected = false,
                        enabled = biometricAvailable,
                        modifier = Modifier.weight(1f),
                        onClick = onBiometric
                    )
                }
            }

            if (biometricEnabled) {
                Text(
                    text = biometricLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (biometricAvailable) {
                        AppColors.Gray600
                    } else {
                        AppColors.Warning
                    }
                )
            }
        }
    }
}

@Composable
private fun AccessMethodButton(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        !enabled -> AppColors.Gray300
        selected -> AppColors.AccentTeal
        else -> AppColors.Divider
    }

    val backgroundColor = when {
        selected -> AppColors.AccentTeal.copy(alpha = 0.10f)
        else -> AppColors.Surface
    }

    val contentColor = when {
        !enabled -> AppColors.Gray400
        selected -> AppColors.AccentTeal
        else -> AppColors.Gray900
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) AppColors.Gray600 else AppColors.Gray400
            )
        }
    }
}





