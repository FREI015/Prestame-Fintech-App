package com.controlprestamos.core.rules

import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus

object FinancialOperationRules {

    fun canCreateLoanForClient(client: Client?): Boolean {
        return client?.status == ClientStatus.ACTIVE
    }

    fun canRegisterPaymentForLoan(
        client: Client?,
        loan: Loan?
    ): Boolean {
        if (client?.status != ClientStatus.ACTIVE) return false
        if (loan?.status != LoanStatus.ACTIVE) return false

        return true
    }

    fun canShowClientInOperationalLists(client: Client?): Boolean {
        return client?.status == ClientStatus.ACTIVE
    }

    fun canShowClientInHistory(client: Client?): Boolean {
        return client != null
    }

    fun canGenerateClientCollectionPdf(client: Client?): Boolean {
        return client != null
    }

    fun canArchiveClient(client: Client?): Boolean {
        return client?.status == ClientStatus.ACTIVE
    }

    fun canReactivateClient(client: Client?): Boolean {
        return client?.status == ClientStatus.INACTIVE
    }

    fun canEditLoanFinancialFields(
        loan: Loan?,
        hasPaymentHistory: Boolean
    ): Boolean {
        if (loan?.status != LoanStatus.ACTIVE) return false
        if (hasPaymentHistory) return false

        return true
    }

    fun canEditLoanDescription(loan: Loan?): Boolean {
        if (loan == null) return false
        if (loan.status == LoanStatus.CANCELLED) return false

        return true
    }

    fun canCancelPayment(payment: Payment?): Boolean {
        return payment?.status == PaymentStatus.ACTIVE
    }

    fun canSharePaymentReceipt(payment: Payment?): Boolean {
        return payment != null
    }

    fun shouldCountLoanInOperationalReports(
        client: Client?,
        loan: Loan?
    ): Boolean {
        if (client?.status != ClientStatus.ACTIVE) return false
        if (loan?.status == LoanStatus.CANCELLED) return false

        return loan != null
    }

    fun shouldCountPaymentFinancially(payment: Payment?): Boolean {
        return payment?.status == PaymentStatus.ACTIVE
    }

    fun isClosedLoan(loan: Loan?): Boolean {
        return loan?.status == LoanStatus.PAID ||
            loan?.status == LoanStatus.CANCELLED
    }

    fun getClientStatusLabel(client: Client?): String {
        return when (client?.status) {
            ClientStatus.ACTIVE -> "Activo"
            ClientStatus.INACTIVE -> "Archivado"
            null -> "No encontrado"
        }
    }

    fun getLoanStatusLabel(loan: Loan?): String {
        return when (loan?.status) {
            LoanStatus.ACTIVE -> "Activo"
            LoanStatus.PAID -> "Pagado"
            LoanStatus.CANCELLED -> "Cancelado"
            null -> "No encontrado"
        }
    }

    fun getPaymentStatusLabel(payment: Payment?): String {
        return when (payment?.status) {
            PaymentStatus.ACTIVE -> "Activo"
            PaymentStatus.CANCELLED -> "Anulado"
            null -> "No encontrado"
        }
    }

    fun getClientOperationMessage(client: Client?): String {
        return when (client?.status) {
            ClientStatus.ACTIVE -> "Cliente activo: puede crear préstamos y registrar pagos operativos."
            ClientStatus.INACTIVE -> "Cliente archivado: su historial se conserva, pero no participa en la operación principal. Reactívalo para crear préstamos o registrar pagos."
            null -> "Cliente no encontrado."
        }
    }

    fun getLoanOperationMessage(loan: Loan?): String {
        return when (loan?.status) {
            LoanStatus.ACTIVE -> "Préstamo activo: admite pagos y seguimiento de cuotas."
            LoanStatus.PAID -> "Préstamo pagado: ya fue saldado por completo y no admite nuevos pagos."
            LoanStatus.CANCELLED -> "Préstamo cancelado: conserva historial, pero no admite nuevos pagos ni cuotas operativas."
            null -> "Préstamo no encontrado."
        }
    }

    fun getPaymentOperationMessage(payment: Payment?): String {
        return when (payment?.status) {
            PaymentStatus.ACTIVE -> "Pago activo: suma al total cobrado y reduce el saldo pendiente."
            PaymentStatus.CANCELLED -> "Pago anulado: queda en historial, pero no suma financieramente ni reduce saldo."
            null -> "Pago no encontrado."
        }
    }
}

