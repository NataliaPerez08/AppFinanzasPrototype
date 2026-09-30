package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.FieldError
import com.appfinanzas.prototype.domain.validation.InvestmentEditForm
import com.appfinanzas.prototype.domain.validation.InvestmentFormValidator
import kotlinx.coroutines.flow.first

sealed class UpdateInvestmentResult {
    data class Success(val investmentId: Long) : UpdateInvestmentResult()
    data class Error(val errors: List<FieldError>) : UpdateInvestmentResult()
    data class BusinessError(val message: String) : UpdateInvestmentResult()
}

class UpdateInvestmentUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(investmentId: Long, form: InvestmentEditForm): UpdateInvestmentResult {
        val errors = InvestmentFormValidator.validateEdit(form)
        if (errors.isNotEmpty()) return UpdateInvestmentResult.Error(errors)

        val investment = repository.getInvestment(investmentId)
            ?: return UpdateInvestmentResult.BusinessError("Inversión no encontrada")
        val institution = repository.observeInstitutions()
            .first()
            .firstOrNull { it.id == form.institutionId }
            ?: return UpdateInvestmentResult.BusinessError("Institución no válida")

        repository.updateInvestment(
            investment.copy(
                name = form.name.trim().uppercase(),
                description = form.name.trim(),
                symbol = form.symbol.trim().uppercase(),
                type = form.type!!,
                institution = institution,
                currency = form.currency!!,
            ),
        )
        return UpdateInvestmentResult.Success(investmentId)
    }
}
