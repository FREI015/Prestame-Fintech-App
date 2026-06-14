package com.controlprestamos.features.payments.domain.model

data class Payment(
    val id: String,
    val loanId: String,
    val amount: Double,
    val method: String,
    val reference: String,
    val notes: String,
    val status: PaymentStatus,
    val cancellationReason: String?,
    val cancelledAtMillis: Long?,
    val createdAtMillis: Long
)
