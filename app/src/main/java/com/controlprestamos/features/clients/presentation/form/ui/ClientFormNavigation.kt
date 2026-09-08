package com.controlprestamos.features.clients.presentation.form.ui


import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun ClientFormNavigation(
    onNext:()->Unit
){

    Button(
        onClick = onNext
    ){

        Text(
            "Continuar"
        )

    }

}

