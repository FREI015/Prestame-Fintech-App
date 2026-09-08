package com.controlprestamos.features.clients.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.clients.domain.model.CreateClientInput
import com.controlprestamos.features.clients.domain.model.UpdateClientInput
import com.controlprestamos.features.clients.domain.repository.ClientRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object LocalClientRepository : ClientRepository {

    private const val PREFS_NAME = "control_prestamos_clients"
    private const val KEY_CLIENTS = "clients"

    private val clients = mutableStateListOf<Client>()
    private var preferences: SharedPreferences? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadClients()
        initialized = true
    }

    fun reloadFromStorage(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadClients()
        initialized = true
    }

    override fun getClients(): List<Client> {
        return clients
    }

    fun getActiveClients(): List<Client> {
        return clients.filter { it.status == ClientStatus.ACTIVE }
    }

    fun getInactiveClients(): List<Client> {
        return clients.filter { it.status == ClientStatus.INACTIVE }
    }

    override fun getClientById(clientId: String): Client? {
        return clients.firstOrNull { it.id == clientId }
    }

    override fun createClient(input: CreateClientInput): Client {
        val duplicate = findDuplicateClient(
            documentId = input.documentId,
            phone = input.phone
        )

        if (duplicate != null) {
            return duplicate
        }

        val client = Client(
            id = UUID.randomUUID().toString(),
            firstName = input.firstName.trim(),
            lastName = input.lastName.trim(),
            documentId = input.documentId.trim(),
            phone = input.phone.trim(),
            address = input.address.trim(),
            notes = input.notes.trim(),
            status = ClientStatus.ACTIVE,
            createdAtMillis = System.currentTimeMillis()
        )

        clients.add(index = 0, element = client)
        saveClients()

        return client
    }

    override fun updateClient(input: UpdateClientInput): Boolean {
        val index = clients.indexOfFirst { it.id == input.clientId }

        if (index < 0) return false

        val duplicate = findDuplicateClient(
            documentId = input.documentId,
            phone = input.phone,
            ignoreClientId = input.clientId
        )

        if (duplicate != null) return false

        val current = clients[index]

        clients[index] = current.copy(
            firstName = input.firstName.trim(),
            lastName = input.lastName.trim(),
            documentId = input.documentId.trim(),
            phone = input.phone.trim(),
            address = input.address.trim(),
            notes = input.notes.trim()
        )

        saveClients()

        return true
    }

    override fun updateClientStatus(
        clientId: String,
        status: ClientStatus
    ): Boolean {
        val index = clients.indexOfFirst { it.id == clientId }

        if (index < 0) return false

        val current = clients[index]

        clients[index] = current.copy(
            status = status
        )

        saveClients()

        return true
    }

    fun findDuplicateClient(
        documentId: String,
        phone: String,
        ignoreClientId: String? = null
    ): Client? {
        val normalizedDocument = normalizeDocument(documentId)
        val normalizedPhone = normalizePhone(phone)

        return clients.firstOrNull { client ->
            val isSameClient = ignoreClientId != null && client.id == ignoreClientId

            if (isSameClient) {
                false
            } else {
                val sameDocument = normalizedDocument.isNotBlank() &&
                    normalizeDocument(client.documentId) == normalizedDocument

                val samePhone = normalizedPhone.length >= 7 &&
                    normalizePhone(client.phone) == normalizedPhone

                sameDocument || samePhone
            }
        }
    }

    fun buildDuplicateClientMessage(client: Client): String {
        return "Ya existe un cliente registrado con ese documento o teléfono: ${client.fullName}."
    }

    fun hasFinancialHistory(clientId: String): Boolean {
        val loans = LocalLoanRepository.getLoansByClient(clientId)
        val payments = LocalPaymentRepository.getPaymentHistoryByClient(clientId)

        return loans.isNotEmpty() || payments.isNotEmpty()
    }

    fun canSafelyHardDeleteClient(clientId: String): Boolean {
        return !hasFinancialHistory(clientId)
    }

    fun archiveClientSafely(clientId: String): Boolean {
        val client = getClientById(clientId) ?: return false

        if (client.status == ClientStatus.INACTIVE) {
            return true
        }

        return updateClientStatus(
            clientId = clientId,
            status = ClientStatus.INACTIVE
        )
    }

    fun reactivateClient(clientId: String): Boolean {
        val client = getClientById(clientId) ?: return false

        if (client.status == ClientStatus.ACTIVE) {
            return true
        }

        return updateClientStatus(
            clientId = clientId,
            status = ClientStatus.ACTIVE
        )
    }

    private fun loadClients() {
        clients.clear()

        val rawJson = preferences
            ?.getString(KEY_CLIENTS, "[]")
            .orEmpty()

        val array = runCatching {
            JSONArray(rawJson)
        }.getOrElse {
            JSONArray()
        }

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            clients.add(json.toClient())
        }
    }

    private fun saveClients() {
        val array = JSONArray()

        clients.forEach { client ->
            array.put(client.toJson())
        }

        preferences
            ?.edit()
            ?.putString(KEY_CLIENTS, array.toString())
            ?.apply()
    }

    private fun Client.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("firstName", firstName)
            put("lastName", lastName)
            put("documentId", documentId)
            put("phone", phone)
            put("address", address)
            put("notes", notes)
            put("status", status.name)
            put("createdAtMillis", createdAtMillis)
        }
    }

    private fun JSONObject.toClient(): Client {
        return Client(
            id = optString("id"),
            firstName = optString("firstName"),
            lastName = optString("lastName"),
            documentId = optString("documentId"),
            phone = optString("phone"),
            address = optString("address"),
            notes = optString("notes"),
            status = statusFromName(optString("status")),
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
        )
    }

    private fun statusFromName(value: String): ClientStatus {
        return runCatching {
            ClientStatus.valueOf(value)
        }.getOrDefault(ClientStatus.ACTIVE)
    }

    private fun normalizeDocument(value: String): String {
        return value
            .trim()
            .uppercase()
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")
    }

    private fun normalizePhone(value: String): String {
        return value.filter { it.isDigit() || it == '+' }
    }
}

