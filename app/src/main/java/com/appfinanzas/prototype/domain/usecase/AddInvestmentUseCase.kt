package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.FieldError
import com.appfinanzas.prototype.domain.validation.InvestmentForm
import com.appfinanzas.prototype.domain.validation.InvestmentFormValidator
import kotlinx.coroutines.flow.first

sealed class AddInvestmentResult {
    data class Success(val investmentId: Long) : AddInvestmentResult()
    data class Error(val errors: List<FieldError>) : AddInvestmentResult()
    data class BusinessError(val message: String) : AddInvestmentResult()
}

class AddInvestmentUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(form: InvestmentForm): AddInvestmentResult {
        val errors = InvestmentFormValidator.validate(form)
        if (errors.isNotEmpty()) return AddInvestmentResult.Error(errors)

        val initialValue = form.initialValueText.toDoubleOrNull() ?: 0.0
        val institution = repository.observeInstitutions()
            .first()
            .firstOrNull { it.id == form.institutionId }
            ?: return AddInvestmentResult.BusinessError("Institución no válida")
        val date = InvestmentFormValidator.parseDate(form.dateText) ?: return AddInvestmentResult.BusinessError("Fecha inválida")

        val investment = Investment(
            id = 0,
            name = form.name.trim().uppercase(),
            description = form.name.trim(),
            symbol = form.symbol.trim().uppercase(),
            type = form.type!!,
            institution = institution,
            currency = form.currency!!,
            currentPrice = initialValue,
            priceChange = 0.0,
            dailyChangePercentage = 0.0,
            quantity = 1.0,
            investedCapital = initialValue,
            currentValue = initialValue,
            dailyValueChange = 0.0,
            returnPercentage = 0.0,
            history = listOf(.5f, .5f),
        )
        val id = repository.saveInvestment(investment)
        repository.saveTransaction(
            Transaction(
                id = 0,
                investmentId = id,
                type = TransactionType.DEPOSITO,
                date = date,
                quantity = 0.0,
                price = 0.0,
                commission = 0.0,
                total = initialValue,
                currency = form.currency!!,
            ),
        )
        return AddInvestmentResult.Success(id)
    }
}