package com.controlprestamos.features.reports.domain

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.controlprestamos.core.documents.PdfDocumentUtils
import java.io.File
import java.io.FileOutputStream

data class ReportPdfLine(
    val label: String,
    val value: String
)

object ReportPdfGenerator {

    fun generate(
        context: Context,
        title: String,
        subtitle: String,
        lines: List<ReportPdfLine>
    ): File {
        val file = PdfDocumentUtils.createPdfFile(
            context = context,
            fileName = "reporte_control_prestamos_${System.currentTimeMillis()}.pdf"
        )

        val document = PdfDocument()
        val page = PdfDocumentUtils.createPage(document)
        val canvas = page.canvas

        var y = PdfDocumentUtils.drawHeader(
            canvas = canvas,
            title = title,
            subtitle = subtitle
        ).toFloat()

        lines.forEach { line ->
            if (y > 780f) return@forEach

            PdfDocumentUtils.drawLabelValue(
                canvas = canvas,
                label = line.label,
                value = line.value,
                x = 40f,
                y = y
            )

            y += 22f
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

