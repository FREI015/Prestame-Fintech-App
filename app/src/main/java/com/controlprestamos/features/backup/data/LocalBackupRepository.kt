package com.controlprestamos.features.backup.data

import android.content.Context
import com.controlprestamos.features.clients.data.LocalClientExtraRepository
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupExportResult(
    val fileName: String,
    val json: String,
    val createdAtMillis: Long
)

data class BackupRestoreResult(
    val success: Boolean,
    val message: String,
    val validationReport: BackupValidationReport? = null
)

data class BackupValidationReport(
    val isValid: Boolean,
    val message: String,
    val schema: String = "",
    val version: Int = -1,
    val createdAt: String = "",
    val clientCount: Int = 0,
    val clientExtraCount: Int = 0,
    val loanCount: Int = 0,
    val installmentCount: Int = 0,
    val paymentCount: Int = 0,
    val preferenceFileCount: Int = 0,
    val warnings: List<String> = emptyList()
) {
    fun toUserMessage(): String {
        return buildString {
            appendLine(message)

            if (isValid) {
                appendLine("Clientes: $clientCount")
                appendLine("Datos extra de clientes: $clientExtraCount")
                appendLine("Préstamos: $loanCount")
                appendLine("Cuotas: $installmentCount")
                appendLine("Pagos / historial: $paymentCount")

                if (createdAt.isNotBlank()) {
                    appendLine("Fecha de copia: $createdAt")
                }

                if (warnings.isNotEmpty()) {
                    appendLine("")
                    appendLine("Alertas:")
                    warnings.forEach { warning ->
                        appendLine("• $warning")
                    }
                }
            }
        }.trim()
    }
}

private data class BackupDataSummary(
    val clientCount: Int,
    val clientExtraCount: Int,
    val loanCount: Int,
    val installmentCount: Int,
    val paymentCount: Int
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("clientCount", clientCount)
            put("clientExtraCount", clientExtraCount)
            put("loanCount", loanCount)
            put("installmentCount", installmentCount)
            put("paymentCount", paymentCount)
        }
    }
}

object LocalBackupRepository {

    private const val schemaVersion = 2

    private const val BACKUP_SCHEMA = "control_prestamos_backup"
    private const val BACKUP_VERSION = 2
    private const val METADATA_PREFS = "control_prestamos_backup_metadata"
    private const val KEY_LAST_BACKUP_AT = "last_backup_at"
    private const val KEY_LAST_RESTORE_AT = "last_restore_at"
    private const val KEY_LAST_BACKUP_TEXT = "last_backup_text"

    private val supportedBackupVersions = setOf(1, 2)

    private val appPreferenceFiles = listOf(
        "control_prestamos_clients",
        "control_prestamos_client_extras",
        "control_prestamos_loans",
        "control_prestamos_installments",
        "control_prestamos_payments",
        "control_prestamos_preferences",
        "control_prestamos_security",
        "control_prestamos_auth"
    )

    fun createBackup(context: Context): BackupExportResult {
        val createdAt = System.currentTimeMillis()
        val root = JSONObject()

        root.put("schema", BACKUP_SCHEMA)
        root.put("version", BACKUP_VERSION)
        root.put("createdAtMillis", createdAt)
        root.put("createdAt", formatDate(createdAt))
        root.put("summary", buildCurrentDataSummary(context).toJson())

        val preferencesJson = JSONObject()

        appPreferenceFiles.forEach { preferenceName ->
            val sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            val preferenceJson = JSONObject()

            sharedPreferences.all.forEach { entry ->
                preferenceJson.put(entry.key, valueToJson(entry.value))
            }

            preferencesJson.put(preferenceName, preferenceJson)
        }

        root.put("preferences", preferencesJson)

        val prettyJson = root.toString(2)
        val fileName = "control_prestamos_backup_${createdAt}.json"

        context.getSharedPreferences(METADATA_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKUP_AT, createdAt)
            .putString(KEY_LAST_BACKUP_TEXT, prettyJson)
            .apply()

        return BackupExportResult(
            fileName = fileName,
            json = prettyJson,
            createdAtMillis = createdAt
        )
    }

