package com.controlprestamos.features.clients.domain.model

enum class ClientStatus(
    val label: String
) {
    ACTIVE("Activo"),
    INACTIVE("Inactivo")
}

data class Client(
    val id: String,
    val firstName: String,
    val lastName: String,
    val documentId: String,
    val phone: String,
    val address: String,
    val notes: String,
    val status: ClientStatus,
    val createdAtMillis: Long
) {
    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
}

