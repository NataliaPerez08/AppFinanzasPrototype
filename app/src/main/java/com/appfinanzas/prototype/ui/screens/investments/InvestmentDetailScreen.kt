package com.appfinanzas.prototype.ui.screens.investments

import androidx.compose.foundation.layout.Arrangement
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
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.MetricCard
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.PercentageMetric
import com.appfinanzas.prototype.ui.components.PortfolioChart
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors
import java.util.Locale

@Composable
fun InvestmentDetailScreen(
    investmentId: Long,
    onNavigate: (String) -> Unit,
) {
    val viewModel: InvestmentDetailViewModel = viewModel(factory = InvestmentDetailViewModel.factory(investmentId))
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = state.name.ifBlank { "Detalle de inversión" },
        subtitle = state.subtitle,
        selectedTab = Routes.INVESTMENTS,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::retry)
            state.isEmpty -> FinanceEmptyState(
                title = "INVERSIÓN NO ENCONTRADA",
                message = "No existe una inversión con ese identificador.",
                actionLabel = "+ Agregar inversión",
                onAction = { onNavigate(Routes.ADD_INVESTMENT) },
            )
            else -> InvestmentDetailContent(
                investmentId = investmentId,
                state = state,
                onNavigate = onNavigate,
            )
        }
    }
}

@Composable
private fun InvestmentDetailContent(
    investmentId: Long,
    state: InvestmentDetailUiState,
    onNavigate: (String) -> Unit,
) {
    FinancePanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MoneyMetric(
                label = "Precio actual ${state.currency.code}",
                amount = state.currentPrice,
                currency = state.currency,
            )
            PercentageMetric(label = "Variación hoy", percentage = state.dailyChangePercentage)
        }
        PortfolioChart(
            points = state.history,
            contentDescription = "Historial del precio del instrumento",
        )
    }
    Spacer(Modifier.height(8.dp))
    FinancePanel {
        MetricCard(label = "Cantidad", value = String.format(Locale.US, "%.2f", state.quantity))
        MoneyMetric(label = "Capital invertido", amount = state.investedCapital)
        MoneyMetric(label = "Valor actual MXN", amount = state.currentValue)
        MoneyMetric(label = "Ganancia", amount = state.profit, accent = true)
        PercentageMetric(label = "Rendimiento", percentage = state.performance)
    }
    Spacer(Modifier.height(8.dp))
    FinanceButton(text = "Registrar movimiento") {
        onNavigate(Routes.addTransaction(investmentId))
    }
    if (state.transactions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        FinancePanel {
            SectionHeader(title = "Movimientos")
            state.transactions.forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = transaction.typeLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = FinanzasColors.Text,
                        )
                        FinanceLabel(transaction.dateLabel)
                    }
                    Text(
                        text = MoneyFormatter.format(transaction.amount, transaction.currency),
                        style = MaterialTheme.typography.titleSmall,
                        color = FinanzasColors.Text,
                    )
                }
            }
        }
    }
}