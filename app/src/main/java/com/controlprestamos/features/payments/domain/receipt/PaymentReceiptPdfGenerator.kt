package com.controlprestamos.features.payments.domain.receipt

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.controlprestamos.core.documents.PdfDocumentUtils
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.model.PaymentStatus
import java.io.File
import java.io.FileOutputStream

object PaymentReceiptPdfGenerator {

    fun generate(
        context: Context,
        client: Client?,
        loan: Loan?,
        payment: Payment,
        businessName: String = "Control Préstamos",
        currencySymbol: String = "$"
    ): File {
        val file = PdfDocumentUtils.createPdfFile(
            context = context,
            fileName = "recibo_pago_${payment.id}.pdf"
        )

        val document = PdfDocument()
        val page = PdfDocumentUtils.createPage(document)
        val canvas = page.canvas

        val isCancelled = payment.status == PaymentStatus.CANCELLED

        var y = PdfDocumentUtils.drawHeader(
            canvas = canvas,
            title = businessName,
            subtitle = if (isCancelled) "Recibo de pago anulado" else "Recibo de pago"
        ).toFloat()

        PdfDocumentUtils.drawLabelValue(canvas, "Estado", payment.status.label, 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Cliente", client?.fullName ?: "No registrado", 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Documento", client?.documentId ?: "-", 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Teléfono", client?.phone ?: "-", 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Préstamo", loan?.id ?: payment.loanId, 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(
            canvas,
            "Monto pagado",
            PdfDocumentUtils.formatMoney(payment.amount, currencySymbol),
            40f,
            y
        )
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Método", payment.method.ifBlank { "-" }, 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(canvas, "Referencia", payment.reference.ifBlank { "-" }, 40f, y)
        y += 24f

        PdfDocumentUtils.drawLabelValue(
            canvas,
            "Fecha del pago",
            PdfDocumentUtils.formatDate(payment.createdAtMillis),
            40f,
            y
        )
        y += 24f

        if (isCancelled) {
            PdfDocumentUtils.drawLabelValue(
                canvas,
                "Fecha anulación",
                payment.cancelledAtMillis?.let { PdfDocumentUtils.formatDate(it) } ?: "-",
                40f,
                y
            )
            y += 24f

            PdfDocumentUtils.drawLabelValue(
                canvas,
                "Motivo",
                payment.cancellationReason ?: "Sin motivo registrado",
                40f,
                y
            )
            y += 28f
        }

        canvas.drawText("Nota:", 40f, y, PdfDocumentUtils.subtitlePaint())
        y += 18f

        y = PdfDocumentUtils.drawMultilineText(
            canvas = canvas,
            text = payment.notes.ifBlank { "Sin nota" },
            x = 40f,
            startY = y
        )

        y += 40f

        if (!isCancelled) {
            canvas.drawText("Firma / recibido por:", 40f, y, PdfDocumentUtils.subtitlePaint())
            y += 34f
            canvas.drawLine(40f, y, 280f, y, PdfDocumentUtils.bodyPaint())
        } else {
            canvas.drawText(
                "Este pago queda en historial, pero no suma financieramente.",
                40f,
                y,
                PdfDocumentUtils.bodyPaint()
            )
        }

        PdfDocumentUtils.drawFooter(canvas, 1)

        document.finishPage(page)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }

        document.close()

        return file
    }
}
