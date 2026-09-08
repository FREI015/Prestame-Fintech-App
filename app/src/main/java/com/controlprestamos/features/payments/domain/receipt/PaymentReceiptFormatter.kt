package com.controlprestamos.features.payments.domain.receipt

import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PaymentReceiptFormatter {

    fun buildPaymentReceipt(
        payment: Payment,
        loan: Loan?,
        client: Client?,
        businessName: String,
        currencySymbol: String
    ): String {
        val statusLabel = when (payment.status) {
            PaymentStatus.ACTIVE -> "ACTIVO"
            PaymentStatus.CANCELLED -> "ANULADO"
        }

        val cancellationBlock = if (payment.status == PaymentStatus.CANCELLED) {
            """

ESTADO DEL RECIBO: ANULADO
Motivo de anulación: ${payment.cancellationReason ?: "Sin motivo registrado"}
Anulado el: ${payment.cancelledAtMillis?.let { formatDate(it) } ?: "Sin fecha"}
""".trimEnd()
        } else {
            ""
        }

        return """
$businessName

RECIBO DE PAGO

Estado: $statusLabel
Cliente: ${client?.fullName ?: "Cliente no encontrado"}
Documento: ${client?.documentId?.ifBlank { "Sin documento" } ?: "Sin documento"}
Teléfono: ${client?.phone?.ifBlank { "Sin teléfono" } ?: "Sin teléfono"}

Préstamo: ${loan?.id?.take(8) ?: "No encontrado"}
Monto prestado: ${formatMoney(loan?.principalAmount ?: 0.0, currencySymbol)}
Total a pagar: ${formatMoney(loan?.totalExpectedAmount ?: 0.0, currencySymbol)}

Pago: ${formatMoney(payment.amount, currencySymbol)}
Método: ${payment.method.ifBlank { "No registrado" }}
Referencia: ${payment.reference.ifBlank { "Sin referencia" }}
Nota: ${payment.notes.ifBlank { "Sin nota" }}
Fecha: ${formatDate(payment.createdAtMillis)}
$cancellationBlock

Generado desde Control Préstamos.
""".trimIndent()
    }

    private fun formatMoney(
        value: Double,
        currencySymbol: String
    ): String {
        return currencySymbol + DecimalFormat("#,##0.00").format(value)
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(millis))
    }
}

