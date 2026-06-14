package com.controlprestamos.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val hasAccount = LocalAuthRepository.hasRegisteredUser(context)
    val registeredEmail = LocalAuthRepository.getRegisteredEmail(context)

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var formMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_auth_hero),
                    contentDescription = "Crear cuenta Control Préstamos",
                    modifier = Modifier.size(110.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Registra el acceso principal de la aplicación.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (hasAccount) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        bordered = true
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Ya existe una cuenta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Gray900
                            )

                            Text(
                                text = registeredEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.AccentTeal,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Por seguridad esta versión local solo permite una cuenta principal por dispositivo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Gray600
                            )

                            SecondaryButton(
                                text = "Volver al inicio",
                                onClick = onNavigateBack
                            )
                        }
                    }

                    return@Column
                }

                AppTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        formMessage = null
                    },
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        formMessage = null
                    },
                    label = "Apellido",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        formMessage = null
                    },
                    label = "Correo electrónico",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        formMessage = null
                    },
                    label = "Confirmar contraseña",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (!formMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppPrimaryButton(
                    text = "Crear cuenta",
                    onClick = {
                        val validation = AuthValidators.validateRegister(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword
                        )

                        if (validation != null) {
                            formMessage = validation
                            return@AppPrimaryButton
                        }

                        val result = LocalAuthRepository.register(
                            context = context,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password
                        )

                        if (result.success) {
                            password = ""
                            confirmPassword = ""
                            formMessage = null
                            onRegisterSuccess()
                        } else {
                            formMessage = result.message
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Ya tengo cuenta")
                }
            }
        }
    }
}
