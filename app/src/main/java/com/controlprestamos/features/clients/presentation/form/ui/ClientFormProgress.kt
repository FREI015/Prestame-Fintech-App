package com.controlprestamos.features.clients.presentation.form.ui


import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable


@Composable
fun ClientFormProgress(
    step:Int
){

    LinearProgressIndicator(
        progress = step / 8f
    )

}

