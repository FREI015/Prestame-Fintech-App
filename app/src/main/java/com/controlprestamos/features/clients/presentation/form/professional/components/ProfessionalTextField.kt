
package com.controlprestamos.features.clients.presentation.form.professional.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable



@Composable
fun ProfessionalTextField(

label:String,

value:String,

onChange:(String)->Unit

){


OutlinedTextField(

value=value,

onValueChange=onChange,

label={
Text(label)
}

)


}


