
package com.controlprestamos.features.clients.presentation.form.wizard


import androidx.compose.runtime.*


@Composable
fun ProfessionalWizardContainer(){


var step by remember{

mutableStateOf(1)

}



when(step){


1 -> {

com.controlprestamos.features.clients.presentation.form.professional.IdentityProfessionalStep()

}



2 -> {

com.controlprestamos.features.clients.presentation.form.professional.ContactProfessionalStep()

}



}



}




