package com.controlprestamos.features.loans.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.loans.domain.model.UpdateLoanInput
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object LocalLoanRepository {

    private const val PREFS_NAME = "control_prestamos_loans"
    private const val KEY_LOANS = "loans"

    private val loans = mutableStateListOf<Loan>()
    private var preferences: SharedPreferences? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadLoans()
        initialized = true
    }

    fun reloadFromStorage(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadLoans()

        initialized = true
    }

    fun getAllLoans(): List<Loan> {
        return loans.sortedByDescending { it.createdAtMillis }
    }

    fun getActiveLoans(): List<Loan> {
        return loans
            .filter { it.status == LoanStatus.ACTIVE }
            .sortedByDescending { it.createdAtMillis }
    }

    fun getCancelledLoans(): List<Loan> {
        return loans
            .filter { it.status == LoanStatus.CANCELLED }
            .sortedByDescending { it.cancelledAtMillis ?: it.createdAtMillis }
    }

    fun getLoansByClient(clientId: String): List<Loan> {
        return loans
            .filter { it.clientId == clientId }
            .sortedByDescending { it.createdAtMillis }
    }

    fun getLoanById(loanId: String): Loan? {
        return loans.firstOrNull { it.id == loanId }
    }

    fun createLoan(input: CreateLoanInput): Loan {
        val principal = input.principalAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
        val interest = input.interestRatePercent.replace(",", ".").toDoubleOrNull() ?: 0.0
        val term = input.termInDays.toIntOrNull() ?: 1
        val safeTerm = if (term <= 0) 1 else term

        val totalExpected = principal + (principal * interest / 100.0)
        val estimatedDailyAmount = totalExpected / safeTerm
        val now = System.currentTimeMillis()

        val loan = Loan(
            id = UUID.randomUUID().toString(),
            clientId = input.clientId,
            principalAmount = principal,
            interestRatePercent = interest,
            termInDays = safeTerm,
            description = input.description.trim(),
            repaymentPlanType = input.repaymentPlanType,
            totalExpectedAmount = totalExpected,
            estimatedDailyAmount = estimatedDailyAmount,
            createdAtMillis = now,
            startDateMillis = input.startDateMillis,
            status = LoanStatus.ACTIVE,
            cancellationReason = null,
            cancelledAtMillis = null
        )

        loans.add(index = 0, element = loan)
        saveLoans()

        return loan
    }

    fun updateLoan(input: UpdateLoanInput): Loan? {
        val index = loans.indexOfFirst { it.id == input.loanId }

        if (index < 0) return null

        val current = loans[index]

        if (current.status == LoanStatus.CANCELLED) return null

        val hasPaymentHistory = LocalPaymentRepository
            .getPaymentHistoryByLoan(input.loanId)
            .isNotEmpty()

        if (hasPaymentHistory || current.status == LoanStatus.PAID) {
            return null
        }

        val principal = input.principalAmount.replace(",", ".").toDoubleOrNull() ?: return null
        val interest = input.interestRatePercent.replace(",", ".").toDoubleOrNull() ?: return null
        val term = input.termInDays.toIntOrNull() ?: return null
        val safeTerm = if (term <= 0) 1 else term

        val totalExpected = principal + (principal * interest / 100.0)
        val estimatedDailyAmount = totalExpected / safeTerm

        val updatedLoan = current.copy(
            principalAmount = principal,
            interestRatePercent = interest,
            termInDays = safeTerm,
            description = input.description.trim(),
            repaymentPlanType = input.repaymentPlanType,
            totalExpectedAmount = totalExpected,
            estimatedDailyAmount = estimatedDailyAmount,
            startDateMillis = input.startDateMillis ?: current.startDateMillis
        )

        loans[index] = updatedLoan
        saveLoans()

        return updatedLoan
    }

    fun updateLoanDescription(
        loanId: String,
        description: String
    ): Loan? {
        val index = loans.indexOfFirst { it.id == loanId }

        if (index < 0) return null

        val current = loans[index]

        if (current.status == LoanStatus.CANCELLED) return null

        val updatedLoan = current.copy(
            description = description.trim()
        )

        loans[index] = updatedLoan
        saveLoans()

        return updatedLoan
    }
    fun markLoanAsPaid(loanId: String): Loan? {
        val index = loans.indexOfFirst { it.id == loanId }

        if (index < 0) return null

        val current = loans[index]

        if (current.status == LoanStatus.CANCELLED) return null

        val updatedLoan = current.copy(status = LoanStatus.PAID)

        loans[index] = updatedLoan
        saveLoans()

        return updatedLoan
    }

    fun updateLoanStatusByBalance(
        loanId: String,
        remainingAmount: Double
    ): Loan? {
        val index = loans.indexOfFirst { it.id == loanId }

        if (index < 0) return null

        val current = loans[index]

        if (current.status == LoanStatus.CANCELLED) return null

        val newStatus = if (remainingAmount <= 0.000001) {
            LoanStatus.PAID
        } else {
            LoanStatus.ACTIVE
        }

        if (current.status == newStatus) {
            return current
        }

        val updatedLoan = current.copy(status = newStatus)

        loans[index] = updatedLoan
        saveLoans()

        return updatedLoan
    }

    fun cancelLoan(
        loanId: String,
        reason: String
    ): Boolean {
        val index = loans.indexOfFirst { it.id == loanId }

        if (index < 0) return false

        val current = loans[index]

        if (current.status == LoanStatus.CANCELLED) return false

        loans[index] = current.copy(
            status = LoanStatus.CANCELLED,
            cancellationReason = reason.trim().ifBlank { "Sin motivo registrado" },
            cancelledAtMillis = System.currentTimeMillis()
        )

        saveLoans()

        return true
    }

    private fun loadLoans() {
        loans.clear()

        val rawJson = preferences
            ?.getString(KEY_LOANS, "[]")
            .orEmpty()

        val array = runCatching {
            JSONArray(rawJson)
        }.getOrElse {
            JSONArray()
        }

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            loans.add(json.toLoan())
        }
    }

    private fun saveLoans() {
        val array = JSONArray()

        loans.forEach { loan ->
            array.put(loan.toJson())
        }

        preferences
            ?.edit()
            ?.putString(KEY_LOANS, array.toString())
            ?.apply()
    }

    private fun Loan.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("clientId", clientId)
            put("principalAmount", principalAmount)
            put("interestRatePercent", interestRatePercent)
            put("termInDays", termInDays)
            put("description", description)
            put("repaymentPlanType", repaymentPlanType.name)
            put("totalExpectedAmount", totalExpectedAmount)
            put("estimatedDailyAmount", estimatedDailyAmount)
            put("createdAtMillis", createdAtMillis)
            put("startDateMillis", startDateMillis)
            put("status", status.name)
            put("cancellationReason", cancellationReason ?: JSONObject.NULL)
            put("cancelledAtMillis", cancelledAtMillis ?: JSONObject.NULL)
        }
    }

    private fun JSONObject.toLoan(): Loan {
        val principal = optDouble("principalAmount", 0.0)
        val interest = optDouble("interestRatePercent", 0.0)
        val term = optInt("termInDays", 1).let { if (it <= 0) 1 else it }
        val totalExpected = optDouble(
            "totalExpectedAmount",
            principal + (principal * interest / 100.0)
        )

        val createdAtMillis = optLong(
            "createdAtMillis",
            System.currentTimeMillis()
        )

        return Loan(
            id = optString("id"),
            clientId = optString("clientId"),
            principalAmount = principal,
            interestRatePercent = interest,
            termInDays = term,
            description = optString("description"),
            repaymentPlanType = repaymentPlanTypeFromName(
                value = optString("repaymentPlanType")
            ),
            totalExpectedAmount = totalExpected,
            estimatedDailyAmount = optDouble(
                "estimatedDailyAmount",
                totalExpected / term
            ),
            createdAtMillis = createdAtMillis,
            startDateMillis = optLong(
                "startDateMillis",
                createdAtMillis
            ),
            status = loanStatusFromName(
                value = optString("status", LoanStatus.ACTIVE.name)
            ),
            cancellationReason = if (isNull("cancellationReason")) {
                null
            } else {
                optString("cancellationReason")
            },
            cancelledAtMillis = if (isNull("cancelledAtMillis")) {
                null
            } else {
                optLong("cancelledAtMillis")
            }
        )
    }

    private fun repaymentPlanTypeFromName(value: String): RepaymentPlanType {
        return runCatching {
            RepaymentPlanType.valueOf(value)
        }.getOrDefault(RepaymentPlanType.INSTALLMENTS)
    }

    private fun loanStatusFromName(value: String): LoanStatus {
        return runCatching {
            LoanStatus.valueOf(value)
        }.getOrDefault(LoanStatus.ACTIVE)
    }
}




