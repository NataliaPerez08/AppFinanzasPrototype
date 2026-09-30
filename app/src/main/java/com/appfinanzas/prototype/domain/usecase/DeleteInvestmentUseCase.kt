package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.repository.InvestmentRepository

sealed class DeleteInvestmentResult {
    data class Success(val investmentId: Long) : DeleteInvestmentResult()
    data class BusinessError(val message: String) : DeleteInvestmentResult()
}

class DeleteInvestmentUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(investmentId: Long): DeleteInvestmentResult {
        val investment = repository.getInvestment(investmentId)
            ?: return DeleteInvestmentResult.BusinessError("Inversión no encontrada")
        repository.deleteInvestment(investment.id)
        return DeleteInvestmentResult.Success(investmentId)
    }
}
