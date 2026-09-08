
package com.controlprestamos.features.clients.presentation.form.wizard


object WizardValidationCoordinator {



fun validateStep(

step:Int,

name:String,

document:String

):List<String>{


val errors =
mutableListOf<String>()



if(step==1){


if(name.isBlank()){

errors.add(
"Nombre obligatorio"
)

}


if(document.isBlank()){

errors.add(
"Documento obligatorio"
)

}


}



return errors


}



}


