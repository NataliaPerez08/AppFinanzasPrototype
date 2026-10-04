package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentsData
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.money.CurrencyConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GetInvestments(
    private val repository: InvestmentRepository,
) {
    fun observe(filter: InvestmentCategory?): Flow<InvestmentsData> =
        repository.observeInvestments().combine(repository.observeSettings()) { investments, settings ->
            val converted = { investment: com.appfinanzas.prototype.domain.model.Investment, amount: Double ->
                CurrencyConverter.convert(amount, investment.currency, settings.baseCurrency, settings.usdToMxnRate)
            }
            InvestmentsData(
                baseCurrency = settings.baseCurrency,
                totalValue = investments.sumOf { converted(it, it.currentValue) },
                totalProfit = investments.sumOf { converted(it, it.currentValue - it.investedCapital) },
                investments = if (filter == null) {
                    investments
                } else {
                    investments.filter { it.type.category == filter }
                },
            )
        }
}
