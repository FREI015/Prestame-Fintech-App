
package com.controlprestamos.features.clients.presentation.form.professional


data class ClientProfessionalFormState(


// IDENTIDAD

val firstName:String = "",

val middleName:String = "",

val lastName:String = "",

val documentType:String = "",

val documentNumber:String = "",

val birthDate:String = "",



// CONTACTO

val phone:String = "",

val secondaryPhone:String = "",

val email:String = "",



// DIRECCION

val country:String = "",

val state:String = "",

val city:String = "",

val address:String = "",



// EMPLEO

val occupation:String = "",

val company:String = "",

val position:String = "",



// FINANZAS

val monthlyIncome:String = "",

val monthlyExpenses:String = "",



// REFERENCIAS

val referenceName:String = "",

val referencePhone:String = "",



// CONSENTIMIENTO

val acceptedTerms:Boolean = false


)


