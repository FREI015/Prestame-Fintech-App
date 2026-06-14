package com.controlprestamos.features.preferences.data

import android.content.Context

data class AppPreferences(
    val businessName: String,
    val currencySymbol: String,
    val dateFormat: String
)

object LocalPreferencesRepository {
    private const val FILE_NAME = "control_prestamos_preferences"
    private const val KEY_BUSINESS_NAME = "business_name"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_DATE_FORMAT = "date_format"

    fun getPreferences(context: Context): AppPreferences {
        val sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

        return AppPreferences(
            businessName = sharedPreferences.getString(KEY_BUSINESS_NAME, "Control Préstamos") ?: "Control Préstamos",
            currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
            dateFormat = sharedPreferences.getString(KEY_DATE_FORMAT, "dd/MM/yyyy") ?: "dd/MM/yyyy"
        )
    }

    fun savePreferences(
        context: Context,
        preferences: AppPreferences
    ) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BUSINESS_NAME, preferences.businessName.trim().ifBlank { "Control Préstamos" })
            .putString(KEY_CURRENCY_SYMBOL, preferences.currencySymbol.trim().ifBlank { "$" })
            .putString(KEY_DATE_FORMAT, preferences.dateFormat.trim().ifBlank { "dd/MM/yyyy" })
            .apply()
    }
}
