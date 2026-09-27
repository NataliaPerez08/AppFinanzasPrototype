package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.FieldError
import com.appfinanzas.prototype.domain.validation.TransactionForm
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator
import kotlinx.coroutines.flow.first

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

        val investment = repository.observeInvestment(investmentId).first()
            ?: return AddTransactionResult.BusinessError("Inversión no encontrada")

        val transaction = TransactionCalculator.buildTransaction(form, investmentId)

        val businessErrors = validateBusiness(investment, transaction)
        if (businessErrors.isNotEmpty()) return AddTransactionResult.BusinessError(businessErrors.joinToString("\n"))

        repository.saveTransaction(transaction)
        repository.updateInvestment(apply(investment, transaction))
        return AddTransactionResult.Success(investmentId)
    }

    private fun validateBusiness(investment: Investment, transaction: Transaction): List<String> {
        val errors = mutableListOf<String>()
        if (transaction.type == TransactionType.VENTA && transaction.quantity > investment.quantity) {
            errors += "La cantidad excede la posición actual (${investment.quantity})"
        }
        if (transaction.type == TransactionType.RETIRO && transaction.total > investment.investedCapital) {
            errors += "El retiro excede el capital aportado"
        }
        return errors
    }

    private fun apply(investment: Investment, transaction: Transaction): Investment {
        var quantity = investment.quantity
        var capital = investment.investedCapital
        var currentValue = investment.currentValue
        var currentPrice = investment.currentPrice

        when (transaction.type) {
            TransactionType.COMPRA -> {
                quantity += transaction.quantity
                capital += transaction.total
                currentPrice = transaction.price
                currentValue = quantity * transaction.price
            }
            TransactionType.VENTA -> {
                quantity -= transaction.quantity
                capital -= transaction.total
                currentPrice = transaction.price
                currentValue = quantity * transaction.price
            }
            TransactionType.DEPOSITO -> {
                capital += transaction.total
                currentValue += transaction.total
            }
            TransactionType.RETIRO -> {
                capital -= transaction.total
                currentValue -= transaction.total
            }
            TransactionType.COMISION -> {
                capital += transaction.total
            }
            TransactionType.DIVIDENDO,
            TransactionType.INTERES -> Unit
        }

        val performance = if (capital > 0.0) (currentValue - capital) / capital * 100.0 else 0.0
        return investment.copy(
            quantity = quantity,
            investedCapital = capital,
            currentValue = currentValue,
            currentPrice = currentPrice,
            returnPercentage = performance,
        )
    }
}