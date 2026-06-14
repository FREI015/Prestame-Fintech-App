package com.controlprestamos.core.documents

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfDocumentUtils {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 40

    fun createPdfFile(
        context: Context,
        fileName: String
    ): File {
        val safeName = fileName
            .replace(" ", "_")
            .replace("/", "-")
            .replace("\\", "-")

        val directory = File(context.cacheDir, "documents").apply {
            mkdirs()
        }

        return File(directory, safeName)
    }

    fun createPage(
        document: PdfDocument,
        pageNumber: Int = 1
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo
            .Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber)
            .create()

        return document.startPage(pageInfo)
    }

    fun titlePaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    fun subtitlePaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    fun bodyPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
        }
    }

    fun smallPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
        }
    }

    fun drawHeader(
        canvas: Canvas,
        title: String,
        subtitle: String
    ): Int {
        val titlePaint = titlePaint()
        val bodyPaint = bodyPaint()

        canvas.drawText(title, MARGIN.toFloat(), 48f, titlePaint)
        canvas.drawText(subtitle, MARGIN.toFloat(), 68f, bodyPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
        }

        canvas.drawLine(
            MARGIN.toFloat(),
            84f,
            (PAGE_WIDTH - MARGIN).toFloat(),
            84f,
            linePaint
        )

        return 110
    }

    fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        canvas.drawText("$label:", x, y, subtitlePaint())
        canvas.drawText(value, x + 150f, y, bodyPaint())
    }

    fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxChars: Int = 78,
        lineHeight: Float = 16f
    ): Float {
        var y = startY

        text
            .ifBlank { "-" }
            .chunked(maxChars)
            .forEach { line ->
                canvas.drawText(line, x, y, bodyPaint())
                y += lineHeight
            }

        return y
    }

    fun drawFooter(
        canvas: Canvas,
        pageNumber: Int
    ) {
        val footer = "Generado desde Control Prestamos - ${formatDate(System.currentTimeMillis())} - Pagina $pageNumber"
        canvas.drawText(footer, MARGIN.toFloat(), (PAGE_HEIGHT - 28).toFloat(), smallPaint())
    }

    fun formatMoney(
        value: Double,
        currencySymbol: String = "$"
    ): String {
        return currencySymbol + DecimalFormat("#,##0.00").format(value)
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(millis))
    }
}
