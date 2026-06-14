package com.controlprestamos.features.payments.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.domain.model.CreatePaymentInput
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import com.controlprestamos.features.payments.domain.repository.PaymentRepository
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object LocalPaymentRepository : PaymentRepository {

    private const val PREFS_NAME = "control_prestamos_payments"
    private const val KEY_PAYMENTS = "payments"

    private val payments = mutableStateListOf<Payment>()
    private var preferences: SharedPreferences? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadPayments()
        initialized = true
    }

    fun reloadFromStorage(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadPayments()

        initialized = true
    }

    fun getAllPayments(): List<Payment> {
        return payments
            .filter { it.status == PaymentStatus.ACTIVE }
            .sortedByDescending { it.createdAtMillis }
    }

    fun getAllPaymentHistory(): List<Payment> {
        return payments.sortedByDescending { it.createdAtMillis }
    }

    override fun getPaymentsByLoan(loanId: String): List<Payment> {
        return payments
            .filter { it.loanId == loanId && it.status == PaymentStatus.ACTIVE }
            .sortedByDescending { it.createdAtMillis }
    }

    override fun getPaymentHistoryByLoan(loanId: String): List<Payment> {
        return payments
            .filter { it.loanId == loanId }
            .sortedByDescending { it.createdAtMillis }
    }

    override fun getPaymentById(paymentId: String): Payment? {
        return payments.firstOrNull { it.id == paymentId }
    }

    override fun getPaymentsByClient(clientId: String): List<Payment> {
        val clientLoanIds = LocalLoanRepository
            .getLoansByClient(clientId)
            .map { it.id }
            .toSet()

        return payments
            .filter { it.loanId in clientLoanIds && it.status == PaymentStatus.ACTIVE }
            .sortedByDescending { it.createdAtMillis }
    }

    override fun getPaymentHistoryByClient(clientId: String): List<Payment> {
        val clientLoanIds = LocalLoanRepository
            .getLoansByClient(clientId)
            .map { it.id }
            .toSet()

        return payments
            .filter { it.loanId in clientLoanIds }
            .sortedByDescending { it.createdAtMillis }
    }

    override fun getTotalPaidByLoan(loanId: String): Double {
        return payments
            .filter { it.loanId == loanId && it.status == PaymentStatus.ACTIVE }
            .sumOf { it.amount }
    }

    override fun createPayment(input: CreatePaymentInput): Payment {
        val amount = input.amount
            .replace(",", ".")
            .toDouble()

        val payment = Payment(
            id = UUID.randomUUID().toString(),
            loanId = input.loanId,
            amount = amount,
            method = input.method.trim(),
            reference = input.reference.trim(),
            notes = input.notes.trim(),
            status = PaymentStatus.ACTIVE,
            cancellationReason = null,
            cancelledAtMillis = null,
            createdAtMillis = input.paymentDateMillis
        )

        payments.add(index = 0, element = payment)
        savePayments()

        return payment
    }

    override fun cancelPayment(
        paymentId: String,
        reason: String
    ): Boolean {
        val index = payments.indexOfFirst { it.id == paymentId }

        if (index < 0) return false

        val current = payments[index]

        if (current.status == PaymentStatus.CANCELLED) return false

        payments[index] = current.copy(
            status = PaymentStatus.CANCELLED,
            cancellationReason = reason.trim().ifBlank { "Sin motivo registrado" },
            cancelledAtMillis = System.currentTimeMillis()
        )

        savePayments()

        return true
    }

    private fun loadPayments() {
        payments.clear()

        val rawJson = preferences
            ?.getString(KEY_PAYMENTS, "[]")
            .orEmpty()

        val array = runCatching {
            JSONArray(rawJson)
        }.getOrElse {
            JSONArray()
        }

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            payments.add(json.toPayment())
        }
    }

    private fun savePayments() {
        val array = JSONArray()

        payments.forEach { payment ->
            array.put(payment.toJson())
        }

        preferences
            ?.edit()
            ?.putString(KEY_PAYMENTS, array.toString())
            ?.apply()
    }

    private fun Payment.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("loanId", loanId)
            put("amount", amount)
            put("method", method)
            put("reference", reference)
            put("notes", notes)
            put("status", status.name)
            put("cancellationReason", cancellationReason ?: JSONObject.NULL)
            put("cancelledAtMillis", cancelledAtMillis ?: JSONObject.NULL)
            put("createdAtMillis", createdAtMillis)
        }
    }

    private fun JSONObject.toPayment(): Payment {
        return Payment(
            id = optString("id"),
            loanId = optString("loanId"),
            amount = optDouble("amount", 0.0),
            method = optString("method"),
            reference = optString("reference"),
            notes = optString("notes"),
            status = statusFromName(optString("status", PaymentStatus.ACTIVE.name)),
            cancellationReason = if (isNull("cancellationReason")) {
                null
            } else {
                optString("cancellationReason")
            },
            cancelledAtMillis = if (isNull("cancelledAtMillis")) {
                null
            } else {
                optLong("cancelledAtMillis")
            },
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
        )
    }

    private fun statusFromName(value: String): PaymentStatus {
        return runCatching {
            PaymentStatus.valueOf(value)
        }.getOrDefault(PaymentStatus.ACTIVE)
    }
}


