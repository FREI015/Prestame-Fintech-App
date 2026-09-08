package com.controlprestamos.features.clients.presentation.form.messages


data class FormMessage(

val type:FormMessageType,

val text:String,

val timestamp:Long=
System.currentTimeMillis()

)

