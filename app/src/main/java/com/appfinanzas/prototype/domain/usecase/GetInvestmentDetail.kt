package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.InvestmentDetail
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetInvestmentDetail(
    private val repository: InvestmentRepository,
) {
    fun observe(investmentId: Long): Flow<InvestmentDetail?> =
        combine(
            repository.observeInvestment(investmentId),
            repository.observeTransactions(investmentId),
        ) { investment, transactions ->
            investment?.let { InvestmentDetail(investment = it, transactions = transactions) }
        }
}