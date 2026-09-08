package com.controlprestamos.features.clients.presentation.form.audit


data class ClientFormAuditEvent(

    val event:String,

    val step:String,

    val timestamp:Long =
        System.currentTimeMillis()

)

