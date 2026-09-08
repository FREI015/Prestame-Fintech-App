
package com.controlprestamos.features.clients.presentation.form.validation



object ClientCoreValidation {



fun required(
value:String
):Boolean{


return value.isNotBlank()


}



fun minLength(
value:String,
size:Int
):Boolean{


return value.length >= size


}


}


