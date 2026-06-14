package com.controlprestamos.features.payments.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.controlprestamos.core.documents.PdfShareUtils
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.installments.domain.model.InstallmentStatus
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.payments.domain.collection.CollectionPdfGenerator
import com.controlprestamos.features.payments.domain.collection.CollectionPdfItem
import com.controlprestamos.features.preferences.data.LocalPreferencesRepository

@Composable
fun PaymentCollectionPdfButton() {
    val context = LocalContext.current
    val preferences = LocalPreferencesRepository.getPreferences(context)

    SecondaryButton(
        text = "PDF de cobranza",
        onClick = {
            val installments = LocalInstallmentRepository
                .getAllInstallments()
                .filter { installment ->
                    installment.status == InstallmentStatus.PENDING ||
                        installment.status == InstallmentStatus.PARTIAL ||
                        installment.status == InstallmentStatus.OVERDUE
                }

            val items = installments.map { installment ->
                val loan = LocalLoanRepository.getLoanById(installment.loanId)
                val client = loan?.let { currentLoan ->
                    LocalClientRepository.getClientById(currentLoan.clientId)
                }

                CollectionPdfItem(
                    client = client,
                    installment = installment,
                    statusLabel = installment.status.label
                )
            }

            val file = CollectionPdfGenerator.generate(
                context = context,
                title = preferences.businessName,
                items = items,
                currencySymbol = preferences.currencySymbol
            )

            PdfShareUtils.sharePdf(
                context = context,
                file = file,
                chooserTitle = "Compartir cobranza PDF"
            )
        }
    )
}
