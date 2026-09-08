package com.controlprestamos.features.clients.domain.profile

data class ClientContactProfile(
    val primaryPhone: String = "",
    val secondaryPhone: String = "",
    val whatsappPhone: String = "",
    val email: String = "",
    val preferredContactMethod: ContactMethod = ContactMethod.PHONE
)

enum class ContactMethod {
    PHONE,
    WHATSAPP,
    EMAIL
}

