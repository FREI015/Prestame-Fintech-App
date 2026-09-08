package com.controlprestamos.features.auth.google

import android.content.Context

sealed class GoogleAuthResult {
    data object NotConfigured : GoogleAuthResult()
    data class Success(
        val email: String,
        val displayName: String
    ) : GoogleAuthResult()
    data class Error(
        val message: String
    ) : GoogleAuthResult()
}

/**
 * Coordinador preparado para Google Auth.
 *
 * Estado actual:
 * - No permite ingreso falso.
 * - No simula usuarios.
 * - No devuelve Success hasta conectar credenciales reales.
 *
 * Próximo paso real:
 * - Agregar OAuth Client ID.
 * - Agregar google-services.json si se usa Firebase.
 * - Conectar Credential Manager / Google Sign-In.
 */
object GoogleAuthCoordinator {

    fun isConfigured(context: Context): Boolean {
        return false
    }

    fun signIn(context: Context): GoogleAuthResult {
        return GoogleAuthResult.NotConfigured
    }

    fun userFacingMessage(): String {
        return "Google todavía no está configurado. Por seguridad, este botón no permite entrar hasta conectar credenciales reales."
    }
}

