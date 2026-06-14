package com.controlprestamos.features.payments.domain.model

enum class PaymentStatus(
    val label: String
) {
    ACTIVE("Activo"),
    CANCELLED("Anulado")
}
