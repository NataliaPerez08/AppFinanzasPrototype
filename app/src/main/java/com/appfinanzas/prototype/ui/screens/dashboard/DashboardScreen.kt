package com.appfinanzas.prototype.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.AllocationBar
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PortfolioChart
import com.appfinanzas.prototype.ui.components.PrimaryMetric
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.format.PercentageFormatter
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Mi patrimonio",
        subtitle = "Disciplina hoy. Libertad mañana.",
        selectedTab = Routes.DASHBOARD,
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
            else -> DashboardContent(state)
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState) {
    FinancePanel {
        PrimaryMetric(
            label = "Valor actual",
            value = MoneyFormatter.format(state.portfolioValue),
        )
        Text(
            text = "${MoneyFormatter.format(state.dailyChange)}  ${PercentageFormatter.format(state.dailyChangePercentage)}  HOY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = FinanzasColors.Accent,
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FinancePanel(Modifier.weight(1f)) {
            MoneyMetric(label = "Capital aportado", amount = state.investedCapital)
        }
        FinancePanel(Modifier.weight(1f)) {
            MoneyMetric(label = "Ganancia", amount = state.profit, accent = true)
        }
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Evolución 2023-2026")
        PortfolioChart(
            points = state.history,
            contentDescription = "Evolución histórica del patrimonio",
        )
        Text(
            text = "1D   1S   1M   3M   1A   TODOS",
            style = MaterialTheme.typography.labelSmall,
            color = FinanzasColors.Text,
        )
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Distribución por activo")
        AllocationBar(items = state.allocation)
    }
}
