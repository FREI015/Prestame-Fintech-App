package com.controlprestamos.features.clients.domain.profile

data class ClientReferenceProfile(
    val id: String,
    val fullName: String,
    val phone: String,
    val relationship: String = "",
    val yearsKnown: Int? = null,
    val verificationStatus: ReferenceVerificationStatus =
        ReferenceVerificationStatus.PENDING,
    val verificationNotes: String = ""
)

enum class ReferenceVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    UNREACHABLE
}

