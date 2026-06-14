package com.controlprestamos.features.auth.data

import android.content.Context
import java.security.MessageDigest
import java.util.UUID

data class LocalAuthUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAtMillis: Long,
    val lastLoginAtMillis: Long
)

data class AuthOperationResult(
    val success: Boolean,
    val message: String
)

object LocalAuthRepository {

    private const val PREFS_NAME = "control_prestamos_auth"

    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD_HASH = "password_hash"
    private const val KEY_PASSWORD_SALT = "password_salt"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_LAST_LOGIN_AT = "last_login_at"
    private const val KEY_SESSION_ACTIVE = "session_active"
    private const val KEY_AUTH_PROVIDER = "auth_provider"

    fun hasRegisteredUser(context: Context): Boolean {
        return getRegisteredEmail(context).isNotBlank()
    }

    fun getRegisteredEmail(context: Context): String {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, "")
            .orEmpty()
    }

    fun getUser(context: Context): LocalAuthUser? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_EMAIL, "").orEmpty()

        if (email.isBlank()) return null

        return LocalAuthUser(
            firstName = prefs.getString(KEY_FIRST_NAME, "").orEmpty(),
            lastName = prefs.getString(KEY_LAST_NAME, "").orEmpty(),
            email = email,
            createdAtMillis = prefs.getLong(KEY_CREATED_AT, 0L),
            lastLoginAtMillis = prefs.getLong(KEY_LAST_LOGIN_AT, 0L)
        )
    }

    fun getDisplayName(context: Context): String {
        val user = getUser(context) ?: return ""

        return "${user.firstName} ${user.lastName}".trim()
    }

    fun register(
        context: Context,
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): AuthOperationResult {
        if (hasRegisteredUser(context)) {
            return AuthOperationResult(
                success = false,
                message = "Ya existe una cuenta registrada en este dispositivo."
            )
        }

        val normalizedEmail = normalizeEmail(email)
        val salt = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FIRST_NAME, firstName.trim())
            .putString(KEY_LAST_NAME, lastName.trim())
            .putString(KEY_EMAIL, normalizedEmail)
            .putString(KEY_PASSWORD_SALT, salt)
            .putString(KEY_PASSWORD_HASH, hashPassword(password, salt))
            .putLong(KEY_CREATED_AT, now)
            .putLong(KEY_LAST_LOGIN_AT, now)
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .putString(KEY_AUTH_PROVIDER, "local")
            .apply()

        return AuthOperationResult(
            success = true,
            message = "Cuenta creada correctamente."
        )
    }

    fun authenticate(
        context: Context,
        email: String,
        password: String
    ): AuthOperationResult {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedEmail = prefs.getString(KEY_EMAIL, "").orEmpty()
        val savedSalt = prefs.getString(KEY_PASSWORD_SALT, "").orEmpty()
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, "").orEmpty()

        if (savedEmail.isBlank() || savedHash.isBlank() || savedSalt.isBlank()) {
            return AuthOperationResult(
                success = false,
                message = "No hay una cuenta registrada. Crea una cuenta primero."
            )
        }

        if (normalizeEmail(email) != savedEmail) {
            return AuthOperationResult(
                success = false,
                message = "El correo no coincide con la cuenta registrada."
            )
        }

        val inputHash = hashPassword(password, savedSalt)

        if (inputHash != savedHash) {
            return AuthOperationResult(
                success = false,
                message = "Contraseña incorrecta."
            )
        }

        markSessionActive(context)

        return AuthOperationResult(
            success = true,
            message = "Inicio de sesión correcto."
        )
    }

    fun markSessionActive(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .putLong(KEY_LAST_LOGIN_AT, System.currentTimeMillis())
            .apply()
    }

    fun unlockWithTrustedAuth(context: Context): AuthOperationResult {
        if (!hasRegisteredUser(context)) {
            return AuthOperationResult(
                success = false,
                message = "No hay una cuenta registrada."
            )
        }

        markSessionActive(context)

        return AuthOperationResult(
            success = true,
            message = "Acceso autorizado."
        )
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SESSION_ACTIVE, false)
            .apply()
    }

    fun isSessionActive(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SESSION_ACTIVE, false)
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    private fun hashPassword(
        password: String,
        salt: String
    ): String {
        val raw = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}
