package com.controlprestamos.features.clients.domain.collection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.controlprestamos.core.documents.PdfDocumentUtils
import com.controlprestamos.core.rules.FinancialOperationRules
import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.Installment
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.LoanStatus
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ClientCollectionPdfGenerator {

    private const val CONTENT_LEFT = 40f
    private const val CONTENT_RIGHT = 555f
    private const val PAGE_BOTTOM_LIMIT = 760f

    fun generate(
        context: Context,
        client: Client,
        loans: List<Loan>,
        businessName: String = "Control Préstamos",
        currencySymbol: String = "$"
    ): File {
        val cleanBusinessName = businessName.ifBlank { "Control Préstamos" }
        val safeClientName = client.fullName
            .ifBlank { "cliente" }
            .replace(" ", "_")
            .replace("/", "-")
            .replace("\\", "-")

        val file = PdfDocumentUtils.createPdfFile(
            context = context,
            fileName = "cobranza_${safeClientName}_${System.currentTimeMillis()}.pdf"
        )

        val document = PdfDocument()
        var pageNumber = 1
        var page = PdfDocumentUtils.createPage(document, pageNumber)
        var canvas = page.canvas
        var y = drawProfessionalHeader(
            canvas = canvas,
            businessName = cleanBusinessName,
            subtitle = "Estado de cobranza del cliente",
            pageNumber = pageNumber
        )

        fun newPage(subtitle: String) {
            PdfDocumentUtils.drawFooter(canvas, pageNumber)
            document.finishPage(page)

            pageNumber += 1
            page = PdfDocumentUtils.createPage(document, pageNumber)
            canvas = page.canvas

            y = drawProfessionalHeader(
                canvas = canvas,
                businessName = cleanBusinessName,
                subtitle = subtitle,
                pageNumber = pageNumber
            )
        }

        fun ensureSpace(requiredHeight: Float, subtitle: String) {
            if (y + requiredHeight > PAGE_BOTTOM_LIMIT) {
                newPage(subtitle)
            }
        }

        val collectibleLoans = loans
            .filter { loan -> loan.status != LoanStatus.CANCELLED }
            .sortedWith(
                compareBy<Loan> { loan ->
                    when (loan.status) {
                        LoanStatus.ACTIVE -> 0
                        LoanStatus.PAID -> 1
                        LoanStatus.CANCELLED -> 2
                    }
                }.thenByDescending { loan -> loan.startDateMillis }
            )

        val activeLoans = collectibleLoans.filter { loan -> loan.status == LoanStatus.ACTIVE }

        val allInstallments = collectibleLoans.flatMap { loan ->
            LocalInstallmentRepository.getInstallmentsByLoan(loan.id)
        }

        val collectibleInstallments = allInstallments
            .filter { installment ->
                installment.status != InstallmentStatus.CANCELLED &&
                    installment.status != InstallmentStatus.PAID
            }
            .sortedWith(
                compareBy<Installment> { installment -> installment.dueDateMillis }
                    .thenBy { installment -> installment.number }
            )

        val overdueInstallments = collectibleInstallments
            .filter { installment -> installment.status == InstallmentStatus.OVERDUE }

        val pendingInstallments = collectibleInstallments
            .filter { installment -> installment.status != InstallmentStatus.OVERDUE }

        val totalExpected = activeLoans.sumOf { loan -> loan.totalExpectedAmount }
        val totalPaid = activeLoans.sumOf { loan ->
            LocalPaymentRepository.getTotalPaidByLoan(loan.id)
        }
        val totalPending = max(totalExpected - totalPaid, 0.0)
        val totalOverdue = overdueInstallments.sumOf { installment ->
            max(installment.pendingAmount, 0.0)
        }

        y = drawClientBlock(
            canvas = canvas,
            client = client,
            y = y
        )

        y += 10f

        y = drawSectionTitle(
            canvas = canvas,
            title = "Resumen de cartera",
            y = y
        )

        y = drawSummaryGrid(
            canvas = canvas,
            y = y,
            currencySymbol = currencySymbol,
            totalExpected = totalExpected,
            totalPaid = totalPaid,
            totalPending = totalPending,
            totalOverdue = totalOverdue,
            activeLoanCount = activeLoans.size,
            overdueCount = overdueInstallments.size
        )

        y += 12f

        y = drawSectionTitle(
            canvas = canvas,
            title = "Detalle de préstamos",
            y = y
        )

        if (collectibleLoans.isEmpty()) {
            canvas.drawText(
                "Este cliente no tiene préstamos activos o históricos cobrables.",
                CONTENT_LEFT,
                y,
                bodyPaint()
            )
            y += 24f
        } else {
            collectibleLoans.forEach { loan ->
                ensureSpace(80f, "Detalle de préstamos")

                val paid = LocalPaymentRepository.getTotalPaidByLoan(loan.id)
                val pending = max(loan.totalExpectedAmount - paid, 0.0)
                val loanInstallments = LocalInstallmentRepository
                    .getInstallmentsByLoan(loan.id)
                    .filter { installment ->
                        installment.status != InstallmentStatus.CANCELLED &&
                            installment.status != InstallmentStatus.PAID
                    }

                y = drawLoanCard(
                    canvas = canvas,
                    loan = loan,
                    paid = paid,
                    pending = pending,
                    pendingInstallments = loanInstallments.size,
                    currencySymbol = currencySymbol,
                    y = y
                )

                y += 10f
            }
        }

        ensureSpace(80f, "Cuotas vencidas y pendientes")
        y = drawSectionTitle(
            canvas = canvas,
            title = "Cuotas vencidas",
            y = y
        )

        if (overdueInstallments.isEmpty()) {
            canvas.drawText(
                "No hay cuotas vencidas registradas para este cliente.",
                CONTENT_LEFT,
                y,
                bodyPaint()
            )
            y += 24f
        } else {
            overdueInstallments.take(12).forEach { installment ->
                ensureSpace(44f, "Cuotas vencidas")
                y = drawInstallmentLine(
                    canvas = canvas,
                    installment = installment,
                    currencySymbol = currencySymbol,
                    y = y
                )
            }

            if (overdueInstallments.size > 12) {
                canvas.drawText(
                    "Hay ${overdueInstallments.size - 12} cuotas vencidas adicionales no mostradas en este resumen.",
                    CONTENT_LEFT,
                    y,
                    smallPaint()
                )
                y += 18f
            }
        }

        ensureSpace(80f, "Cuotas pendientes")
        y = drawSectionTitle(
            canvas = canvas,
            title = "Próximas cuotas pendientes",
            y = y
        )

        if (pendingInstallments.isEmpty()) {
            canvas.drawText(
                "No hay próximas cuotas pendientes registradas.",
                CONTENT_LEFT,
                y,
                bodyPaint()
            )
            y += 24f
        } else {
            pendingInstallments.take(10).forEach { installment ->
                ensureSpace(44f, "Cuotas pendientes")
                y = drawInstallmentLine(
                    canvas = canvas,
                    installment = installment,
                    currencySymbol = currencySymbol,
                    y = y
                )
            }

            if (pendingInstallments.size > 10) {
                canvas.drawText(
                    "Hay ${pendingInstallments.size - 10} cuotas pendientes adicionales no mostradas en este resumen.",
                    CONTENT_LEFT,
                    y,
                    smallPaint()
                )
                y += 18f
            }
        }

        ensureSpace(150f, "Mensaje de cobranza")
        y = drawSectionTitle(
            canvas = canvas,
            title = "Mensaje sugerido para WhatsApp",
            y = y
        )

        val whatsappText = buildWhatsappText(
            client = client,
            totalPending = totalPending,
            totalOverdue = totalOverdue,
            overdueCount = overdueInstallments.size,
            currencySymbol = currencySymbol,
            businessName = cleanBusinessName
        )

        y = PdfDocumentUtils.drawMultilineText(
            canvas = canvas,
            text = whatsappText,
            x = CONTENT_LEFT,
            startY = y,
            maxChars = 74,
            lineHeight = 15f
        )

        y += 18f

        canvas.drawText(
            "Gestiona. Controla. Haz crecer tu negocio.",
            CONTENT_LEFT,
            y,
            footerBrandPaint()
        )

        PdfDocumentUtils.drawFooter(canvas, pageNumber)
        document.finishPage(page)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }

        document.close()

        return file
    }

    private fun drawProfessionalHeader(
        canvas: Canvas,
        businessName: String,
        subtitle: String,
        pageNumber: Int
    ): Float {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(13, 27, 54)
            style = Paint.Style.FILL
        }

        canvas.drawRect(0f, 0f, PdfDocumentUtils.PAGE_WIDTH.toFloat(), 92f, headerPaint)

        canvas.drawText(
            businessName,
            CONTENT_LEFT,
            42f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )

        canvas.drawText(
            subtitle,
            CONTENT_LEFT,
            66f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(220, 245, 245)
                textSize = 11f
            }
        )

        canvas.drawText(
            "Página $pageNumber",
            505f,
            66f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(220, 245, 245)
                textSize = 9f
                textAlign = Paint.Align.RIGHT
            }
        )

        return 120f
    }

    private fun drawClientBlock(
        canvas: Canvas,
        client: Client,
        y: Float
    ): Float {
        var currentY = y

        currentY = drawSectionTitle(
            canvas = canvas,
            title = "Datos del cliente",
            y = currentY
        )

        drawLabelValue(canvas, "Cliente", client.fullName.ifBlank { "-" }, currentY)
        currentY += 18f

        drawLabelValue(canvas, "Documento", client.documentId.ifBlank { "-" }, currentY)
        currentY += 18f

        drawLabelValue(canvas, "Teléfono", client.phone.ifBlank { "-" }, currentY)
        currentY += 18f

        drawLabelValue(canvas, "Dirección", client.address.ifBlank { "-" }, currentY)
        currentY += 18f

        drawLabelValue(canvas, "Estado", FinancialOperationRules.getClientStatusLabel(client), currentY)
        currentY += 24f

        return currentY
    }

    private fun drawSectionTitle(
        canvas: Canvas,
        title: String,
        y: Float
    ): Float {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 139, 138)
            strokeWidth = 2f
        }

        canvas.drawText(title, CONTENT_LEFT, y, sectionTitlePaint())
        canvas.drawLine(CONTENT_LEFT, y + 8f, CONTENT_RIGHT, y + 8f, linePaint)

        return y + 26f
    }

    private fun drawSummaryGrid(
        canvas: Canvas,
        y: Float,
        currencySymbol: String,
        totalExpected: Double,
        totalPaid: Double,
        totalPending: Double,
        totalOverdue: Double,
        activeLoanCount: Int,
        overdueCount: Int
    ): Float {
        var currentY = y

        drawSummaryRow(
            canvas = canvas,
            y = currentY,
            leftLabel = "Total a cobrar",
            leftValue = PdfDocumentUtils.formatMoney(totalExpected, currencySymbol),
            rightLabel = "Total pagado",
            rightValue = PdfDocumentUtils.formatMoney(totalPaid, currencySymbol)
        )
        currentY += 42f

        drawSummaryRow(
            canvas = canvas,
            y = currentY,
            leftLabel = "Saldo pendiente",
            leftValue = PdfDocumentUtils.formatMoney(totalPending, currencySymbol),
            rightLabel = "Saldo vencido",
            rightValue = PdfDocumentUtils.formatMoney(totalOverdue, currencySymbol)
        )
        currentY += 42f

        drawSummaryRow(
            canvas = canvas,
            y = currentY,
            leftLabel = "Préstamos activos",
            leftValue = activeLoanCount.toString(),
            rightLabel = "Cuotas vencidas",
            rightValue = overdueCount.toString()
        )
        currentY += 44f

        return currentY
    }

    private fun drawSummaryRow(
        canvas: Canvas,
        y: Float,
        leftLabel: String,
        leftValue: String,
        rightLabel: String,
        rightValue: String
    ) {
        drawSummaryBox(
            canvas = canvas,
            x = CONTENT_LEFT,
            y = y,
            width = 240f,
            label = leftLabel,
            value = leftValue
        )

        drawSummaryBox(
            canvas = canvas,
            x = 315f,
            y = y,
            width = 240f,
            label = rightLabel,
            value = rightValue
        )
    }

    private fun drawSummaryBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        label: String,
        value: String
    ) {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(209, 213, 219)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        canvas.drawRoundRect(x, y - 18f, x + width, y + 18f, 8f, 8f, backgroundPaint)
        canvas.drawRoundRect(x, y - 18f, x + width, y + 18f, 8f, 8f, borderPaint)

        canvas.drawText(label, x + 10f, y - 2f, smallMutedPaint())
        canvas.drawText(value, x + 10f, y + 13f, summaryValuePaint())
    }

    private fun drawLoanCard(
        canvas: Canvas,
        loan: Loan,
        paid: Double,
        pending: Double,
        pendingInstallments: Int,
        currencySymbol: String,
        y: Float
    ): Float {
        var currentY = y

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(209, 213, 219)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        canvas.drawRoundRect(
            CONTENT_LEFT,
            currentY - 16f,
            CONTENT_RIGHT,
            currentY + 58f,
            8f,
            8f,
            backgroundPaint
        )

        canvas.drawRoundRect(
            CONTENT_LEFT,
            currentY - 16f,
            CONTENT_RIGHT,
            currentY + 58f,
            8f,
            8f,
            borderPaint
        )

        canvas.drawText(
            "Préstamo ${loan.id.take(8)} · ${FinancialOperationRules.getLoanStatusLabel(loan)}",
            CONTENT_LEFT + 10f,
            currentY,
            subtitlePaint()
        )
        currentY += 17f

        canvas.drawText(
            "Total: ${PdfDocumentUtils.formatMoney(loan.totalExpectedAmount, currencySymbol)}   Pagado: ${PdfDocumentUtils.formatMoney(paid, currencySymbol)}   Saldo: ${PdfDocumentUtils.formatMoney(pending, currencySymbol)}",
            CONTENT_LEFT + 10f,
            currentY,
            bodyPaint()
        )
        currentY += 17f

        canvas.drawText(
            "Inicio: ${PdfDocumentUtils.formatDate(loan.startDateMillis)}   Tasa: ${loan.interestRatePercent}%   Cuotas pendientes: $pendingInstallments",
            CONTENT_LEFT + 10f,
            currentY,
            smallMutedPaint()
        )
        currentY += 34f

        return currentY
    }

    private fun drawInstallmentLine(
        canvas: Canvas,
        installment: Installment,
        currencySymbol: String,
        y: Float
    ): Float {
        var currentY = y

        val statusLabel = installment.status.label
        val pendingAmount = PdfDocumentUtils.formatMoney(
            max(installment.pendingAmount, 0.0),
            currencySymbol
        )

        canvas.drawText(
            "Cuota ${installment.number} · $statusLabel · ${installment.loanId.take(8)}",
            CONTENT_LEFT,
            currentY,
            subtitlePaint()
        )
        currentY += 15f

        canvas.drawText(
            "Pendiente: $pendingAmount · Vence: ${PdfDocumentUtils.formatDate(installment.dueDateMillis)}",
            CONTENT_LEFT,
            currentY,
            bodyPaint()
        )
        currentY += 24f

        return currentY
    }

    private fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        y: Float
    ) {
        canvas.drawText("$label:", CONTENT_LEFT, y, labelPaint())
        canvas.drawText(value, CONTENT_LEFT + 120f, y, bodyPaint())
    }

    private fun buildWhatsappText(
        client: Client,
        totalPending: Double,
        totalOverdue: Double,
        overdueCount: Int,
        currencySymbol: String,
        businessName: String
    ): String {
        val clientName = client.fullName.ifBlank { "cliente" }
        val pendingText = PdfDocumentUtils.formatMoney(totalPending, currencySymbol)
        val overdueText = PdfDocumentUtils.formatMoney(totalOverdue, currencySymbol)

        return if (totalPending > 0.0) {
            "Hola $clientName, le compartimos su estado de cobranza de $businessName. Saldo pendiente: $pendingText. Saldo vencido: $overdueText. Cuotas vencidas: $overdueCount. Por favor revise el detalle del documento y comuníquese para coordinar su pago."
        } else {
            "Hola $clientName, le compartimos su estado de cobranza de $businessName. Actualmente no presenta saldo pendiente en los préstamos activos registrados. Gracias por mantenerse al día."
        }
    }

    private fun sectionTitlePaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(13, 27, 54)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun subtitlePaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(17, 24, 39)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun summaryValuePaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(13, 27, 54)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun labelPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(55, 65, 81)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun bodyPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(55, 65, 81)
            textSize = 10.5f
        }
    }

    private fun smallPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(75, 85, 99)
            textSize = 9f
        }
    }

    private fun smallMutedPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(107, 114, 128)
            textSize = 9f
        }
    }

    private fun footerBrandPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 139, 138)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
}

