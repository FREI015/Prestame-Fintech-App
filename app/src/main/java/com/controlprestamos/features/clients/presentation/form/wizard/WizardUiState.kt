
package com.controlprestamos.features.clients.presentation.form.wizard


data class WizardUiState(

val currentStep:Int = 1,

val totalSteps:Int = 8,

val errors:List<String> = emptyList(),

val message:String? = null,

val completed:Boolean = false

)


