package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator
import java.time.LocalDate

sealed class AddPricePointResult {
    data class Success(val investmentId: Long) : AddPricePointResult()
    data class Invalid(val message: String) : AddPricePointResult()
    data class BusinessError(val message: String) : AddPricePointResult()
}

class AddPricePointUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(
        investmentId: Long,
        priceText: String,
        dateText: String,
    ): AddPricePointResult {
        val price = priceText.trim().replace(",", "").toDoubleOrNull()
        if (price == null || !price.isFinite() || price <= 0.0) {
            return AddPricePointResult.Invalid("Ingresa un precio mayor a cero")
        }

        val date = if (dateText.isBlank()) {
            LocalDate.now()
        } else {
            TransactionFormValidator.parseDate(dateText)
                ?: return AddPricePointResult.Invalid("Fecha inválida (dd/mm/aaaa)")
        }
        if (date.isAfter(LocalDate.now())) {
            return AddPricePointResult.Invalid("La fecha no puede ser futura")
        }

        return try {
            repository.savePricePoint(
                PricePoint(investmentId = investmentId, date = date, price = price),
            )
            AddPricePointResult.Success(investmentId)
        } catch (e: Exception) {
            AddPricePointResult.BusinessError(e.message ?: "No se pudo guardar el precio")
        }
    }
}
