package com.appfinanzas.prototype.ui.screens.portfolio

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.AllocationBar
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PercentageMetric
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun PortfolioScreen(onNavigate: (String) -> Unit) {
    val viewModel: PortfolioViewModel = viewModel(factory = PortfolioViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Patrimonio",
        subtitle = "Análisis y distribución de tu portafolio",
        selectedTab = Routes.PORTFOLIO,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::retry)
            state.isEmpty -> FinanceEmptyState(
                title = "NO HAY INVERSIONES",
                message = "Registra tu primera inversión\npara comenzar a analizar\ntu patrimonio.",
                actionLabel = "+ Agregar inversión",
                onAction = { onNavigate(Routes.ADD_INVESTMENT) },
            )
            else -> PortfolioContent(state)
        }
    }
}

@Composable
private fun PortfolioContent(state: PortfolioUiState) {
    FinancePanel {
        MoneyMetric(label = "Valor actual", amount = state.portfolioValue)
        MoneyMetric(label = "Capital aportado", amount = state.investedCapital)
        MoneyMetric(label = "Ganancia", amount = state.profit, accent = true)
        PercentageMetric(label = "Rendimiento", percentage = state.performance)
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Por tipo")
        AllocationBar(items = state.byCategory)
        Spacer(Modifier.height(12.dp))
        SectionHeader(title = "Por institución")
        AllocationBar(items = state.byInstitution)
        Spacer(Modifier.height(12.dp))
        SectionHeader(title = "Por moneda")
        AllocationBar(items = state.byCurrency)
    }
}