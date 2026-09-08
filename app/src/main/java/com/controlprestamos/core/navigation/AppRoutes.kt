package com.controlprestamos.core.navigation

sealed class AppRoute(val route: String) {

    // Auth
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object AppLock : AppRoute("app_lock")

    // Main navigation
    data object Dashboard : AppRoute("dashboard")
    data object Clients : AppRoute("clients")
    data object Loans : AppRoute("loans")
    data object Payments : AppRoute("payments")
    data object More : AppRoute("more")

    // More / tools
    data object Reports : AppRoute("reports")
    data object Backup : AppRoute("backup")
    data object Security : AppRoute("security")
    data object Preferences : AppRoute("preferences")
    data object Help : AppRoute("help")
    data object FinancialAudit : AppRoute("financial_audit")

    // Clients
    data object CreateClient : AppRoute("clients/create")

    data object ClientDetail : AppRoute("clients/{clientId}") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId"
        }
    }

    data object EditClient : AppRoute("clients/{clientId}/edit") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId/edit"
        }
    }

    // Client loans
    data object LoansByClient : AppRoute("clients/{clientId}/loans") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId/loans"
        }
    }

    data object CreateLoan : AppRoute("clients/{clientId}/loans/create") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId/loans/create"
        }
    }

    // Loans
    data object LoanDetail : AppRoute("loans/{loanId}") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId"
        }
    }

    data object EditLoan : AppRoute("loans/{loanId}/edit") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId/edit"
        }
    }

    // Payments
    data object PaymentsByLoan : AppRoute("loans/{loanId}/payments") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId/payments"
        }
    }

    data object CreatePayment : AppRoute("loans/{loanId}/payments/create") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId/payments/create"
        }
    }
}

