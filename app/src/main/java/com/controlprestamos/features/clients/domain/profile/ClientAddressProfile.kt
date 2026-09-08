package com.controlprestamos.features.clients.domain.profile

data class ClientAddressProfile(
    val country: String = "",
    val state: String = "",
    val city: String = "",
    val municipality: String = "",
    val parish: String = "",
    val addressLine: String = "",
    val reference: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val verified: Boolean = false
)

