package com.controlprestamos.features.clients.domain.model

data class CreateClientInput(
    val firstName: String,
    val lastName: String,
    val documentId: String,
    val phone: String,
    val address: String,
    val notes: String
)