    fun inspectBackup(backupJson: String): BackupValidationReport {
        val parsed = runCatching {
            JSONObject(backupJson)
        }.getOrElse {
            return BackupValidationReport(
                isValid = false,
                message = "El archivo seleccionado no es un JSON válido."
            )
        }

        val schema = parsed.optString("schema")
        val version = parsed.optInt("version", -1)
        val createdAt = parsed.optString("createdAt")

        if (schema != BACKUP_SCHEMA) {
            return BackupValidationReport(
                isValid = false,
                message = "El backup no pertenece a Control Préstamos.",
                schema = schema,
                version = version,
                createdAt = createdAt
            )
        }

        if (version !in supportedBackupVersions) {
            return BackupValidationReport(
                isValid = false,
                message = "Versión de backup no compatible.",
                schema = schema,
                version = version,
                createdAt = createdAt
            )
        }

        val preferencesJson = parsed.optJSONObject("preferences")
            ?: return BackupValidationReport(
                isValid = false,
                message = "El backup no contiene datos de preferencias.",
                schema = schema,
                version = version,
                createdAt = createdAt
            )

        val warnings = mutableListOf<String>()

        appPreferenceFiles.forEach { preferenceName ->
            if (!preferencesJson.has(preferenceName)) {
                warnings += "No se encontró el bloque $preferenceName."
            }
        }

        val clientCount = countJsonArrayPreference(
            preferencesJson = preferencesJson,
            preferenceName = "control_prestamos_clients",
            key = "clients"
        )

        val clientExtraCount = countJsonArrayPreference(
            preferencesJson = preferencesJson,
            preferenceName = "control_prestamos_client_extras",
            key = "client_extras"
        )

        val loanCount = countJsonArrayPreference(
            preferencesJson = preferencesJson,
            preferenceName = "control_prestamos_loans",
            key = "loans"
        )

        val installmentCount = countJsonArrayPreference(
            preferencesJson = preferencesJson,
            preferenceName = "control_prestamos_installments",
            key = "installments"
        )

        val paymentCount = countJsonArrayPreference(
            preferencesJson = preferencesJson,
            preferenceName = "control_prestamos_payments",
            key = "payments"
        )

        if (loanCount > 0 && clientCount == 0) {
            warnings += "Hay préstamos, pero no se detectaron clientes."
        }

        if (paymentCount > 0 && loanCount == 0) {
            warnings += "Hay pagos, pero no se detectaron préstamos."
        }

        if (installmentCount > 0 && loanCount == 0) {
            warnings += "Hay cuotas, pero no se detectaron préstamos."
        }

        if (loanCount > 0 && installmentCount == 0) {
            warnings += "Hay préstamos sin cuotas guardadas. La app intentará reconstruirlas al restaurar."
        }

        if (!preferencesJson.has("control_prestamos_preferences")) {
            warnings += "No se encontró configuración de negocio/moneda."
        }

        return BackupValidationReport(
            isValid = true,
            message = if (warnings.isEmpty()) {
                "Backup válido. Puedes restaurarlo con seguridad."
            } else {
                "Backup válido, pero con alertas para revisar."
            },
            schema = schema,
            version = version,
            createdAt = createdAt,
            clientCount = clientCount,
            clientExtraCount = clientExtraCount,
            loanCount = loanCount,
            installmentCount = installmentCount,
            paymentCount = paymentCount,
            preferenceFileCount = preferencesJson.length(),
            warnings = warnings
        )
    }

    fun getLastBackupLabel(context: Context): String {
        val lastBackupAt = context
            .getSharedPreferences(METADATA_PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP_AT, 0L)

        return if (lastBackupAt <= 0L) {
            "Sin copias registradas"
        } else {
            formatDate(lastBackupAt)
        }
    }

    fun getLastBackupText(context: Context): String {
        return context
            .getSharedPreferences(METADATA_PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_BACKUP_TEXT, "")
            .orEmpty()
    }

