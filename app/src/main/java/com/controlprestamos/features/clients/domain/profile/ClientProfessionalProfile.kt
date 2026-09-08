package com.controlprestamos.features.clients.domain.profile

data class ClientProfessionalProfile(
    val clientId: String,

    val contact: ClientContactProfile =
        ClientContactProfile(),

    val address: ClientAddressProfile =
        ClientAddressProfile(),

    val employment: ClientEmploymentProfile =
        ClientEmploymentProfile(),

    val financial: ClientFinancialProfile =
        ClientFinancialProfile(),

    val references: List<ClientReferenceProfile> =
        emptyList(),

    val documents: List<ClientDocumentProfile> =
        emptyList(),

    val risk: ClientRiskProfile =
        ClientRiskProfile(),

    val internalNotes: String = "",

    val registeredBy: String = "",

    val referredBy: String = "",

    val updatedAtMillis: Long =
        System.currentTimeMillis()
)

