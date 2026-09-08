
package com.controlprestamos.features.clients.presentation.form.professional


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable



@Composable
fun ContactProfessionalStep(){


Column{


Text(
"Contacto"
)


OutlinedTextField(

value="",

onValueChange={},

label={
Text("Telefono")
}

)



OutlinedTextField(

value="",

onValueChange={},

label={
Text("Correo")
}

)


}



}