    fun restoreBackup(
        context: Context,
        backupJson: String
    ): BackupRestoreResult {
        val validationReport = inspectBackup(backupJson)

        if (!validationReport.isValid) {
            return BackupRestoreResult(
                success = false,
                message = validationReport.message,
                validationReport = validationReport
            )
        }

        val parsed = JSONObject(backupJson)
        val preferencesJson = parsed.optJSONObject("preferences")
            ?: return BackupRestoreResult(
                success = false,
                message = "El backup no contiene datos de preferencias.",
                validationReport = validationReport
            )

        appPreferenceFiles.forEach { preferenceName ->
            val preferenceJson = preferencesJson.optJSONObject(preferenceName) ?: JSONObject()
            val editor = context
                .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                .edit()
                .clear()

            val keys = preferenceJson.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val valueObject = preferenceJson.optJSONObject(key) ?: continue

                when (valueObject.optString("type")) {
                    "string" -> editor.putString(key, valueObject.optString("value"))
                    "int" -> editor.putInt(key, valueObject.optInt("value"))
                    "long" -> editor.putLong(key, valueObject.optLong("value"))
                    "float" -> editor.putFloat(key, valueObject.optDouble("value").toFloat())
                    "boolean" -> editor.putBoolean(key, valueObject.optBoolean("value"))
                    "string_set" -> {
                        val array = valueObject.optJSONArray("value") ?: JSONArray()
                        val set = mutableSetOf<String>()

                        for (index in 0 until array.length()) {
                            set.add(array.optString(index))
                        }

                        editor.putStringSet(key, set)
                    }
                }
            }

            editor.apply()
        }

        reloadRepositoriesAfterRestore(context)
        rebuildFinancialStateAfterRestore()

        val restoredAt = System.currentTimeMillis()

        context.getSharedPreferences(METADATA_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_RESTORE_AT, restoredAt)
            .apply()

        return BackupRestoreResult(
            success = true,
            message = "Backup restaurado correctamente. Datos recargados y cartera recalculada.",
            validationReport = validationReport
        )
    }

    private fun reloadRepositoriesAfterRestore(context: Context) {
        LocalClientRepository.reloadFromStorage(context)
        LocalClientExtraRepository.reloadFromStorage(context)
        LocalLoanRepository.reloadFromStorage(context)
        LocalPaymentRepository.reloadFromStorage(context)
        LocalInstallmentRepository.reloadFromStorage(context)
    }

    private fun rebuildFinancialStateAfterRestore() {
        LocalLoanRepository.getAllLoans().forEach { loan ->
            LocalInstallmentRepository.rebuildInstallmentsForLoan(
                loanId = loan.id
            )
        }
    }

    private fun buildCurrentDataSummary(context: Context): BackupDataSummary {
        return BackupDataSummary(
            clientCount = countJsonArrayInSharedPreferences(
                context = context,
                preferenceName = "control_prestamos_clients",
                key = "clients"
            ),
            clientExtraCount = countJsonArrayInSharedPreferences(
                context = context,
                preferenceName = "control_prestamos_client_extras",
                key = "client_extras"
            ),
            loanCount = countJsonArrayInSharedPreferences(
                context = context,
                preferenceName = "control_prestamos_loans",
                key = "loans"
            ),
            installmentCount = countJsonArrayInSharedPreferences(
                context = context,
                preferenceName = "control_prestamos_installments",
                key = "installments"
            ),
            paymentCount = countJsonArrayInSharedPreferences(
                context = context,
                preferenceName = "control_prestamos_payments",
                key = "payments"
            )
        )
    }

    private fun countJsonArrayInSharedPreferences(
        context: Context,
        preferenceName: String,
        key: String
    ): Int {
        val raw = context
            .getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getString(key, "[]")
            .orEmpty()

        return runCatching {
            JSONArray(raw).length()
        }.getOrDefault(0)
    }

    private fun countJsonArrayPreference(
        preferencesJson: JSONObject,
        preferenceName: String,
        key: String
    ): Int {
        val preferenceJson = preferencesJson.optJSONObject(preferenceName) ?: return 0
        val valueObject = preferenceJson.optJSONObject(key) ?: return 0

        if (valueObject.optString("type") != "string") return 0

        val raw = valueObject.optString("value", "[]")

        return runCatching {
            JSONArray(raw).length()
        }.getOrDefault(0)
    }

    private fun valueToJson(value: Any?): JSONObject {
        return JSONObject().apply {
            when (value) {
                is String -> {
                    put("type", "string")
                    put("value", value)
                }

                is Int -> {
                    put("type", "int")
                    put("value", value)
                }

                is Long -> {
                    put("type", "long")
                    put("value", value)
                }

                is Float -> {
                    put("type", "float")
                    put("value", value.toDouble())
                }

                is Boolean -> {
                    put("type", "boolean")
                    put("value", value)
                }

                is Set<*> -> {
                    put("type", "string_set")

                    val array = JSONArray()
                    value.forEach { item ->
                        array.put(item?.toString().orEmpty())
                    }

                    put("value", array)
                }

                else -> {
                    put("type", "string")
                    put("value", value?.toString().orEmpty())
                }
            }
        }
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(millis))
    }
}

