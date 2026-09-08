package com.controlprestamos.features.loans.domain.model

data class Loan(
    val id: String,
    val clientId: String,
    val principalAmount: Double,
    val interestRatePercent: Double,
    val termInDays: Int,
    val description: String,
    val repaymentPlanType: RepaymentPlanType = RepaymentPlanType.INSTALLMENTS,
    val totalExpectedAmount: Double,
    val estimatedDailyAmount: Double,
    val createdAtMillis: Long,
    val startDateMillis: Long = createdAtMillis,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val cancellationReason: String? = null,
    val cancelledAtMillis: Long? = null
)

