package com.controlprestamos.features.preferences.data

import android.content.Context
import android.content.SharedPreferences

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
    val businessName: String = LocalPreferencesRepository.DEFAULT_BUSINESS_NAME,
    val currencySymbol: String = LocalPreferencesRepository.DEFAULT_CURRENCY_SYMBOL,
    val dateFormat: String = LocalPreferencesRepository.DEFAULT_DATE_FORMAT,
    val visualTheme: String = AppVisualTheme.EXECUTIVE_BLUE.name,
    val visualScale: String = AppVisualScale.NORMAL.name
)

object LocalPreferencesRepository {

    const val DEFAULT_BUSINESS_NAME = "Control Préstamos"
    const val DEFAULT_CURRENCY_SYMBOL = "$"
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"

    private const val FILE_NAME = "control_prestamos_preferences"

    private const val KEY_BUSINESS_NAME = "business_name"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_DATE_FORMAT = "date_format"
    private const val KEY_VISUAL_THEME = "visual_theme"
    private const val KEY_VISUAL_SCALE = "visual_scale"

    private val allowedDateFormats = setOf(
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd"
    )

    fun getPreferences(context: Context): AppPreferences {
        val sharedPreferences = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )

        val preferences = AppPreferences(
            businessName = getCleanPreference(
                sharedPreferences = sharedPreferences,
                key = KEY_BUSINESS_NAME,
                fallback = DEFAULT_BUSINESS_NAME
            ),
            currencySymbol = getCleanPreference(
                sharedPreferences = sharedPreferences,
                key = KEY_CURRENCY_SYMBOL,
                fallback = DEFAULT_CURRENCY_SYMBOL
            ),
            dateFormat = getCleanPreference(
                sharedPreferences = sharedPreferences,
                key = KEY_DATE_FORMAT,
                fallback = DEFAULT_DATE_FORMAT
            ),
            visualTheme = getCleanPreference(
                sharedPreferences = sharedPreferences,
                key = KEY_VISUAL_THEME,
                fallback = AppVisualTheme.EXECUTIVE_BLUE.name
            ),
            visualScale = getCleanPreference(
                sharedPreferences = sharedPreferences,
                key = KEY_VISUAL_SCALE,
                fallback = AppVisualScale.NORMAL.name
            )
        ).normalized()

        repairIfNeeded(
            sharedPreferences = sharedPreferences,
            preferences = preferences
        )

        return preferences
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
        return getVisualThemeFromName(
            getPreferences(context).visualTheme
        )
    }

    fun getVisualScale(context: Context): AppVisualScale {
        return getVisualScaleFromName(
            getPreferences(context).visualScale
        )
    }

    fun getAvailableVisualThemes(): List<AppVisualTheme> {
        return AppVisualTheme.entries
    }

    fun getAvailableVisualScales(): List<AppVisualScale> {
        return AppVisualScale.entries
    }

    fun getAvailableDateFormats(): List<String> {
        return allowedDateFormats.toList()
    }

    private fun repairIfNeeded(
        sharedPreferences: SharedPreferences,
        preferences: AppPreferences
    ) {
        val currentBusinessName = sharedPreferences.getString(KEY_BUSINESS_NAME, null)
        val currentCurrencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, null)
        val currentDateFormat = sharedPreferences.getString(KEY_DATE_FORMAT, null)
        val currentVisualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, null)
        val currentVisualScale = sharedPreferences.getString(KEY_VISUAL_SCALE, null)

        val needsRepair =
            currentBusinessName != preferences.businessName ||
                currentCurrencySymbol != preferences.currencySymbol ||
                currentDateFormat != preferences.dateFormat ||
                currentVisualTheme != preferences.visualTheme ||
                currentVisualScale != preferences.visualScale

        if (!needsRepair) return

        sharedPreferences.edit()
            .putString(KEY_BUSINESS_NAME, preferences.businessName)
            .putString(KEY_CURRENCY_SYMBOL, preferences.currencySymbol)
            .putString(KEY_DATE_FORMAT, preferences.dateFormat)
            .putString(KEY_VISUAL_THEME, preferences.visualTheme)
            .putString(KEY_VISUAL_SCALE, preferences.visualScale)
            .apply()
    }

    private fun getCleanPreference(
        sharedPreferences: SharedPreferences,
        key: String,
        fallback: String
    ): String {
        val raw = sharedPreferences.getString(key, fallback) ?: fallback

        return sanitizeVisibleText(raw)
            .trim()
            .ifBlank { fallback }
    }

    private fun AppPreferences.normalized(): AppPreferences {
        return copy(
            businessName = sanitizeBusinessName(businessName),
            currencySymbol = sanitizeCurrencySymbol(currencySymbol),
            dateFormat = sanitizeDateFormat(dateFormat),
            visualTheme = getVisualThemeFromName(visualTheme).name,
            visualScale = getVisualScaleFromName(visualScale).name
        )
    }

    private fun sanitizeBusinessName(value: String): String {
        return sanitizeVisibleText(value)
            .trim()
            .replace(Regex("\\s+"), " ")
            .take(60)
            .ifBlank { DEFAULT_BUSINESS_NAME }
    }

    private fun sanitizeCurrencySymbol(value: String): String {
        return sanitizeVisibleText(value)
            .trim()
            .replace(Regex("\\s+"), "")
            .take(4)
            .ifBlank { DEFAULT_CURRENCY_SYMBOL }
    }

    private fun sanitizeDateFormat(value: String): String {
        val cleanValue = sanitizeVisibleText(value).trim()

        return if (cleanValue in allowedDateFormats) {
            cleanValue
        } else {
            DEFAULT_DATE_FORMAT
        }
    }

    private fun getVisualThemeFromName(value: String): AppVisualTheme {
        return runCatching {
            AppVisualTheme.valueOf(value.trim())
        }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
    }

    private fun getVisualScaleFromName(value: String): AppVisualScale {
        return runCatching {
            AppVisualScale.valueOf(value.trim())
        }.getOrDefault(AppVisualScale.NORMAL)
    }

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
            .replace("Ã¡", "á")
            .replace("Ã©", "é")
            .replace("Ã­", "í")
            .replace("Ã³", "ó")
            .replace("Ãº", "ú")
            .replace("Ã±", "ñ")
            .replace("Ã¼", "ü")
            .replace("Â¿", "¿")
            .replace("Â¡", "¡")
            .replace("Â", "")
            .replace("ðŸ", "")
            .filter { character ->
                !character.isISOControl() || character == '\n' || character == '\t'
            }
    }
}
