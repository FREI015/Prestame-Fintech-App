package com.controlprestamos.features.loans.domain.model

data class CreateLoanInput(
    val clientId: String,
    val principalAmount: String,
    val interestRatePercent: String,
    val termInDays: String,
    val description: String,
    val repaymentPlanType: RepaymentPlanType = RepaymentPlanType.INSTALLMENTS,
    val startDateMillis: Long = System.currentTimeMillis()
)
