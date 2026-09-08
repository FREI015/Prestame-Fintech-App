package com.controlprestamos.features.clients.domain.validation

object ClientIdentityValidator {


    fun validate(
        firstName:String,
        lastName:String,
        document:String
    ):ValidationResult {


        val errors = mutableListOf<String>()


        if(firstName.isBlank())
            errors.add("Nombre obligatorio")


        if(lastName.isBlank())
            errors.add("Apellido obligatorio")


        if(document.isBlank())
            errors.add("Documento obligatorio")


        return ValidationResult(
            errors.isEmpty(),
            errors
        )
    }
}

