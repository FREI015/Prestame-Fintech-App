package com.controlprestamos.features.clients.domain.profile

data class ClientFinancialProfile(
    val monthlyIncome: Double? = null,
    val additionalIncome: Double? = null,
    val monthlyExpenses: Double? = null,
    val existingDebtPayments: Double? = null,
    val declaredAssetsValue: Double? = null,
    val declaredLiabilitiesValue: Double? = null
) {

    val estimatedAvailableCapacity: Double?
        get() {
            val income = monthlyIncome ?: return null
            val extra = additionalIncome ?: 0.0
            val expenses = monthlyExpenses ?: 0.0
            val debts = existingDebtPayments ?: 0.0

            return (income + extra - expenses - debts)
                .coerceAtLeast(0.0)
        }
}

