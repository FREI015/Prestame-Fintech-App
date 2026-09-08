package com.controlprestamos.features.clients.domain.validation

object ClientContactValidator {


    fun validatePhone(phone:String):ValidationResult {


        val errors = mutableListOf<String>()


        if(phone.length < 7)
            errors.add("Telefono invalido")


        return ValidationResult(
            errors.isEmpty(),
            errors
        )

    }

}

