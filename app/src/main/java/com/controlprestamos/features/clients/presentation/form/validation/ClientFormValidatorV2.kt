package com.controlprestamos.features.clients.presentation.form.validation


object ClientFormValidatorV2 {


    fun validateIdentity(

        name:String,

        lastname:String,

        document:String

    ):FormValidationResult {


        val errors =
            mutableListOf<FieldError>()


        if(name.length < 2){

            errors.add(
                FieldError(
                    "name",
                    "Nombre invalido"
                )
            )

        }


        if(lastname.length < 2){

            errors.add(
                FieldError(
                    "lastname",
                    "Apellido invalido"
                )
            )

        }


        if(document.isBlank()){

            errors.add(
                FieldError(
                    "document",
                    "Documento obligatorio"
                )
            )

        }


        return FormValidationResult(

            errors.isEmpty(),

            errors

        )


    }


}

