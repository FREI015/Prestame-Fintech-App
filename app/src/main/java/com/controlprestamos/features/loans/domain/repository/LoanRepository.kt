package com.controlprestamos.features.loans.domain.repository

import com.controlprestamos.features.loans.domain.model.CreateLoanInput
import com.controlprestamos.features.loans.domain.model.Loan
import com.controlprestamos.features.loans.domain.model.UpdateLoanInput

interface LoanRepository {
    fun getAllLoans(): List<Loan>

    fun getLoansByClient(clientId: String): List<Loan>

    fun getLoanById(loanId: String): Loan?

    fun createLoan(input: CreateLoanInput): Loan

    fun updateLoan(input: UpdateLoanInput): Loan?

    fun cancelLoan(
        loanId: String,
        reason: String
    ): Boolean
}
