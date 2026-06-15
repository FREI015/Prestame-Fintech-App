package com.controlprestamos.core.navigation

import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.DisposableEffect
import com.controlprestamos.features.security.data.LocalSecurityRepository
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.controlprestamos.features.auth.data.LocalAuthRepository
import com.controlprestamos.features.auth.presentation.LoginScreen
import com.controlprestamos.features.auth.presentation.RegisterScreen
import com.controlprestamos.features.clients.presentation.ClientDetailScreen
import com.controlprestamos.features.clients.presentation.ClientsScreen
import com.controlprestamos.features.clients.presentation.CreateClientScreen
import com.controlprestamos.features.clients.presentation.EditClientScreen
import com.controlprestamos.features.dashboard.presentation.DashboardScreen
import com.controlprestamos.features.loans.presentation.CreateLoanScreen
import com.controlprestamos.features.loans.presentation.EditLoanScreen
import com.controlprestamos.features.loans.presentation.LoanDetailScreen
import com.controlprestamos.features.loans.presentation.LoansByClientScreen
import com.controlprestamos.features.loans.presentation.LoansScreen
import com.controlprestamos.features.payments.presentation.CreatePaymentScreen
import com.controlprestamos.features.payments.presentation.PaymentsByLoanScreen
import com.controlprestamos.features.payments.presentation.PaymentsScreen
import com.controlprestamos.features.more.presentation.MoreScreen
import com.controlprestamos.features.help.presentation.HelpScreen
import com.controlprestamos.features.security.presentation.SecurityScreen
import com.controlprestamos.features.backup.presentation.BackupScreen
import com.controlprestamos.features.reports.presentation.ReportsScreen
import com.controlprestamos.features.preferences.presentation.PreferencesScreen
import com.controlprestamos.features.audit.presentation.FinancialAuditScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    fun navigateToLogin() {
        navController.navigate(AppRoute.Login.route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    fun navigateToDashboard() {
        navController.navigate(AppRoute.Dashboard.route) {
            launchSingleTop = true
        }
    }

    fun navigateToClients() {
        navController.navigate(AppRoute.Clients.route) {
            launchSingleTop = true
        }
    }

    fun navigateToPayments() {
        navController.navigate(AppRoute.Payments.route) {
            launchSingleTop = true
        }
    }
    fun navigateToLoans() {
        navController.navigate(AppRoute.Loans.route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(AppRoute.Dashboard.route) {
                saveState = true
            }
        }
    }
    fun navigateToMore() {
        navController.navigate(AppRoute.More.route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(AppRoute.Dashboard.route) {
                saveState = true
            }
        }
    }
    val appLockContext = LocalContext.current

    val appLockStartDestination = when {
        LocalSecurityRepository.shouldRequirePinOnLaunch(appLockContext) -> AppRoute.Login.route
        LocalAuthRepository.isSessionActive(appLockContext) -> AppRoute.Dashboard.route
        else -> AppRoute.Login.route
    }
    val autoLockLifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(autoLockLifecycleOwner, navController, appLockContext) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    LocalSecurityRepository.markBackgrounded(appLockContext)
                }

                Lifecycle.Event.ON_START -> {
                    val currentRoute = navController.currentBackStackEntry
                        ?.destination
                        ?.route

                    val shouldLock = LocalSecurityRepository
                        .shouldRequirePinAfterBackground(appLockContext)

                    if (
                        shouldLock &&
                        currentRoute != AppRoute.Login.route
                    ) {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                }

                else -> Unit
            }
        }

        autoLockLifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            autoLockLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }




    NavHost(
        navController = navController,
        startDestination = appLockStartDestination
    ) {
        composable(AppRoute.AppLock.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoute.Register.route)
                }
            )
        }
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoute.Register.route)
                }
            )
        }

        composable(AppRoute.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Dashboard.route) {
                DashboardScreen(
                    onOpenDashboard = {
                        navController.navigate(AppRoute.Dashboard.route)
                    },
                    onOpenClients = {
                        navController.navigate(AppRoute.Clients.route)
                    },
                    onOpenLoans = {
                        navController.navigate(AppRoute.Loans.route)
                    },
                    onOpenPayments = {
                        navController.navigate(AppRoute.Payments.route)
                    },
                    onOpenMore = {
                        navController.navigate(AppRoute.More.route)
                    }
                )
        }

        composable(AppRoute.Clients.route) {
            ClientsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDashboard = {
                    navigateToDashboard()
                },
                onOpenPayments = {
                    navigateToPayments()
                },
                onOpenLoans = {
                    navigateToLoans()
                },
                onOpenMore = {
                    navigateToMore()
                },
                onCreateClient = {
                    navController.navigate(AppRoute.CreateClient.route)
                },
                onOpenClient = { clientId ->
                    navController.navigate(AppRoute.ClientDetail.createRoute(clientId))
                }
            )
        }
        composable(AppRoute.Loans.route) {
            LoansScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDashboard = {
                    navigateToDashboard()
                },
                onOpenClients = {
                    navigateToClients()
                },
                onOpenPayments = {
                    navigateToPayments()
                },
                onOpenMore = {
                    navigateToMore()
                },
                onOpenLoanDetail = { loanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
                },
                onCreateLoanFromClients = {
                    navigateToClients()
                }
            )
        }
        composable(AppRoute.Payments.route) {
            PaymentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDashboard = {
                    navigateToDashboard()
                },
                onOpenClients = {
                    navigateToClients()
                },
                onOpenLoans = {
                    navigateToLoans()
                },
                onOpenMore = {
                    navigateToMore()
                },
                onRegisterPayment = { loanId ->
                    navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
                }
            )
        }
        composable(AppRoute.More.route) {
            MoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenPreferences = {
                    navController.navigate(AppRoute.Preferences.route)
                },
                onOpenSecurity = {
                    navController.navigate(AppRoute.Security.route)
                },
                onOpenBackup = {
                    navController.navigate(AppRoute.Backup.route)
                },
                onOpenReports = {
                    navController.navigate(AppRoute.Reports.route)
                },
                onOpenFinancialAudit = {
                    navController.navigate(AppRoute.FinancialAudit.route)
                },
                onOpenHelp = {
                    navController.navigate(AppRoute.Help.route)
                },
                onLogout = {
                    LocalAuthRepository.logout(appLockContext)
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.Preferences.route) {
            PreferencesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Security.route) {
            SecurityScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Backup.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Reports.route) {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.FinancialAudit.route) {
            FinancialAuditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.Help.route) {
            HelpScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoute.CreateClient.route) {
            CreateClientScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClientCreated = { clientId ->
                    navController.navigate(AppRoute.ClientDetail.createRoute(clientId)) {
                        popUpTo(AppRoute.Clients.route)
                    }
                }
            )
        }

        composable(
            route = AppRoute.ClientDetail.route,
            arguments = listOf(
                navArgument(AppRoute.ClientDetail.ARG_CLIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments
                ?.getString(AppRoute.ClientDetail.ARG_CLIENT_ID)
                .orEmpty()

            ClientDetailScreen(
                clientId = clientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreateLoan = {
                    navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
                },
                onCreatePayment = {
                    navController.navigate(AppRoute.LoansByClient.createRoute(clientId))
                },
                onLoanClick = { loanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
                },
                onOpenLoanDetail = { loanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
                },
                onEditClient = {
                    navController.navigate(AppRoute.EditClient.createRoute(clientId))
                }
            )
        }
        composable(
            route = AppRoute.LoansByClient.route,
            arguments = listOf(
                navArgument(AppRoute.LoansByClient.ARG_CLIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments
                ?.getString(AppRoute.LoansByClient.ARG_CLIENT_ID)
                .orEmpty()

            LoansByClientScreen(
                clientId = clientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDashboard = {
                    navigateToDashboard()
                },
                onOpenClients = {
                    navigateToClients()
                },
                onOpenPayments = {
                    navigateToPayments()
                },
                onOpenMore = {
                    navigateToMore()
                },
                onCreateLoan = {
                    navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
                },
                onOpenLoan = { loanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
                }
            ,
                onCreatePayment = { loanId ->
                    navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
                })
        }

        composable(
            route = AppRoute.CreateLoan.route,
            arguments = listOf(
                navArgument(AppRoute.CreateLoan.ARG_CLIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments
                ?.getString(AppRoute.CreateLoan.ARG_CLIENT_ID)
                .orEmpty()

            CreateLoanScreen(
                clientId = clientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoanCreated = { loanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
                        popUpTo(AppRoute.CreateLoan.createRoute(clientId)) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = AppRoute.LoanDetail.route,
            arguments = listOf(
                navArgument(AppRoute.LoanDetail.ARG_LOAN_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments
                ?.getString(AppRoute.LoanDetail.ARG_LOAN_ID)
                .orEmpty()

            LoanDetailScreen(
                loanId = loanId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreatePayment = {
                    navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
                },
                onOpenPayments = {
                    navController.navigate(AppRoute.PaymentsByLoan.createRoute(loanId))
                }
                ,
                onEditLoan = {
                    navController.navigate(AppRoute.EditLoan.createRoute(loanId))
                }
            )
        }

        composable(
            route = AppRoute.PaymentsByLoan.route,
            arguments = listOf(
                navArgument(AppRoute.PaymentsByLoan.ARG_LOAN_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments
                ?.getString(AppRoute.PaymentsByLoan.ARG_LOAN_ID)
                .orEmpty()

            PaymentsByLoanScreen(
                loanId = loanId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreatePayment = {
                    navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
                }
            )
        }

        composable(
            route = AppRoute.CreatePayment.route,
            arguments = listOf(
                navArgument(AppRoute.CreatePayment.ARG_LOAN_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments
                ?.getString(AppRoute.CreatePayment.ARG_LOAN_ID)
                .orEmpty()

            CreatePaymentScreen(
                loanId = loanId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentCreated = {
                    navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
                        popUpTo(AppRoute.CreatePayment.createRoute(loanId)) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = AppRoute.EditClient.route,
            arguments = listOf(
                navArgument(AppRoute.EditClient.ARG_CLIENT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments
                ?.getString(AppRoute.EditClient.ARG_CLIENT_ID)
                .orEmpty()

            EditClientScreen(
                clientId = clientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClientUpdated = { updatedClientId ->
                    navController.navigate(AppRoute.ClientDetail.createRoute(updatedClientId)) {
                        popUpTo(AppRoute.EditClient.createRoute(updatedClientId)) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppRoute.EditLoan.route,
            arguments = listOf(
                navArgument(AppRoute.EditLoan.ARG_LOAN_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments
                ?.getString(AppRoute.EditLoan.ARG_LOAN_ID)
                .orEmpty()

            EditLoanScreen(
                loanId = loanId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoanUpdated = { updatedLoanId ->
                    navController.navigate(AppRoute.LoanDetail.createRoute(updatedLoanId)) {
                        popUpTo(AppRoute.EditLoan.createRoute(updatedLoanId)) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
































