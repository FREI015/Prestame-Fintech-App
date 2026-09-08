package com.controlprestamos.features.clients.presentation.form.messages


import androidx.compose.material3.*
import androidx.compose.runtime.Composable


@Composable
fun FormSnackbar(message:String?){

message?.let{

Snackbar{
Text(it)
}

}

}


