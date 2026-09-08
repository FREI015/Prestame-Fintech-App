package com.controlprestamos.features.clients.presentation.form.identity


import androidx.compose.runtime.Composable


import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormEvent

import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormUiState



@Composable
fun IdentityStepContainer(


    state: ClientFormUiState,


    onEvent: (ClientFormEvent) -> Unit


){



IdentityStep(


    name = state.firstName,


    lastName = state.lastName,


    document = state.document,



    onNameChange = {


        onEvent(

            ClientFormEvent.NameChanged(it)

        )


    },



    onLastNameChange = {


        onEvent(

            ClientFormEvent.LastNameChanged(it)

        )


    },



    onDocumentChange = {


        onEvent(

            ClientFormEvent.DocumentChanged(it)

        )


    }



)



}
