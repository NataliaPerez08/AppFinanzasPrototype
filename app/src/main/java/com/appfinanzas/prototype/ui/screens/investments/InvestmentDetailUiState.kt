package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentDetail
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.ui.format.DateFormatter

data class InvestmentDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val name: String = "",
    val subtitle: String = "",
    val currentPrice: Double = 0.0,
    val currency: Currency = Currency.MXN,
    val priceChange: Double = 0.0,
    val dailyChangePercentage: Double = 0.0,
    val quantity: Double = 0.0,
    val investedCapital: Double = 0.0,
    val currentValue: Double = 0.0,
    val cashBalance: Double = 0.0,
    val averageCost: Double = 0.0,
    val realizedProfit: Double = 0.0,
    val profit: Double = 0.0,
    val performance: Double = 0.0,
    val history: List<Float> = emptyList(),
    val transactions: List<TransactionUi> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
)

data class TransactionUi(
    val id: Long,
    val dateLabel: String,
    val typeLabel: String,
    val amount: Double,
    val currency: Currency,
)

internal fun InvestmentDetail.toUiState(): InvestmentDetailUiState {
    val investment = this.investment
    return InvestmentDetailUiState(
        name = investment.name,
        subtitle = "${investment.description} · ${investment.institution.name} · ${investment.type.category.label}",
        currentPrice = investment.currentPrice,
        currency = investment.currency,
        priceChange = investment.priceChange,
        dailyChangePercentage = investment.dailyChangePercentage,
        quantity = investment.quantity,
        investedCapital = investment.investedCapital,
        currentValue = investment.currentValue,
        cashBalance = investment.cashBalance,
        averageCost = investment.averageCost,
        realizedProfit = investment.realizedProfit,
        profit = investment.currentValue - investment.investedCapital,
        performance = investment.returnPercentage,
        history = investment.history,
        transactions = transactions.map { it.toUi() },
    )
}

private fun Transaction.toUi(): TransactionUi =
    TransactionUi(
        id = id,
        dateLabel = DateFormatter.format(date),
        typeLabel = type.name,
        amount = total,
        currency = currency,
    )
