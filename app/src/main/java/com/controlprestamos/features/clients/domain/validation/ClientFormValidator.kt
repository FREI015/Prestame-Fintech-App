package com.controlprestamos.features.clients.domain.validation

import com.controlprestamos.features.clients.domain.model.CreateClientInput

data class ClientFormValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object ClientFormValidator {

    fun validate(input: CreateClientInput): ClientFormValidationResult {
        val normalizedPhone = input.phone.filter { it.isDigit() || it == '+' }

        return when {
            input.firstName.isBlank() -> {
                ClientFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa el nombre del cliente."
                )
            }

            input.lastName.isBlank() -> {
                ClientFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa el apellido del cliente."
                )
            }

            input.documentId.isBlank() -> {
                ClientFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa la cédula o documento del cliente."
                )
            }

            input.phone.isBlank() -> {
                ClientFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa el teléfono del cliente."
                )
            }

            normalizedPhone.length < 7 -> {
                ClientFormValidationResult(
                    isValid = false,
                    errorMessage = "Ingresa un teléfono válido."
                )
            }

            else -> ClientFormValidationResult(isValid = true)
        }
    }
}
