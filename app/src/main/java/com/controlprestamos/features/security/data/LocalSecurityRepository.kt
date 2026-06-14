package com.controlprestamos.features.security.data

import android.content.Context
import java.security.MessageDigest

data class SecuritySettings(
    val hasPin: Boolean,
    val biometricEnabled: Boolean,
    val autoLockMinutes: Int
)

object LocalSecurityRepository {

    private const val PREFS_NAME = "control_prestamos_security"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
    private const val KEY_LAST_UNLOCK_AT = "last_unlock_at"
    private const val KEY_LAST_BACKGROUND_AT = "last_background_at"

    fun getSettings(context: Context): SecuritySettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pinHash = prefs.getString(KEY_PIN_HASH, "").orEmpty()

        return SecuritySettings(
            hasPin = pinHash.isNotBlank(),
            biometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            autoLockMinutes = prefs.getInt(KEY_AUTO_LOCK_MINUTES, 5)
        )
    }

    fun setPin(
        context: Context,
        pin: String
    ): Boolean {
        if (!isPinValid(pin)) return false

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN_HASH, hashPin(pin))
            .remove(KEY_LAST_BACKGROUND_AT)
            .apply()

        return true
    }

    fun validatePin(
        context: Context,
        pin: String
    ): Boolean {
        val storedHash = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PIN_HASH, "")
            .orEmpty()

        if (storedHash.isBlank()) return false

        return storedHash == hashPin(pin)
    }

    fun disablePin(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .remove(KEY_LAST_UNLOCK_AT)
            .remove(KEY_LAST_BACKGROUND_AT)
            .apply()
    }

    fun setBiometricEnabled(
        context: Context,
        enabled: Boolean
    ): Boolean {
        val settings = getSettings(context)

        if (enabled && !settings.hasPin) {
            return false
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()

        return true
    }

    fun setAutoLockMinutes(
        context: Context,
        minutes: Int
    ) {
        val safeMinutes = when (minutes) {
            1, 5, 15, 30 -> minutes
            else -> 5
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_AUTO_LOCK_MINUTES, safeMinutes)
            .apply()
    }

    fun shouldRequirePinOnLaunch(context: Context): Boolean {
        return getSettings(context).hasPin
    }

    fun markUnlocked(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_UNLOCK_AT, System.currentTimeMillis())
            .remove(KEY_LAST_BACKGROUND_AT)
            .apply()
    }

    fun markBackgrounded(context: Context) {
        val settings = getSettings(context)

        if (!settings.hasPin) return

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKGROUND_AT, System.currentTimeMillis())
            .apply()
    }

    fun shouldRequirePinAfterBackground(context: Context): Boolean {
        val settings = getSettings(context)

        if (!settings.hasPin) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastBackgroundAt = prefs.getLong(KEY_LAST_BACKGROUND_AT, 0L)

        if (lastBackgroundAt <= 0L) return false

        val elapsedMillis = System.currentTimeMillis() - lastBackgroundAt
        val requiredMillis = settings.autoLockMinutes * 60L * 1000L

        return elapsedMillis >= requiredMillis
    }

    fun getLastUnlockAt(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_UNLOCK_AT, 0L)
    }

    fun getLastBackgroundAt(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKGROUND_AT, 0L)
    }

    fun isPinValid(pin: String): Boolean {
        return pin.length in 4..6 && pin.all { it.isDigit() }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}
