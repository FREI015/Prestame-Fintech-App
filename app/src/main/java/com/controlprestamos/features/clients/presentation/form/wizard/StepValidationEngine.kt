
package com.controlprestamos.features.clients.presentation.form.wizard


object StepValidationEngine{


fun validateIdentity(

name:String,

document:String

):List<String>{


val errors =
mutableListOf<String>()


if(name.isBlank()){

errors.add(
"Nombre requerido"
)

}


if(document.isBlank()){

errors.add(
"Documento requerido"
)

}


return errors

}


}


