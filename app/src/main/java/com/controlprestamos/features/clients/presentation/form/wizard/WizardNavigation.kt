package com.controlprestamos.features.clients.presentation.form.wizard


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth


import androidx.compose.material3.Button
import androidx.compose.material3.Text


import androidx.compose.runtime.Composable


import androidx.compose.ui.Modifier



@Composable
fun WizardNavigation(


    onBack: () -> Unit,


    onNext: () -> Unit,


    isFirstStep: Boolean = false,


    isLastStep: Boolean = false


){



Row(

    modifier =
    Modifier.fillMaxWidth(),


    horizontalArrangement =
    Arrangement.SpaceBetween

){



if(isFirstStep){


Spacer(
    modifier =
    Modifier.weight(1f)
)


}
else{


Button(

    onClick = onBack

){

Text("ATRÁS")

}


}



Button(

    onClick = onNext

){

Text(

    if(isLastStep)

        "FINALIZAR"

    else

        "CONTINUAR"

)

}



}



}
