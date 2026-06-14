package com.controlprestamos.features.security.presentation

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val BIOMETRIC_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK

fun isBiometricAvailable(
    context: Context
): Boolean {
    return BiometricManager
        .from(context)
        .canAuthenticate(BIOMETRIC_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
}

fun biometricAvailabilityLabel(
    context: Context
): String {
    return when (
        BiometricManager
            .from(context)
            .canAuthenticate(BIOMETRIC_AUTHENTICATORS)
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            "Biometría disponible en este teléfono."
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            "Este teléfono no tiene hardware biométrico compatible."
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            "La biometría no está disponible temporalmente."
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            "No hay huellas/rostro configurados en el teléfono."
        }

        else -> {
            "Biometría no disponible."
        }
    }
}

fun launchBiometricAuthentication(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context.findFragmentActivity()

    if (activity == null) {
        onError("No se pudo abrir la biometría en esta pantalla.")
        return
    }

    val biometricManager = BiometricManager.from(context)
    val availability = biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS)

    if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
        onError(biometricAvailabilityLabel(context))
        return
    }

    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometría no reconocida. Intenta de nuevo o usa el PIN.")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Desbloquear Control Préstamos")
        .setSubtitle("Usa tu huella o rostro para continuar")
        .setNegativeButtonText("Usar PIN")
        .setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
