package com.controlprestamos.features.clients.presentation.form.identity


data class IdentityUiState(

val name:String="",

val lastname:String="",

val document:String="",


val nameError:String?=null,

val lastnameError:String?=null,

val documentError:String?=null

)

