package com.controlprestamos.features.loans.domain.model

enum class RepaymentPlanType(
    val label: String,
    val description: String
) {
    SINGLE_PAYMENT(
        label = "Pago único",
        description = "Una sola cuota por el total a pagar."
    ),
    INSTALLMENTS(
        label = "Por cuotas",
        description = "Divide el total en varias cuotas según el plazo."
    )
}

