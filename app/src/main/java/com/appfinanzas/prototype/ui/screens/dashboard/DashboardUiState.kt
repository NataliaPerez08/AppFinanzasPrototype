package com.appfinanzas.prototype.ui.screens.dashboard

import com.appfinanzas.prototype.domain.model.DashboardSummary
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.components.AllocationItem

data class DashboardUiState(
    val isLoading: Boolean = false,
    val baseCurrency: Currency = Currency.MXN,
    val portfolioValue: Double = 0.0,
    val dailyChange: Double = 0.0,
    val dailyChangePercentage: Double = 0.0,
    val investedCapital: Double = 0.0,
    val profit: Double = 0.0,
    val performance: Double = 0.0,
    val allocation: List<AllocationItem> = emptyList(),
    val history: List<Float> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
)

internal fun DashboardSummary.toUiState(): DashboardUiState {
    val totalAllocation = allocation.sumOf { it.value }
    return DashboardUiState(
        portfolioValue = portfolioValue,
        baseCurrency = baseCurrency,
        dailyChange = dailyChange,
        dailyChangePercentage = dailyChangePercentage,
        investedCapital = investedCapital,
        profit = profit,
        performance = performance,
        allocation = allocation.map { item ->
            AllocationItem(
                label = item.category.label,
                percentage = if (totalAllocation > 0.0) (item.value / totalAllocation * 100.0).toFloat() else 0f,
            )
        },
        history = history,
        isEmpty = investmentCount == 0,
    )
}
