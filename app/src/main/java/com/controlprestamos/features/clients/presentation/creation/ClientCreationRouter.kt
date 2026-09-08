package com.controlprestamos.features.clients.presentation.creation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel


import com.controlprestamos.features.clients.presentation.form.ClientFormScreen
import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormViewModel



@Composable
fun ClientCreationRouter(

    onNavigateBack: () -> Unit,

    onClientCreated: (String) -> Unit

){


    val viewModel: ClientFormViewModel = viewModel()



    ClientFormScreen(

        viewModel = viewModel,

        onNavigateBack = onNavigateBack,

        onClientCreated = onClientCreated

    )


}
