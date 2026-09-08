package com.controlprestamos.features.clients.domain.profile

data class ClientRiskProfile(
    val score: Int? = null,
    val level: ClientRiskLevel = ClientRiskLevel.NOT_EVALUATED,
    val reason: String = "",
    val evaluatedAtMillis: Long? = null
)

enum class ClientRiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
    NOT_EVALUATED
}

