
package com.controlprestamos.features.clients.presentation.form.viewmodel

data class ClientFormUiState(

    // ==================================================
    // IDENTIDAD
    // ==================================================

    val firstName: String = "",
    val lastName: String = "",
    val document: String = "",
    val documentType: String = "",
    val birthDate: String = "",

    // ==================================================
    // CONTACTO
    // ==================================================

    val phone: String = "",
    val email: String = "",
    val whatsapp: String = "",

    // ==================================================
    // DIRECCION
    // ==================================================

    val address: String = "",
    val city: String = "",
    val stateProvince: String = "",

    // ==================================================
    // EMPLEO / PERFIL
    // ==================================================

    val occupation: String = "",
    val company: String = "",
    val income: String = "",

    // ==================================================
    // WIZARD
    // ==================================================

    val currentStep: Int = 1,
    val totalSteps: Int = 8,

    // ==================================================
    // VALIDACION
    // ==================================================

    val errors: List<String> = emptyList(),
    val message: String? = null,
    val validating: Boolean = false,
    val canContinue: Boolean = false,

    // ==================================================
    // PROCESO
    // ==================================================

    val saving: Boolean = false,
    val completed: Boolean = false
)
