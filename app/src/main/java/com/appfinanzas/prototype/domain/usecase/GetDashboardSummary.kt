package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.CategoryAllocation
import com.appfinanzas.prototype.domain.model.DashboardSummary
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDashboardSummary(
    private val repository: InvestmentRepository,
) {
    fun observe(): Flow<DashboardSummary> =
        combine(
            repository.observeInvestments(),
            repository.observePortfolioHistory(),
        ) { investments, history ->
            summarize(investments, history)
        }

    fun summarize(investments: List<Investment>, history: List<Float>): DashboardSummary {
        val portfolioValue = investments.sumOf { it.currentValue }
        val investedCapital = investments.sumOf { it.investedCapital }
        val profit = portfolioValue - investedCapital
        val dailyChange = investments.sumOf { it.dailyValueChange }
        val performance = if (investedCapital > 0.0) profit / investedCapital * 100.0 else 0.0
        val previousValue = portfolioValue - dailyChange
        val dailyChangePercentage = if (previousValue > 0.0) dailyChange / previousValue * 100.0 else 0.0
        val allocation = investments
            .groupBy { it.type.category }
            .map { (category, group) -> CategoryAllocation(category, group.sumOf { it.currentValue }) }
            .sortedByDescending { it.value }
        return DashboardSummary(
            portfolioValue = portfolioValue,
            dailyChange = dailyChange,
            dailyChangePercentage = dailyChangePercentage,
            investedCapital = investedCapital,
            profit = profit,
            performance = performance,
            allocation = allocation,
            history = history,
            investmentCount = investments.size,
        )
    }
}