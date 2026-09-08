package com.controlprestamos.features.payments.domain.collection

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.controlprestamos.core.documents.PdfDocumentUtils
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.installments.domain.model.Installment
import java.io.File
import java.io.FileOutputStream

data class CollectionPdfItem(
    val client: Client?,
    val installment: Installment,
    val statusLabel: String
)

object CollectionPdfGenerator {

    fun generate(
        context: Context,
        title: String,
        items: List<CollectionPdfItem>,
        currencySymbol: String = "$"
    ): File {
        val file = PdfDocumentUtils.createPdfFile(
            context = context,
            fileName = "cobranza_control_prestamos_${System.currentTimeMillis()}.pdf"
        )

        val document = PdfDocument()
        val page = PdfDocumentUtils.createPage(document)
        val canvas = page.canvas

        var y = PdfDocumentUtils.drawHeader(
            canvas = canvas,
            title = title,
            subtitle = "Listado de cobranza"
        ).toFloat()

        if (items.isEmpty()) {
            canvas.drawText("No hay cuotas para mostrar.", 40f, y, PdfDocumentUtils.bodyPaint())
        } else {
            items.take(32).forEach { item ->
                if (y > 780f) return@forEach

                val clientName = item.client?.fullName ?: "Cliente no registrado"
                val amount = PdfDocumentUtils.formatMoney(item.installment.pendingAmount, currencySymbol)
                val dueDate = PdfDocumentUtils.formatDate(item.installment.dueDateMillis)

                canvas.drawText(clientName, 40f, y, PdfDocumentUtils.subtitlePaint())
                y += 16f

                canvas.drawText(
                    "Monto: $amount | Vence: $dueDate | Estado: ${item.statusLabel}",
                    40f,
                    y,
                    PdfDocumentUtils.bodyPaint()
                )
                y += 22f
            }
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

