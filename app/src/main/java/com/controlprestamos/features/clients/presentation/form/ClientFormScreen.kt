package com.controlprestamos.features.clients.presentation.form


import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import androidx.compose.runtime.getValue


import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormViewModel

import com.controlprestamos.features.clients.presentation.form.wizard.ClientWizardScreen



@Composable
fun ClientFormScreen(

    viewModel: ClientFormViewModel,

    onNavigateBack: () -> Unit,

    onClientCreated: (String) -> Unit

){



    val state by viewModel.state.collectAsStateWithLifecycle()



    ClientWizardScreen(

        state = state,

        onEvent = viewModel::onEvent,

        onNavigateBack = onNavigateBack,

        onClientCreated = onClientCreated

    )


}
