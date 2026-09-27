package com.appfinanzas.prototype.ui.screens.investments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.ui.components.AssetRow
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceEmptyState
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.MoneyMetric
import com.appfinanzas.prototype.ui.components.SegmentedFilter
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.format.PercentageFormatter
import com.appfinanzas.prototype.ui.navigation.Routes

private val filterOptions = listOf("Todas", "Renta variable", "Renta fija", "SOFIPO", "Otros")

private fun categoryFor(label: String): InvestmentCategory? = when (label) {
    "Renta variable" -> InvestmentCategory.RENTA_VARIABLE
    "Renta fija" -> InvestmentCategory.RENTA_FIJA
    "SOFIPO" -> InvestmentCategory.SOFIPO
    "Otros" -> InvestmentCategory.OTROS
    else -> null
}

private fun labelFor(category: InvestmentCategory?): String = category?.label ?: "Todas"

@Composable
fun InvestmentsScreen(onNavigate: (String) -> Unit) {
    val viewModel: InvestmentsViewModel = viewModel(factory = InvestmentsViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Inversiones",
        subtitle = "Todos tus activos. En un solo lugar.",
        selectedTab = Routes.INVESTMENTS,
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
            else -> InvestmentsContent(
                state = state,
                onSelectFilter = viewModel::selectFilter,
                onNavigate = onNavigate,
            )
        }
    }
}

@Composable
private fun InvestmentsContent(
    state: InvestmentsUiState,
    onSelectFilter: (InvestmentCategory?) -> Unit,
    onNavigate: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FinancePanel(Modifier.weight(1f)) {
            MoneyMetric(label = "Valor total", amount = state.totalValue)
        }
        FinancePanel(Modifier.weight(1f)) {
            MoneyMetric(label = "Ganancia", amount = state.totalProfit, accent = true)
        }
    }
    Spacer(Modifier.height(8.dp))
    SegmentedFilter(
        options = filterOptions,
        selected = labelFor(state.filter),
        onSelect = { label -> onSelectFilter(categoryFor(label)) },
    )
    Spacer(Modifier.height(8.dp))
    state.investments.forEach { item ->
        AssetRow(
            name = item.name,
            subtitle = item.subtitle,
            value = MoneyFormatter.format(item.value),
            change = PercentageFormatter.format(item.change),
            onClick = { onNavigate(Routes.investmentDetail(item.id)) },
        )
        Spacer(Modifier.height(4.dp))
    }
    Spacer(Modifier.height(8.dp))
    FinanceButton(text = "+ Agregar inversión") { onNavigate(Routes.ADD_INVESTMENT) }
}