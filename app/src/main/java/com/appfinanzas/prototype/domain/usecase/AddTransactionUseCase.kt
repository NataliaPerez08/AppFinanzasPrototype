package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.ledger.LedgerCalculator
import com.appfinanzas.prototype.domain.ledger.withLedger
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.FieldError
import com.appfinanzas.prototype.domain.validation.TransactionForm
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator

sealed class AddTransactionResult {
    data class Success(val investmentId: Long) : AddTransactionResult()
    data class Error(val errors: List<FieldError>) : AddTransactionResult()
    data class BusinessError(val message: String) : AddTransactionResult()
}

class AddTransactionUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(investmentId: Long, form: TransactionForm): AddTransactionResult {
        val errors = TransactionFormValidator.validate(form)
        if (errors.isNotEmpty()) return AddTransactionResult.Error(errors)

        val investment = repository.getInvestment(investmentId)
            ?: return AddTransactionResult.BusinessError("Inversión no encontrada")

        val transaction = TransactionCalculator.buildTransaction(form, investmentId)
        val ledger = repository.getTransactions(investmentId) + transaction.copy(id = Long.MAX_VALUE)

        val businessErrors = LedgerCalculator.validate(ledger)
        if (businessErrors.isNotEmpty()) {
            return AddTransactionResult.BusinessError(businessErrors.joinToString("\n"))
        }

        val state = LedgerCalculator.recompute(investment.currentPrice, ledger)
        repository.saveTransactionWithInvestment(transaction, investment.withLedger(state))
        return AddTransactionResult.Success(investmentId)
    }
}
