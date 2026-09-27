package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentsData
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetInvestments(
    private val repository: InvestmentRepository,
) {
    fun observe(filter: InvestmentCategory?): Flow<InvestmentsData> =
        repository.observeInvestments().map { investments ->
            InvestmentsData(
                totalValue = investments.sumOf { it.currentValue },
                totalProfit = investments.sumOf { it.currentValue - it.investedCapital },
                investments = if (filter == null) {
                    investments
                } else {
                    investments.filter { it.type.category == filter }
                },
            )
        }
}