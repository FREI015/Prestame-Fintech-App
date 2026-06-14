package com.controlprestamos.features.payments.domain.validation

import com.controlprestamos.features.payments.domain.model.CreatePaymentInput

data class PaymentFormValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object PaymentFormValidator {

    fun validate(
        input: CreatePaymentInput,
        remainingAmount: Double
    ): PaymentFormValidationResult {
        val amount = input.amount
            .replace(",", ".")
            .toDoubleOrNull()

        return when {
            input.loanId.isBlank() -> {
                PaymentFormValidationResult(
                    isValid = false,
                    errorMessage = "No se encontró el préstamo asociado al pago."
                )
            }

            amount == null -> {
                PaymentFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa un monto válido."
                )
            }

            amount <= 0.0 -> {
                PaymentFormValidationResult(
                    isValid = false,
                    errorMessage = "El pago debe ser mayor que cero."
                )
            }

            amount > remainingAmount -> {
                PaymentFormValidationResult(
                    isValid = false,
                    errorMessage = "El pago no puede ser mayor que el saldo pendiente."
                )
            }

            input.method.isBlank() -> {
                PaymentFormValidationResult(
                    isValid = false,
                    errorMessage = "Selecciona o escribe un método de pago."
                )
            }

            else -> PaymentFormValidationResult(isValid = true)
        }
    }
}
