package com.controlprestamos.features.clients.presentation.form.validation


data class FormValidationResult(

    val valid:Boolean,

    val errors:List<FieldError> = emptyList()

)

