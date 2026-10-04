package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.CategoryAllocation
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.NamedAllocation
import com.appfinanzas.prototype.domain.model.PortfolioSummary
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.money.CurrencyConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GetPortfolioSummary(
    private val repository: InvestmentRepository,
) {
    fun observe(): Flow<PortfolioSummary> =
        repository.observeInvestments().combine(repository.observeSettings()) { investments, settings ->
            summarize(investments, settings)
        }

    fun summarize(
        investments: List<Investment>,
        settings: com.appfinanzas.prototype.domain.model.AppSettings = com.appfinanzas.prototype.domain.model.AppSettings(),
    ): PortfolioSummary {
        fun value(investment: Investment, amount: Double) = CurrencyConverter.convert(
            amount,
            investment.currency,
            settings.baseCurrency,
            settings.usdToMxnRate,
        )
        val portfolioValue = investments.sumOf { value(it, it.currentValue) }
        val investedCapital = investments.sumOf { value(it, it.investedCapital) }
        val profit = portfolioValue - investedCapital
        val performance = if (investedCapital > 0.0) profit / investedCapital * 100.0 else 0.0

        val byCategory = investments
            .groupBy { it.type.category }
            .map { (category, group) -> CategoryAllocation(category, group.sumOf { value(it, it.currentValue) }) }
            .sortedByDescending { it.value }

        val byInstitution = investments
            .groupBy { it.institution.name }
            .map { (name, group) -> NamedAllocation(name, group.sumOf { value(it, it.currentValue) }) }
            .sortedByDescending { it.value }

        val byCurrency = investments
            .groupBy { it.currency }
            .map { (currency, group) ->
                NamedAllocation(currency.code, group.sumOf { value(it, it.currentValue) })
            }
            .sortedByDescending { it.value }

        return PortfolioSummary(
            baseCurrency = settings.baseCurrency,
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
