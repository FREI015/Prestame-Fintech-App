package com.controlprestamos.core.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Dashboard : AppRoute("dashboard")
    data object Clients : AppRoute("clients")
    data object Payments : AppRoute("payments")
    data object Loans : AppRoute("loans")
    data object More : AppRoute("more")
    data object Help : AppRoute("help")
    data object Reports : AppRoute("reports")
    data object Backup : AppRoute("backup")
    data object Security : AppRoute("security")
    data object Preferences : AppRoute("preferences")
    data object FinancialAudit : AppRoute("financial_audit")
    data object CreateClient : AppRoute("clients/create")

    data object ClientDetail : AppRoute("clients/{clientId}") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId"
        }
    }

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

    data object LoanDetail : AppRoute("loans/{loanId}") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId"
        }
    }

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

    data object EditClient : AppRoute("clients/{clientId}/edit") {
        const val ARG_CLIENT_ID = "clientId"

        fun createRoute(clientId: String): String {
            return "clients/$clientId/edit"
        }
    }

    data object AppLock : AppRoute("app_lock")

    data object EditLoan : AppRoute("loans/{loanId}/edit") {
        const val ARG_LOAN_ID = "loanId"

        fun createRoute(loanId: String): String {
            return "loans/$loanId/edit"
        }
    }
}







