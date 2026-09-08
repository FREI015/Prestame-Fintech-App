
package com.controlprestamos.features.clients.presentation.form.wizard


enum class WizardStep(

val number:Int,
val title:String

){


IDENTITY(
1,
"Identidad"
),


CONTACT(
2,
"Contacto"
),


ADDRESS(
3,
"Direccion"
),


EMPLOYMENT(
4,
"Empleo"
),


FINANCIAL(
5,
"Finanzas"
),


REFERENCES(
6,
"Referencias"
),


DOCUMENTS(
7,
"Documentos"
),


CONSENT(
8,
"Consentimientos"
)


}


