package com.controlprestamos.features.clients.domain.profile

data class ClientEmploymentProfile(
    val occupation: String = "",
    val companyName: String = "",
    val position: String = "",
    val workplaceAddress: String = "",
    val employmentType: EmploymentType = EmploymentType.UNKNOWN,
    val monthsAtCurrentJob: Int? = null,
    val monthlyIncome: Double? = null
)

enum class EmploymentType {
    EMPLOYEE,
    SELF_EMPLOYED,
    BUSINESS_OWNER,
    INDEPENDENT,
    RETIRED,
    UNEMPLOYED,
    OTHER,
    UNKNOWN
}

