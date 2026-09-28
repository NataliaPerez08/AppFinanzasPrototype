package com.appfinanzas.prototype.ui.screens.portfolio

import com.appfinanzas.prototype.domain.model.CategoryAllocation
import com.appfinanzas.prototype.domain.model.NamedAllocation
import com.appfinanzas.prototype.domain.model.PortfolioSummary
import com.appfinanzas.prototype.ui.components.AllocationItem

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val portfolioValue: Double = 0.0,
    val investedCapital: Double = 0.0,
    val profit: Double = 0.0,
    val performance: Double = 0.0,
    val byCategory: List<AllocationItem> = emptyList(),
    val byInstitution: List<AllocationItem> = emptyList(),
    val byCurrency: List<AllocationItem> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
)

internal fun PortfolioSummary.toUiState(): PortfolioUiState {
    val total = portfolioValue
    fun toAllocation(label: String, value: Double): AllocationItem =
        AllocationItem(
            label = label,
            percentage = if (total > 0.0) (value / total * 100.0).toFloat() else 0f,
        )
    return PortfolioUiState(
        portfolioValue = portfolioValue,
        investedCapital = investedCapital,
        profit = profit,
        performance = performance,
        byCategory = byCategory.map { toAllocation(it.category.label, it.value) },
        byInstitution = byInstitution.map { toAllocation(it.label, it.value) },
        byCurrency = byCurrency.map { toAllocation(it.label, it.value) },
        isEmpty = investmentCount == 0,
    )
}

internal fun CategoryAllocation.toUiAllocation(total: Double): AllocationItem =
    AllocationItem(
        label = category.label,
        percentage = if (total > 0.0) (value / total * 100.0).toFloat() else 0f,
    )

internal fun NamedAllocation.toUiAllocation(total: Double): AllocationItem =
    AllocationItem(
        label = label,
        percentage = if (total > 0.0) (value / total * 100.0).toFloat() else 0f,
    )