package com.controlprestamos.features.clients.domain.validation

data class ValidationResult(
    val valid:Boolean,
    val errors:List<String> = emptyList()
)

