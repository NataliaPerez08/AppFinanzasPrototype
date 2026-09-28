package com.appfinanzas.prototype.ui.screens.projection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PortfolioChart
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun ProjectionScreen(onNavigate: (String) -> Unit) {
    val viewModel: ProjectionViewModel = viewModel(factory = ProjectionViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Proyección a 1 año",
        subtitle = "Escenarios para tu patrimonio",
        selectedTab = Routes.PROJECTION,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::retry)
            state.isEmpty -> FinanceEmptyState(
                title = "SIN PATRIMONIO",
                message = "Registra tus inversiones\npara proyectar tu patrimonio.",
                actionLabel = "+ Agregar inversión",
                onAction = { onNavigate(Routes.ADD_INVESTMENT) },
            )
            else -> ProjectionContent(state, viewModel)
        }
    }
}

@Composable
private fun ProjectionContent(
    state: ProjectionUiState,
    viewModel: ProjectionViewModel,
) {
    FinanceTextField(
        label = "Rendimiento esperado anual",
        value = state.expectedReturnText,
        onValueChange = viewModel::onExpectedReturnChange,
    )
    FinanceTextField(
        label = "Inflación estimada anual",
        value = state.inflationText,
        onValueChange = viewModel::onInflationChange,
    )
    FinanceTextField(
        label = "ISR estimado",
        value = state.isrText,
        onValueChange = viewModel::onIsrChange,
    )
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Resultados estimados")
        MoneyMetric(label = "Valor actual", amount = state.currentValue)
        MoneyMetric(label = "Valor nominal", amount = state.nominal)
        MoneyMetric(label = "Después de ISR", amount = state.afterIsr, accent = true)
        MoneyMetric(label = "Valor real", amount = state.real)
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Escenarios")
        state.scenarios.forEach { scenario ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = scenario.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = FinanzasColors.Text,
                    )
                    FinanceLabel("REAL")
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MoneyFormatter.format(scenario.nominal),
                        style = MaterialTheme.typography.titleSmall,
                        color = FinanzasColors.Text,
                    )
                    Text(
                        text = MoneyFormatter.format(scenario.real),
                        style = MaterialTheme.typography.labelMedium,
                        color = FinanzasColors.Accent,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        SectionHeader(title = "Escenario base")
        PortfolioChart(
            points = state.chartPoints,
            contentDescription = "Proyección del patrimonio a un año",
        )
    }
}