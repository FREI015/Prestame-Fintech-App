package com.controlprestamos.features.clients.domain.profile

data class ClientDocumentProfile(
    val id: String,
    val type: ClientDocumentType,
    val uri: String,
    val fileName: String = "",
    val sha256: String = "",
    val verificationStatus: DocumentVerificationStatus =
        DocumentVerificationStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class ClientDocumentType {
    IDENTITY_DOCUMENT,
    PASSPORT,
    PROOF_OF_ADDRESS,
    EMPLOYMENT_PROOF,
    INCOME_PROOF,
    BANK_REFERENCE,
    CLIENT_PHOTO,
    HOME_PHOTO,
    OTHER
}

enum class DocumentVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    EXPIRED
}

