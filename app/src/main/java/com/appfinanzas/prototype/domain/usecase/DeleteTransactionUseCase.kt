package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.ledger.LedgerCalculator
import com.appfinanzas.prototype.domain.ledger.withLedger
import com.appfinanzas.prototype.domain.repository.InvestmentRepository

sealed class DeleteTransactionResult {
    data class Success(val investmentId: Long) : DeleteTransactionResult()
    data class BusinessError(val message: String) : DeleteTransactionResult()
}

class DeleteTransactionUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(investmentId: Long, transactionId: Long): DeleteTransactionResult {
        val investment = repository.getInvestment(investmentId)
            ?: return DeleteTransactionResult.BusinessError("Inversión no encontrada")

        val existing = repository.getTransactions(investmentId)
        if (existing.none { it.id == transactionId }) {
            return DeleteTransactionResult.BusinessError("Movimiento no encontrado")
        }

        val ledger = existing.filterNot { it.id == transactionId }
        val businessErrors = LedgerCalculator.validate(ledger)
        if (businessErrors.isNotEmpty()) {
            return DeleteTransactionResult.BusinessError(businessErrors.joinToString("\n"))
        }

        val state = LedgerCalculator.recompute(investment.currentPrice, ledger)
        repository.deleteTransactionWithInvestment(transactionId, investment.withLedger(state))
        return DeleteTransactionResult.Success(investmentId)
    }
}
