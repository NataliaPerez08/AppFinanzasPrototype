package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.ledger.LedgerCalculator
import com.appfinanzas.prototype.domain.ledger.withLedger
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.FieldError
import com.appfinanzas.prototype.domain.validation.TransactionForm
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator

sealed class UpdateTransactionResult {
    data class Success(val investmentId: Long) : UpdateTransactionResult()
    data class Error(val errors: List<FieldError>) : UpdateTransactionResult()
    data class BusinessError(val message: String) : UpdateTransactionResult()
}

class UpdateTransactionUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(
        investmentId: Long,
        transactionId: Long,
        form: TransactionForm,
    ): UpdateTransactionResult {
        val errors = TransactionFormValidator.validate(form)
        if (errors.isNotEmpty()) return UpdateTransactionResult.Error(errors)

        val investment = repository.getInvestment(investmentId)
            ?: return UpdateTransactionResult.BusinessError("Inversión no encontrada")

        val existing = repository.getTransactions(investmentId)
        if (existing.none { it.id == transactionId }) {
            return UpdateTransactionResult.BusinessError("Movimiento no encontrado")
        }

        val updated = TransactionCalculator.buildTransaction(form, investmentId).copy(id = transactionId)
        val ledger = existing.map { if (it.id == transactionId) updated else it }

        val businessErrors = LedgerCalculator.validate(ledger)
        if (businessErrors.isNotEmpty()) {
            return UpdateTransactionResult.BusinessError(businessErrors.joinToString("\n"))
        }

        val state = LedgerCalculator.recompute(investment.currentPrice, ledger)
        repository.updateTransactionWithInvestment(updated, investment.withLedger(state))
        return UpdateTransactionResult.Success(investmentId)
    }
}
