package com.controlprestamos.features.clients.presentation.form.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable


@Composable
fun ClientTextField(
    label:String,
    value:String,
    error:String?,
    onChange:(String)->Unit
){

OutlinedTextField(
    value=value,
    onValueChange=onChange,
    label={Text(label)},
    isError=error!=null
)

}


