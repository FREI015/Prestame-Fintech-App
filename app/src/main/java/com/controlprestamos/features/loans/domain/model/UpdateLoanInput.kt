package com.controlprestamos.features.loans.domain.model

data class UpdateLoanInput(
    val loanId: String,
    val principalAmount: String,
    val interestRatePercent: String,
    val termInDays: String,
    val description: String,
    val repaymentPlanType: RepaymentPlanType = RepaymentPlanType.INSTALLMENTS,
    val startDateMillis: Long? = null
)
