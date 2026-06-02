package com.fintrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fintrack.domain.repository.AuthRepository
import com.fintrack.ui.auth.LoginScreen
import com.fintrack.ui.auth.RegisterScreen
import com.fintrack.ui.bills.BillScreen
import com.fintrack.ui.budgets.BudgetScreen
import com.fintrack.ui.dashboard.DashboardScreen
import com.fintrack.ui.payments.PaymentScreen
import com.fintrack.ui.transactions.AddTransactionScreen
import com.fintrack.ui.transactions.TransactionScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object Budgets : Screen("budgets")
    object Bills : Screen("bills")
    object Payments : Screen("payments")
}

@Composable
fun FinTrackNavGraph(navController: NavHostController) {
    val authRepository: AuthRepository = koinInject()
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) },
                onNavigateToBills = { navController.navigate(Screen.Bills.route) },
                onNavigateToPayments = { navController.navigate(Screen.Payments.route) },
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionScreen(
                onBack = { navController.popBackStack() },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Budgets.route) {
            BudgetScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Bills.route) {
            BillScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Payments.route) {
            PaymentScreen(onBack = { navController.popBackStack() })
        }
    }
}
