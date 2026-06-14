package com.controlprestamos.core.app

import android.content.Context
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.data.LocalPaymentRepository

object AppDependencies {

    fun initialize(context: Context) {
        val appContext = context.applicationContext

        LocalClientRepository.initialize(appContext)
        LocalLoanRepository.initialize(appContext)
        LocalPaymentRepository.initialize(appContext)
        LocalInstallmentRepository.initialize(appContext)
    }
}
