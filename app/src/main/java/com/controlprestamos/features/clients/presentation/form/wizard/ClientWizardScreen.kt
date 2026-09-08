package com.controlprestamos.features.clients.presentation.form.wizard


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


import androidx.compose.material3.Text


import androidx.compose.runtime.Composable


import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


import com.controlprestamos.features.clients.presentation.form.identity.IdentityStepContainer

import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormEvent

import com.controlprestamos.features.clients.presentation.form.viewmodel.ClientFormUiState



@Composable
fun ClientWizardScreen(

    state: ClientFormUiState,

    onEvent: (ClientFormEvent) -> Unit,

    onNavigateBack: () -> Unit,

    onClientCreated: (String) -> Unit

){


Column(

    modifier =
    Modifier
        .fillMaxSize()
        .verticalScroll(
            rememberScrollState()
        )
        .padding(16.dp)

){



Text(

    text = "Nuevo Cliente"

)



Spacer(

    modifier =
    Modifier.height(8.dp)

)



StepIndicator(

    step = state.currentStep

)



Spacer(

    modifier =
    Modifier.height(16.dp)

)



when(state.currentStep){


    1 -> {

        IdentityStepContainer(

            state = state,

            onEvent = onEvent

        )

    }


    2 -> {

        Text(
            text = "Contacto"
        )

    }


    3 -> {

        Text(
            text = "Dirección"
        )

    }


    4 -> {

        Text(
            text = "Empleo"
        )

    }


    5 -> {

        Text(
            text = "Finanzas"
        )

    }


    6 -> {

        Text(
            text = "Referencias"
        )

    }


    7 -> {

        Text(
            text = "Documentos"
        )

    }


    8 -> {

        Text(
            text = "Consentimiento"
        )

    }


}



Spacer(

    modifier =
    Modifier.height(24.dp)

)



WizardNavigation(

    onBack = {


        if(state.currentStep == 1){

            onNavigateBack()

        }
        else{

            onEvent(
                ClientFormEvent.PreviousStep
            )

        }


    },


    onNext = {


        if(state.currentStep < 8){


            onEvent(
                ClientFormEvent.NextStep
            )


        }
        else{


            onClientCreated(
                "NEW_CLIENT"
            )


        }


    },


    isFirstStep =
    state.currentStep == 1,


    isLastStep =
    state.currentStep == 8

)


}


}
