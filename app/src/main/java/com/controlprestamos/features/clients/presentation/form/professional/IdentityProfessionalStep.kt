
package com.controlprestamos.features.clients.presentation.form.professional


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable


@Composable
fun IdentityProfessionalStep(){


Column{


Text(
"Identidad del Cliente"
)



OutlinedTextField(

value="",

onValueChange={},

label={
Text("Nombre")
}

)



OutlinedTextField(

value="",

onValueChange={},

label={
Text("Apellido")
}

)



OutlinedTextField(

value="",

onValueChange={},

label={
Text("Documento")
}

)



}



}


