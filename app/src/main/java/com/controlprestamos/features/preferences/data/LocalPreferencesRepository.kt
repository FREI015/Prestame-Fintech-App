package com.controlprestamos.features.preferences.data

import android.content.Context

enum class AppVisualTheme(
    val label: String,
    val description: String
) {
    EXECUTIVE_BLUE(
        label = "Azul ejecutivo",
        description = "Profesional, sobrio y financiero."
    ),
    FINANCIAL_GREEN(
        label = "Verde financiero",
        description = "Enfocado en crecimiento, cobro y estabilidad."
    ),
    PROFESSIONAL_GOLD(
        label = "Dorado elegante",
        description = "Elegante, comercial y de alto valor."
    )
}

enum class AppVisualScale(
    val label: String,
    val description: String
) {
    COMPACT(
        label = "Compacta",
        description = "Más información en pantalla."
    ),
    NORMAL(
        label = "Normal",
        description = "Equilibrada para uso diario."
    ),
    COMFORTABLE(
        label = "Cómoda",
        description = "Más aire visual entre tarjetas."
    ),
    LARGE(
        label = "Grande",
        description = "Textos y espacios más amplios."
    )
}

data class AppPreferences(
    val businessName: String = "Control Préstamos",
    val currencySymbol: String = "$",
    val dateFormat: String = "dd/MM/yyyy",
    val visualTheme: String = AppVisualTheme.EXECUTIVE_BLUE.name,
    val visualScale: String = AppVisualScale.NORMAL.name
)

object LocalPreferencesRepository {
    private fun sanitizeVisibleText(value: String): String {
        return value
            .replace("\u00C3\u00A1", "á")
            .replace("\u00C3\u00A9", "é")
            .replace("\u00C3\u00AD", "í")
            .replace("\u00C3\u00B3", "ó")
            .replace("\u00C3\u00BA", "ú")
            .replace("\u00C3\u00B1", "ñ")
            .replace("\u00C3\u00BC", "ü")
            .replace("\u00C2\u00BF", "¿")
            .replace("\u00C2\u00A1", "¡")
            .replace("\u00C2\u00A0", " ")
            .replace("\uFFFD", "")
            .replace("\u00F0\u0178", "")
    }

    private fun getCleanPreference(
        sharedPreferences: android.content.SharedPreferences,
        key: String,
        fallback: String
    ): String {
        val raw = sharedPreferences.getString(key, fallback) ?: fallback
        val clean = sanitizeVisibleText(raw).trim().ifBlank { fallback }

        if (clean != raw) {
            sharedPreferences.edit().putString(key, clean).apply()
        }

        return clean
    }

    private const val FILE_NAME = "control_prestamos_preferences"

    private const val KEY_BUSINESS_NAME = "business_name"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_DATE_FORMAT = "date_format"
    private const val KEY_VISUAL_THEME = "visual_theme"
    private const val KEY_VISUAL_SCALE = "visual_scale"

    fun getPreferences(context: Context): AppPreferences {
        val sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

        return AppPreferences(
            businessName = getCleanPreference(sharedPreferences, KEY_BUSINESS_NAME, "Control Préstamos"),
            currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
            dateFormat = sharedPreferences.getString(KEY_DATE_FORMAT, "dd/MM/yyyy") ?: "dd/MM/yyyy",
            visualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, AppVisualTheme.EXECUTIVE_BLUE.name)
                ?: AppVisualTheme.EXECUTIVE_BLUE.name,
            visualScale = sharedPreferences.getString(KEY_VISUAL_SCALE, AppVisualScale.NORMAL.name)
                ?: AppVisualScale.NORMAL.name
        ).normalized()
    }

    fun savePreferences(
        context: Context,
        preferences: AppPreferences
    ) {
        val normalized = preferences.normalized()

        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BUSINESS_NAME, normalized.businessName)
            .putString(KEY_CURRENCY_SYMBOL, normalized.currencySymbol)
            .putString(KEY_DATE_FORMAT, normalized.dateFormat)
            .putString(KEY_VISUAL_THEME, normalized.visualTheme)
            .putString(KEY_VISUAL_SCALE, normalized.visualScale)
            .apply()
    }

    fun getVisualTheme(context: Context): AppVisualTheme {
        return runCatching {
            AppVisualTheme.valueOf(getPreferences(context).visualTheme)
        }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
    }

    fun getVisualScale(context: Context): AppVisualScale {
        return runCatching {
            AppVisualScale.valueOf(getPreferences(context).visualScale)
        }.getOrDefault(AppVisualScale.NORMAL)
    }

    private fun AppPreferences.normalized(): AppPreferences {
        val safeTheme = runCatching {
            AppVisualTheme.valueOf(visualTheme)
        }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)

        val safeScale = runCatching {
            AppVisualScale.valueOf(visualScale)
        }.getOrDefault(AppVisualScale.NORMAL)

        return copy(
            businessName = sanitizeVisibleText(businessName).trim().ifBlank { "Control Préstamos" },
            currencySymbol = currencySymbol.trim().ifBlank { "$" }.take(4),
            dateFormat = dateFormat.trim().ifBlank { "dd/MM/yyyy" },
            visualTheme = safeTheme.name,
            visualScale = safeScale.name
        )
    }
}





