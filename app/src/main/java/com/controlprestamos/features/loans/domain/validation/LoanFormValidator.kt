package com.controlprestamos.features.loans.domain.validation

import com.controlprestamos.features.loans.domain.model.CreateLoanInput

data class LoanFormValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object LoanFormValidator {

    fun validate(input: CreateLoanInput): LoanFormValidationResult {
        val amount = input.principalAmount
            .replace(",", ".")
            .toDoubleOrNull()

        val interest = input.interestRatePercent
            .replace(",", ".")
            .toDoubleOrNull()

        val term = input.termInDays.toIntOrNull()

        return when {
            input.clientId.isBlank() -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "Selecciona un cliente para crear el préstamo."
                )
            }

            amount == null -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa un monto válido."
                )
            }

            amount <= 0.0 -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "El monto prestado debe ser mayor que cero."
                )
            }

            interest == null -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa un porcentaje de interés válido."
                )
            }

            interest < 0.0 -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "El interés no puede ser negativo."
                )
            }

            term == null -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa una duración válida."
                )
            }

            term <= 0 -> {
                LoanFormValidationResult(
                    isValid = false,
                    errorMessage = "La duración debe ser mayor que cero."
                )
            }

            else -> LoanFormValidationResult(isValid = true)
        }
    }
}
