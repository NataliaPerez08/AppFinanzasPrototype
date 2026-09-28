package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.CategoryAllocation
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.NamedAllocation
import com.appfinanzas.prototype.domain.model.PortfolioSummary
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPortfolioSummary(
    private val repository: InvestmentRepository,
) {
    fun observe(): Flow<PortfolioSummary> =
        repository.observeInvestments().map { summarize(it) }

    fun summarize(investments: List<Investment>): PortfolioSummary {
        val portfolioValue = investments.sumOf { it.currentValue }
        val investedCapital = investments.sumOf { it.investedCapital }
        val profit = portfolioValue - investedCapital
        val performance = if (investedCapital > 0.0) profit / investedCapital * 100.0 else 0.0

        val byCategory = investments
            .groupBy { it.type.category }
            .map { (category, group) -> CategoryAllocation(category, group.sumOf { it.currentValue }) }
            .sortedByDescending { it.value }

        val byInstitution = investments
            .groupBy { it.institution.name }
            .map { (name, group) -> NamedAllocation(name, group.sumOf { it.currentValue }) }
            .sortedByDescending { it.value }

        val byCurrency = investments
            .groupBy { it.currency }
            .map { (currency, group) -> NamedAllocation(currency.code, group.sumOf { it.currentValue }) }
            .sortedByDescending { it.value }

        return PortfolioSummary(
            portfolioValue = portfolioValue,
            investedCapital = investedCapital,
            profit = profit,
            performance = performance,
            byCategory = byCategory,
            byInstitution = byInstitution,
            byCurrency = byCurrency,
            investmentCount = investments.size,
        )
    }
}