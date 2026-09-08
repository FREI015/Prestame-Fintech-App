package com.controlprestamos.features.clients.presentation.form.validation


interface ValidationRule {


    fun validate():FieldError?

}

