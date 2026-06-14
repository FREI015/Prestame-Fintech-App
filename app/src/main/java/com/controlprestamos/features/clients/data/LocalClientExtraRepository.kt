package com.controlprestamos.features.clients.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONArray
import org.json.JSONObject

data class ClientExtraInfo(
    val clientId: String,
    val photoUri: String = "",
    val referredBy: String = "",
    val registeredBy: String = "",
    val referenceName: String = "",
    val referencePhone: String = "",
    val secondReferenceName: String = "",
    val secondReferencePhone: String = "",
    val occupation: String = "",
    val workplace: String = "",
    val addressDetail: String = "",
    val internalNotes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

object LocalClientExtraRepository {

    private const val PREFS_NAME = "control_prestamos_client_extras"
    private const val KEY_EXTRAS = "client_extras"

    private val extras = mutableStateMapOf<String, ClientExtraInfo>()
    private var preferences: SharedPreferences? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadExtras()
        initialized = true
    }

    fun reloadFromStorage(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadExtras()

        initialized = true
    }

    fun getExtra(clientId: String): ClientExtraInfo {
        return extras[clientId] ?: ClientExtraInfo(clientId = clientId)
    }

    fun saveExtra(extra: ClientExtraInfo) {
        if (extra.clientId.isBlank()) return

        extras[extra.clientId] = extra.copy(
            updatedAtMillis = System.currentTimeMillis()
        )

        saveExtras()
    }

    fun deleteExtra(clientId: String) {
        extras.remove(clientId)
        saveExtras()
    }

    private fun loadExtras() {
        extras.clear()

        val rawJson = preferences
            ?.getString(KEY_EXTRAS, "[]")
            .orEmpty()

        val array = runCatching {
            JSONArray(rawJson)
        }.getOrElse {
            JSONArray()
        }

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            val extra = json.toClientExtraInfo()

            if (extra.clientId.isNotBlank()) {
                extras[extra.clientId] = extra
            }
        }
    }

    private fun saveExtras() {
        val array = JSONArray()

        extras.values.forEach { extra ->
            array.put(extra.toJson())
        }

        preferences
            ?.edit()
            ?.putString(KEY_EXTRAS, array.toString())
            ?.apply()
    }

    private fun ClientExtraInfo.toJson(): JSONObject {
        return JSONObject().apply {
            put("clientId", clientId)
            put("photoUri", photoUri)
            put("referredBy", referredBy)
            put("registeredBy", registeredBy)
            put("referenceName", referenceName)
            put("referencePhone", referencePhone)
            put("secondReferenceName", secondReferenceName)
            put("secondReferencePhone", secondReferencePhone)
            put("occupation", occupation)
            put("workplace", workplace)
            put("addressDetail", addressDetail)
            put("internalNotes", internalNotes)
            put("updatedAtMillis", updatedAtMillis)
        }
    }

    private fun JSONObject.toClientExtraInfo(): ClientExtraInfo {
        return ClientExtraInfo(
            clientId = optString("clientId"),
            photoUri = optString("photoUri"),
            referredBy = optString("referredBy"),
            registeredBy = optString("registeredBy"),
            referenceName = optString("referenceName"),
            referencePhone = optString("referencePhone"),
            secondReferenceName = optString("secondReferenceName"),
            secondReferencePhone = optString("secondReferencePhone"),
            occupation = optString("occupation"),
            workplace = optString("workplace"),
            addressDetail = optString("addressDetail"),
            internalNotes = optString("internalNotes"),
            updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis())
        )
    }
}

