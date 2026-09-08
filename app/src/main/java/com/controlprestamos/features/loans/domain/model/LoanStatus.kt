package com.controlprestamos.features.loans.domain.model

enum class LoanStatus(
    val label: String
) {
    ACTIVE("Activo"),
    PAID("Pagado"),
    CANCELLED("Cancelado")
}


