package com.controlprestamos.features.clients.domain.validation


object ClientFinancialValidator {


    fun validateIncome(
        income:Double?
    ):ValidationResult {


        val errors=mutableListOf<String>()


        if(income != null && income < 0)
            errors.add("Ingreso invalido")


        return ValidationResult(
            errors.isEmpty(),
            errors
        )
    }

}

