
package com.controlprestamos.features.clients.presentation.form.professional


object ProfessionalFormValidator {



fun validateIdentity(

state:ClientProfessionalFormState

):List<ClientFieldError>{



val errors =
mutableListOf<ClientFieldError>()



if(state.firstName.isBlank()){


errors.add(

ClientFieldError(
"firstName",
"Nombre obligatorio"
)

)

}



if(state.lastName.isBlank()){


errors.add(

ClientFieldError(
"lastName",
"Apellido obligatorio"
)

)

}



if(state.documentNumber.isBlank()){


errors.add(

ClientFieldError(
"documentNumber",
"Documento obligatorio"
)

)

}



return errors


}


}


