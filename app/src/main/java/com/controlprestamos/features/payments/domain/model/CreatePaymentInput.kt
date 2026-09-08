package com.controlprestamos.features.payments.domain.model

data class CreatePaymentInput(
    val loanId: String,
    val amount: String,
    val method: String,
    val reference: String = "",
    val notes: String,
    val paymentDateMillis: Long = System.currentTimeMillis()
)

