package com.controlprestamos.features.security.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

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

    private const val PIN_HASH_PREFIX_V2 = "v2"
    private const val DEFAULT_AUTO_LOCK_MINUTES = 5

    fun getSettings(context: Context): SecuritySettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pinHash = prefs.getString(KEY_PIN_HASH, "").orEmpty()
        val hasPin = pinHash.isNotBlank()

        return SecuritySettings(
            hasPin = hasPin,
            biometricEnabled = hasPin && prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            autoLockMinutes = prefs.getInt(KEY_AUTO_LOCK_MINUTES, DEFAULT_AUTO_LOCK_MINUTES)
        )
    }

    fun setPin(
        context: Context,
        pin: String
    ): Boolean {
        val cleanPin = cleanPin(pin)

        if (!isPinValid(cleanPin)) {
            return false
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN_HASH, hashPinV2(cleanPin))
            .remove(KEY_LAST_BACKGROUND_AT)
            .apply()

        return true
    }

    fun validatePin(
        context: Context,
        pin: String
    ): Boolean {
        val cleanPin = cleanPin(pin)

        if (!isPinValid(cleanPin)) {
            return false
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedHash = prefs.getString(KEY_PIN_HASH, "").orEmpty()

        if (storedHash.isBlank()) {
            return false
        }

        val isValid = if (storedHash.startsWith("$PIN_HASH_PREFIX_V2:")) {
            validatePinV2(cleanPin, storedHash)
        } else {
            validateLegacyPin(cleanPin, storedHash)
        }

        if (isValid && !storedHash.startsWith("$PIN_HASH_PREFIX_V2:")) {
            prefs.edit()
                .putString(KEY_PIN_HASH, hashPinV2(cleanPin))
                .apply()
        }

        return isValid
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
        val safeMinutes = normalizeAutoLockMinutes(minutes)

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

        if (!settings.hasPin) {
            return
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKGROUND_AT, System.currentTimeMillis())
            .apply()
    }

    fun shouldRequirePinAfterBackground(context: Context): Boolean {
        val settings = getSettings(context)

        if (!settings.hasPin) {
            return false
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastBackgroundAt = prefs.getLong(KEY_LAST_BACKGROUND_AT, 0L)

        if (lastBackgroundAt <= 0L) {
            return false
        }

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
        val cleanPin = cleanPin(pin)
        return cleanPin.length in 4..6 && cleanPin.all { it.isDigit() }
    }

    private fun cleanPin(pin: String): String {
        return pin.filter { it.isDigit() }.take(6)
    }

    private fun normalizeAutoLockMinutes(minutes: Int): Int {
        return when (minutes) {
            1, 5, 15, 30 -> minutes
            else -> DEFAULT_AUTO_LOCK_MINUTES
        }
    }

    private fun validateLegacyPin(
        pin: String,
        storedHash: String
    ): Boolean {
        return secureEquals(storedHash, hashPinLegacy(pin))
    }

    private fun hashPinLegacy(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }

    private fun hashPinV2(pin: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val saltEncoded = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashEncoded = hashPinWithSalt(pin, salt)

        return "$PIN_HASH_PREFIX_V2:$saltEncoded:$hashEncoded"
    }

    private fun validatePinV2(
        pin: String,
        storedValue: String
    ): Boolean {
        val parts = storedValue.split(":")

        if (parts.size != 3) {
            return false
        }

        val version = parts[0]
        val saltEncoded = parts[1]
        val storedHash = parts[2]

        if (version != PIN_HASH_PREFIX_V2) {
            return false
        }

        return try {
            val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
            val calculatedHash = hashPinWithSalt(pin, salt)

            secureEquals(storedHash, calculatedHash)
        } catch (_: Throwable) {
            false
        }
    }

    private fun hashPinWithSalt(
        pin: String,
        salt: ByteArray
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.toByteArray())

        val bytes = digest.digest()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun secureEquals(
        first: String,
        second: String
    ): Boolean {
        return MessageDigest.isEqual(
            first.toByteArray(),
            second.toByteArray()
        )
    }
}
