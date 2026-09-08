package com.controlprestamos.features.installments.domain.model

data class Installment(
    val id: String,
    val loanId: String,
    val number: Int,
    val dueDateMillis: Long,
    val expectedAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val status: InstallmentStatus,
    val paidAtMillis: Long?,
    val createdAtMillis: Long
)

