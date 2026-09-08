package com.controlprestamos.features.payments.domain.repository

import com.controlprestamos.features.payments.domain.model.CreatePaymentInput
import com.controlprestamos.features.payments.domain.model.Payment

interface PaymentRepository {
    fun getPaymentsByLoan(loanId: String): List<Payment>

    fun getPaymentHistoryByLoan(loanId: String): List<Payment>

    fun getPaymentById(paymentId: String): Payment?

    fun getPaymentsByClient(clientId: String): List<Payment>

    fun getPaymentHistoryByClient(clientId: String): List<Payment>

    fun getTotalPaidByLoan(loanId: String): Double

    fun createPayment(input: CreatePaymentInput): Payment

    fun cancelPayment(
        paymentId: String,
        reason: String
    ): Boolean
}

