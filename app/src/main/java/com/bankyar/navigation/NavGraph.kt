package com.bankyar.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.bankyar.data.database.AppDatabase
import com.bankyar.data.repository.UserRepository
import com.bankyar.ui.screens.*
import com.bankyar.ui.viewmodels.AuthViewModel
import com.bankyar.ui.viewmodels.TransactionViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction?editId={editId}") {
        fun route(editId: Int = -1) = "add_transaction?editId=$editId"
    }
    object TransactionDetail : Screen("transaction/{id}") {
        fun route(id: Int) = "transaction/$id"
    }
}

@Composable
fun BankYarNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val loggedInUserId by authViewModel.loggedInUserId.collectAsState()
    val context = LocalContext.current
    var userName by remember { mutableStateOf("کاربر") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loggedInUserId) {
        if (loggedInUserId > 0) {
            scope.launch {
                val repo = UserRepository(AppDatabase.getInstance(context).userDao())
                repo.getUserById(loggedInUserId).collect { user ->
                    user?.let { userName = it.name }
                }
            }
        }
    }

    val startDest = if (loggedInUserId > 0) Screen.Home.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDest) {
        composable(Screen.Auth.route) {
            AuthScreen(authViewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Home.route) {
            val userId = loggedInUserId
            if (userId > 0) {
                HomeScreen(
                    userId = userId,
                    userName = userName,
                    viewModel = transactionViewModel,
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.route()) },
                    onViewAll = { navController.navigate(Screen.Transactions.route) },
                    onTransactionClick = { navController.navigate(Screen.TransactionDetail.route(it)) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route()) },
                onTransactionClick = { navController.navigate(Screen.TransactionDetail.route(it)) }
            )
        }

        composable(
            Screen.AddTransaction.route,
            arguments = listOf(navArgument("editId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStack ->
            val editId = backStack.arguments?.getInt("editId") ?: -1
            AddTransactionScreen(
                userId = loggedInUserId,
                editId = editId,
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.TransactionDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id") ?: return@composable
            TransactionDetailScreen(
                transactionId = id,
                viewModel = transactionViewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.AddTransaction.route(it)) }
            )
        }
    }
}
