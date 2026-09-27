package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentsData

data class InvestmentsUiState(
    val isLoading: Boolean = false,
    val totalValue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val filter: InvestmentCategory? = null,
    val investments: List<InvestmentUi> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
)

data class InvestmentUi(
    val id: Long,
    val name: String,
    val subtitle: String,
    val value: Double,
    val change: Double,
    val currency: Currency,
)

internal fun InvestmentsData.toUiState(filter: InvestmentCategory?): InvestmentsUiState =
    InvestmentsUiState(
        totalValue = totalValue,
        totalProfit = totalProfit,
        filter = filter,
        investments = investments.map { it.toUi() },
        isEmpty = totalValue == 0.0,
    )

internal fun Investment.toUi(): InvestmentUi =
    InvestmentUi(
        id = id,
        name = name,
        subtitle = "${institution.name} · ${type.category.label}",
        value = currentValue,
        change = returnPercentage,
        currency = currency,
    )