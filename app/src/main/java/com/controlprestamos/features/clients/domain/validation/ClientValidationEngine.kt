package com.controlprestamos.features.clients.domain.validation


object ClientValidationEngine {


    fun validateBasicClient(
        name:String,
        lastname:String,
        document:String,
        phone:String
    ):ValidationResult {


        val errors = mutableListOf<String>()


        val identity =
            ClientIdentityValidator.validate(
                name,
                lastname,
                document
            )


        val contact =
            ClientContactValidator.validatePhone(
                phone
            )


        errors.addAll(identity.errors)

        errors.addAll(contact.errors)


        return ValidationResult(
            errors.isEmpty(),
            errors
        )

    }

}

