package com.controlprestamos.features.clients.presentation.form.audit


data class ValidationAuditEvent(

    val field:String,

    val error:String,

    val timestamp:Long =
        System.currentTimeMillis()

)


