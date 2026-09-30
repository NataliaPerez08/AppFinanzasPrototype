package com.appfinanzas.prototype.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appfinanzas.prototype.ui.screens.dashboard.DashboardScreen
import com.appfinanzas.prototype.ui.screens.investments.AddInvestmentScreen
import com.appfinanzas.prototype.ui.screens.investments.AddTransactionScreen
import com.appfinanzas.prototype.ui.screens.investments.InvestmentDetailScreen
import com.appfinanzas.prototype.ui.screens.investments.InvestmentsScreen
import com.appfinanzas.prototype.ui.screens.more.InstitutionsScreen
import com.appfinanzas.prototype.ui.screens.more.MoreScreen
import com.appfinanzas.prototype.ui.screens.more.SettingsScreen
import com.appfinanzas.prototype.ui.screens.portfolio.PortfolioScreen
import com.appfinanzas.prototype.ui.screens.projection.ProjectionScreen
import com.appfinanzas.prototype.ui.theme.FinanzasColors
import com.appfinanzas.prototype.ui.theme.FinanzasTheme

@Composable
fun FinanzasApp() {
    val navController = rememberNavController()
    FinanzasTheme {
        Box(Modifier.fillMaxSize().background(FinanzasColors.Background)) {
            GridBackground()
            NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(onNavigate = navController::navigate)
                }
                composable(Routes.INVESTMENTS) {
                    InvestmentsScreen(onNavigate = navController::navigate)
                }
                composable(
                    route = Routes.INVESTMENT_DETAIL,
                    arguments = listOf(
                        navArgument(Routes.ARG_INVESTMENT_ID) { type = NavType.LongType },
                    ),
                ) { entry ->
                    val investmentId = entry.arguments?.getLong(Routes.ARG_INVESTMENT_ID) ?: 0L
                    InvestmentDetailScreen(
                        investmentId = investmentId,
                        onNavigate = navController::navigate,
                    )
                }
                composable(Routes.ADD_INVESTMENT) {
                    AddInvestmentScreen(onNavigate = navController::navigate)
                }
                composable(
                    route = Routes.EDIT_INVESTMENT,
                    arguments = listOf(
                        navArgument(Routes.ARG_INVESTMENT_ID) { type = NavType.LongType },
                    ),
                ) { entry ->
                    val investmentId = entry.arguments?.getLong(Routes.ARG_INVESTMENT_ID) ?: 0L
                    AddInvestmentScreen(
                        investmentId = investmentId,
                        onNavigate = navController::navigate,
                    )
                }
                composable(
                    route = Routes.ADD_TRANSACTION,
                    arguments = listOf(
                        navArgument(Routes.ARG_INVESTMENT_ID) { type = NavType.LongType },
                    ),
                ) { entry ->
                    val investmentId = entry.arguments?.getLong(Routes.ARG_INVESTMENT_ID) ?: 0L
                    AddTransactionScreen(
                        investmentId = investmentId,
                        onNavigate = navController::navigate,
                    )
                }
                composable(
                    route = Routes.EDIT_TRANSACTION,
                    arguments = listOf(
                        navArgument(Routes.ARG_INVESTMENT_ID) { type = NavType.LongType },
                        navArgument(Routes.ARG_TRANSACTION_ID) { type = NavType.LongType },
                    ),
                ) { entry ->
                    val investmentId = entry.arguments?.getLong(Routes.ARG_INVESTMENT_ID) ?: 0L
                    val transactionId = entry.arguments?.getLong(Routes.ARG_TRANSACTION_ID) ?: 0L
                    AddTransactionScreen(
                        investmentId = investmentId,
                        transactionId = transactionId,
                        onNavigate = navController::navigate,
                    )
                }
                composable(Routes.PORTFOLIO) {
                    PortfolioScreen(onNavigate = navController::navigate)
                }
                composable(Routes.PROJECTION) {
                    ProjectionScreen(onNavigate = navController::navigate)
                }
                composable(Routes.MORE) {
                    MoreScreen(onNavigate = navController::navigate)
                }
                composable(Routes.INSTITUTIONS) {
                    InstitutionsScreen(onNavigate = navController::navigate)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(onNavigate = navController::navigate)
                }
            }
        }
    }
}

@Composable
private fun GridBackground() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 16.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(FinanzasColors.Divider.copy(alpha = 0.18f), Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(FinanzasColors.Divider.copy(alpha = 0.18f), Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }
    }
}