package com.controlprestamos.features.payments.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.controlprestamos.core.documents.PdfShareUtils
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.domain.model.Payment
import com.controlprestamos.features.payments.domain.receipt.PaymentReceiptPdfGenerator
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository

@Composable
fun PaymentPdfShareButton(
    payment: Payment
) {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    SecondaryButton(
        text = "Compartir PDF",
        onClick = {
            val loan = LocalLoanRepository.getLoanById(payment.loanId)

            val client = loan?.let { currentLoan ->
                LocalClientRepository.getClientById(currentLoan.clientId)
            }

            val file = PaymentReceiptPdfGenerator.generate(
                context = context,
                client = client,
                loan = loan,
                payment = payment,
                businessName = preferences.businessName,
                currencySymbol = preferences.currencySymbol
            )

            PdfShareUtils.sharePdf(
                context = context,
                file = file,
                chooserTitle = "Compartir recibo PDF"
            )
        }
    )
}
