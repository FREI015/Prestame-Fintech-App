package com.controlprestamos.features.installments.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import java.util.Calendar
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object LocalInstallmentRepository {

    private const val PREFS_NAME = "control_prestamos_installments"
    private const val KEY_INSTALLMENTS = "installments"

    private val installments = mutableStateListOf<Installment>()
    private var preferences: SharedPreferences? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return

        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadInstallments()
        refreshOverdueStatuses()
        initialized = true
    }

    fun reloadFromStorage(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadInstallments()
        refreshOverdueStatuses()
        initialized = true
    }

    fun getAllInstallments(): List<Installment> {
        refreshOverdueStatuses()

        return installments.sortedWith(
            compareBy<Installment> { it.dueDateMillis }
                .thenBy { it.number }
        )
    }

    fun getInstallmentsByLoan(loanId: String): List<Installment> {
        refreshOverdueStatuses()

        return installments
            .filter { it.loanId == loanId }
            .sortedBy { it.number }
    }

    fun getPendingInstallmentsByLoan(loanId: String): List<Installment> {
        refreshOverdueStatuses()

        return installments
            .filter { installment ->
                installment.loanId == loanId &&
                    installment.status in listOf(
                        InstallmentStatus.PENDING,
                        InstallmentStatus.PARTIAL,
                        InstallmentStatus.OVERDUE
                    ) &&
                    installment.pendingAmount > 0.0
            }
            .sortedBy { it.number }
    }

    fun getNextPendingInstallment(loanId: String): Installment? {
        return getPendingInstallmentsByLoan(loanId).firstOrNull()
    }

    fun generateInstallmentsForLoan(loan: Loan): List<Installment> {
        val existingInstallments = installments
            .filter { it.loanId == loan.id }
            .sortedBy { it.number }

        if (existingInstallments.isNotEmpty()) {
            return existingInstallments
        }

        val installmentCount = when (loan.repaymentPlanType) {
            RepaymentPlanType.SINGLE_PAYMENT -> 1
            RepaymentPlanType.INSTALLMENTS -> if (loan.termInDays <= 0) 1 else loan.termInDays
        }

        val installmentAmount = loan.totalExpectedAmount / installmentCount

        val createdInstallments = (1..installmentCount).map { number ->
            val daysToAdd = when (loan.repaymentPlanType) {
                RepaymentPlanType.SINGLE_PAYMENT -> if (loan.termInDays <= 0) 1 else loan.termInDays
                RepaymentPlanType.INSTALLMENTS -> number
            }

            Installment(
                id = UUID.randomUUID().toString(),
                loanId = loan.id,
                number = number,
                dueDateMillis = calculateDueDateMillis(
                    startMillis = loan.startDateMillis,
                    daysToAdd = daysToAdd
                ),
                expectedAmount = installmentAmount,
                paidAmount = 0.0,
                pendingAmount = installmentAmount,
                status = InstallmentStatus.PENDING,
                paidAtMillis = null,
                createdAtMillis = System.currentTimeMillis()
            )
        }

        installments.addAll(createdInstallments)
        refreshOverdueStatuses()
        saveInstallments()

        return createdInstallments
    }

    fun applyPaymentToInstallments(
        loan: Loan,
        paymentAmount: Double,
        paidAtMillis: Long = System.currentTimeMillis()
    ) {
        if (paymentAmount <= 0.0) return

        if (getInstallmentsByLoan(loan.id).isEmpty()) {
            generateInstallmentsForLoan(loan)
        }

        var remainingPaymentAmount = paymentAmount
        val updatedInstallmentsById = mutableMapOf<String, Installment>()

        getPendingInstallmentsByLoan(loan.id).forEach { installment ->
            if (remainingPaymentAmount <= 0.0) return@forEach

            val amountToApply = minOf(
                installment.pendingAmount,
                remainingPaymentAmount
            )

            val newPaidAmount = installment.paidAmount + amountToApply
            val newPendingAmount = maxOf(
                installment.expectedAmount - newPaidAmount,
                0.0
            )

            val newStatus = when {
                newPendingAmount <= 0.000001 -> InstallmentStatus.PAID
                newPaidAmount > 0.0 -> InstallmentStatus.PARTIAL
                else -> installment.status
            }

            val updatedInstallment = installment.copy(
                paidAmount = newPaidAmount,
                pendingAmount = newPendingAmount,
                status = newStatus,
                paidAtMillis = if (newStatus == InstallmentStatus.PAID) {
                    paidAtMillis
                } else {
                    installment.paidAtMillis
                }
            )

            updatedInstallmentsById[installment.id] = updatedInstallment
            remainingPaymentAmount -= amountToApply
        }

        if (updatedInstallmentsById.isEmpty()) return

        val refreshedInstallments = installments.map { installment ->
            updatedInstallmentsById[installment.id] ?: installment
        }

        installments.clear()
        installments.addAll(refreshedInstallments)

        refreshOverdueStatuses()
        saveInstallments()
    }

    fun applyPaymentToInstallments(
        loanId: String,
        amount: Double,
        paidAtMillis: Long = System.currentTimeMillis()
    ) {
        val loan = LocalLoanRepository.getLoanById(loanId) ?: return

        applyPaymentToInstallments(
            loan = loan,
            paymentAmount = amount,
            paidAtMillis = paidAtMillis
        )
    }

    fun refreshOverdueStatuses() {
        val todayStartMillis = startOfTodayMillis()
        var changed = false

        val refreshed = installments.map { installment ->
            val shouldBeOverdue =
                installment.dueDateMillis < todayStartMillis &&
                    installment.pendingAmount > 0.0 &&
                    installment.status in listOf(
                        InstallmentStatus.PENDING,
                        InstallmentStatus.PARTIAL
                    )

            if (shouldBeOverdue) {
                changed = true
                installment.copy(status = InstallmentStatus.OVERDUE)
            } else {
                installment
            }
        }

        if (changed) {
            installments.clear()
            installments.addAll(refreshed)
            saveInstallments()
        }
    }

    private fun calculateDueDateMillis(
        startMillis: Long,
        daysToAdd: Int
    ): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.DAY_OF_YEAR, daysToAdd)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun loadInstallments() {
        installments.clear()

        val rawJson = preferences
            ?.getString(KEY_INSTALLMENTS, "[]")
            .orEmpty()

        val array = runCatching {
            JSONArray(rawJson)
        }.getOrElse {
            JSONArray()
        }

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            installments.add(json.toInstallment())
        }
    }

    private fun saveInstallments() {
        val array = JSONArray()

        installments.forEach { installment ->
            array.put(installment.toJson())
        }

        preferences
            ?.edit()
            ?.putString(KEY_INSTALLMENTS, array.toString())
            ?.apply()
    }

    private fun Installment.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("loanId", loanId)
            put("number", number)
            put("dueDateMillis", dueDateMillis)
            put("expectedAmount", expectedAmount)
            put("paidAmount", paidAmount)
            put("pendingAmount", pendingAmount)
            put("status", status.name)
            put("paidAtMillis", paidAtMillis ?: JSONObject.NULL)
            put("createdAtMillis", createdAtMillis)
        }
    }

    private fun JSONObject.toInstallment(): Installment {
        return Installment(
            id = optString("id"),
            loanId = optString("loanId"),
            number = optInt("number", 1),
            dueDateMillis = optLong("dueDateMillis", System.currentTimeMillis()),
            expectedAmount = optDouble("expectedAmount", 0.0),
            paidAmount = optDouble("paidAmount", 0.0),
            pendingAmount = optDouble("pendingAmount", 0.0),
            status = statusFromName(optString("status")),
            paidAtMillis = if (isNull("paidAtMillis")) {
                null
            } else {
                optLong("paidAtMillis")
            },
            createdAtMillis = optLong("createdAtMillis", System.currentTimeMillis())
        )
    }

    private fun statusFromName(value: String): InstallmentStatus {
        return runCatching {
            InstallmentStatus.valueOf(value)
        }.getOrDefault(InstallmentStatus.PENDING)
    }

    @Suppress("UNUSED_PARAMETER")
    fun rebuildInstallmentsForLoan(
        loanId: String,
        paidAtMillis: Long = System.currentTimeMillis()
    ) {
        val loan = LocalLoanRepository.getLoanById(loanId) ?: return

        if (getInstallmentsByLoan(loanId).isEmpty()) {
            generateInstallmentsForLoan(loan)
        }

        val resetInstallments = installments.map { installment ->
            if (installment.loanId == loanId) {
                installment.copy(
                    paidAmount = 0.0,
                    pendingAmount = installment.expectedAmount,
                    status = InstallmentStatus.PENDING,
                    paidAtMillis = null
                )
            } else {
                installment
            }
        }

        installments.clear()
        installments.addAll(resetInstallments)

        refreshOverdueStatuses()
        saveInstallments()

        val activePayments = LocalPaymentRepository
            .getPaymentsByLoan(loanId)
            .sortedWith(
                compareBy(
                    { payment -> payment.createdAtMillis },
                    { payment -> payment.id }
                )
            )

        activePayments.forEach { payment ->
            applyPaymentToInstallments(
                loan = loan,
                paymentAmount = payment.amount,
                paidAtMillis = payment.createdAtMillis
            )
        }

        val activePaidAmount = activePayments.sumOf { payment ->
            payment.amount
        }

        val remainingAmount = maxOf(
            loan.totalExpectedAmount - activePaidAmount,
            0.0
        )

        LocalLoanRepository.updateLoanStatusByBalance(
            loanId = loanId,
            remainingAmount = remainingAmount
        )

        refreshOverdueStatuses()
        saveInstallments()
    }
    fun removeInstallmentsForLoan(loanId: String) {
        val remainingInstallments = installments.filterNot { it.loanId == loanId }

        installments.clear()
        installments.addAll(remainingInstallments)

        saveInstallments()
    }

    fun cancelInstallmentsForLoan(loanId: String) {
        val refreshedInstallments = installments.map { installment ->
            if (installment.loanId == loanId) {
                installment.copy(status = InstallmentStatus.CANCELLED)
            } else {
                installment
            }
        }

        installments.clear()
        installments.addAll(refreshedInstallments)

        saveInstallments()
    }
}






