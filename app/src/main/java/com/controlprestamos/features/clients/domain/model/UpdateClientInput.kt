package com.controlprestamos.features.clients.domain.model

data class UpdateClientInput(
    val clientId: String,
    val firstName: String,
    val lastName: String,
    val documentId: String,
    val phone: String,
    val address: String,
    val notes: String
)

