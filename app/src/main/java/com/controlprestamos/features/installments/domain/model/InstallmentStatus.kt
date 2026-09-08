package com.controlprestamos.features.installments.domain.model

enum class InstallmentStatus(
    val label: String
) {
    PENDING("Pendiente"),
    PARTIAL("Parcial"),
    PAID("Pagada"),
    OVERDUE("Vencida"),
    CANCELLED("Cancelada")
}

