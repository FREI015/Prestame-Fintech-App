package com.controlprestamos.core.ui.components

/**
 * Archivo de compatibilidad del sistema de botones.
 *
 * Los botones reales de la app están definidos en:
 * - AppPrimaryButton.kt
 * - AppSecondaryButton.kt
 *
 * Este archivo existe para mantener una estructura clara del sistema UI
 * sin duplicar funciones ni romper imports existentes.
 */
object AppButtonCompatibility {
    const val PRIMARY_BUTTON_FILE = "AppPrimaryButton.kt"
    const val SECONDARY_BUTTON_FILE = "AppSecondaryButton.kt"
}
