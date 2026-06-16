package com.controlprestamos.debug

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.controlprestamos.core.app.AppDependencies
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.CreateClientInput
import com.controlprestamos.features.installments.data.LocalInstallmentRepository
import com.controlprestamos.features.loans.data.LocalLoanRepository
import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.RepaymentPlanType
import com.controlprestamos.features.payments.data.LocalPaymentRepository
import com.controlprestamos.features.payments.domain.model.CreatePaymentInput
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeedDemo90DataTest {

    @Test
    fun seedThreeMonthsDemoData() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        AppDependencies.initialize(context)

        val existingDemoClients = LocalClientRepository
            .getClients()
            .filter { client ->
                client.documentId.startsWith(DEMO_PREFIX)
            }

        if (existingDemoClients.isNotEmpty()) {
            println("DEMO90 ya existe. No se duplica data.")
            println("Clientes demo existentes: ${existingDemoClients.size}")
            return
        }

        val baseDate = startOfDay(daysAgo = 89)

        val clients = demoClients().mapIndexed { index, item ->
            LocalClientRepository.createClient(
                CreateClientInput(
                    firstName = item.firstName,
                    lastName = item.lastName,
                    documentId = "$DEMO_PREFIX-${1000 + index}",
                    phone = "0414${String.format(Locale.US, "%07d", 9000000 + index)}",
                    address = item.address,
                    notes = "Cliente demo generado para pruebas de 90 días. Puede archivarse o eliminarse según reglas normales de la app."
                )
            )
        }

        val loans = mutableListOf<String>()

        clients.forEachIndexed { index, client ->
            val firstLoanStart = baseDate + days((index * 5).toLong())
            val secondLoanStart = baseDate + days((index * 5 + 28).toLong())

            val firstLoan = LocalLoanRepository.createLoan(
                CreateLoanInput(
                    clientId = client.id,
                    principalAmount = listOf("120", "180", "250", "320", "400")[index % 5],
                    interestRatePercent = listOf("15", "18", "20", "22")[index % 4],
                    termInDays = listOf("30", "45", "60")[index % 3],
                    description = "DEMO90 - Préstamo principal ${index + 1}",
                    repaymentPlanType = RepaymentPlanType.INSTALLMENTS,
                    startDateMillis = firstLoanStart
                )
            )

            LocalInstallmentRepository.generateInstallmentsForLoan(firstLoan)
            loans += firstLoan.id

            if (index % 2 == 0) {
                val secondLoan = LocalLoanRepository.createLoan(
                    CreateLoanInput(
                        clientId = client.id,
                        principalAmount = listOf("90", "150", "210")[index % 3],
                        interestRatePercent = listOf("12", "16", "18")[index % 3],
                        termInDays = listOf("20", "30", "40")[index % 3],
                        description = "DEMO90 - Segundo préstamo ${index + 1}",
                        repaymentPlanType = RepaymentPlanType.INSTALLMENTS,
                        startDateMillis = secondLoanStart
                    )
                )

                LocalInstallmentRepository.generateInstallmentsForLoan(secondLoan)
                loans += secondLoan.id
            }
        }

        loans.forEachIndexed { index, loanId ->
            val loan = LocalLoanRepository.getLoanById(loanId) ?: return@forEachIndexed
            val paymentCount = when (index % 4) {
                0 -> 4
                1 -> 7
                2 -> 10
                else -> 13
            }

            val installmentAmount = max(
                loan.totalExpectedAmount / max(loan.termInDays, 1),
                1.0
            )

            repeat(paymentCount) { paymentIndex ->
                val paymentDate = loan.startDateMillis + days((paymentIndex * 5L) + (index % 3))
                val amount = when {
                    paymentIndex == paymentCount - 1 && index % 5 == 0 -> installmentAmount * 0.5
                    paymentIndex % 4 == 0 -> installmentAmount * 2.0
                    else -> installmentAmount
                }

                if (paymentDate <= System.currentTimeMillis()) {
                    LocalPaymentRepository.createPayment(
                        CreatePaymentInput(
                            loanId = loan.id,
                            amount = formatAmount(amount),
                            method = paymentMethod(index + paymentIndex),
                            reference = "DEMO90-REF-${index + 1}-${paymentIndex + 1}",
                            notes = "Pago demo generado automáticamente para pruebas de 90 días.",
                            paymentDateMillis = paymentDate
                        )
                    )
                }
            }

            LocalInstallmentRepository.rebuildInstallmentsForLoan(
                loanId = loan.id
            )
        }

        val finalClients = LocalClientRepository
            .getClients()
            .filter { client ->
                client.documentId.startsWith(DEMO_PREFIX)
            }

        val finalLoans = finalClients.flatMap { client ->
            LocalLoanRepository.getLoansByClient(client.id)
        }

        val finalPayments = finalLoans.flatMap { loan ->
            LocalPaymentRepository.getPaymentHistoryByLoan(loan.id)
        }

        println("DEMO90 generado correctamente")
        println("Clientes demo: ${finalClients.size}")
        println("Préstamos demo: ${finalLoans.size}")
        println("Pagos demo: ${finalPayments.size}")

        assertTrue(finalClients.isNotEmpty())
        assertTrue(finalLoans.isNotEmpty())
        assertTrue(finalPayments.isNotEmpty())
    }

    private fun demoClients(): List<DemoClient> {
        return listOf(
            DemoClient("María", "Fernández", "Sector Centro"),
            DemoClient("Carlos", "Ramírez", "Av. Principal"),
            DemoClient("Ana", "Gómez", "Barrio Norte"),
            DemoClient("José", "Martínez", "Zona Comercial"),
            DemoClient("Valentina", "Rojas", "Urbanización Sol"),
            DemoClient("Pedro", "Castillo", "Calle 8"),
            DemoClient("Daniela", "Morales", "Residencias Lago"),
            DemoClient("Luis", "Herrera", "Sector La Paz"),
            DemoClient("Camila", "Torres", "Av. Bolívar"),
            DemoClient("Miguel", "Vargas", "Calle Nueva"),
            DemoClient("Sofía", "Pérez", "Conjunto Las Palmas"),
            DemoClient("Andrés", "Suárez", "Sector El Carmen")
        )
    }

    private fun startOfDay(daysAgo: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun days(value: Long): Long {
        return value * 24L * 60L * 60L * 1000L
    }

    private fun paymentMethod(index: Int): String {
        return when (index % 4) {
            0 -> "Efectivo"
            1 -> "Transferencia"
            2 -> "Pago móvil"
            else -> "Otro"
        }
    }

    private fun formatAmount(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private data class DemoClient(
        val firstName: String,
        val lastName: String,
        val address: String
    )

    private companion object {
        const val DEMO_PREFIX = "DEMO90"
    }
}