package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.validation.TransactionForm
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator

object TransactionCalculator {

    fun computeTotal(form: TransactionForm): Double = when (form.type) {
        TransactionType.COMPRA,
        TransactionType.VENTA -> {
            val quantity = form.quantityText.toDoubleOrNull() ?: 0.0
            val price = form.priceText.toDoubleOrNull() ?: 0.0
            val commission = form.commissionText.toDoubleOrNull() ?: 0.0
            quantity * price + commission
        }
        else -> form.quantityText.toDoubleOrNull() ?: 0.0
    }

    fun buildTransaction(form: TransactionForm, investmentId: Long): Transaction =
        Transaction(
            id = 0,
            investmentId = investmentId,
            type = form.type ?: error("Tipo de movimiento requerido"),
            date = TransactionFormValidator.parseDate(form.dateText) ?: error("Fecha inválida"),
            quantity = form.quantityText.toDoubleOrNull() ?: 0.0,
            price = form.priceText.toDoubleOrNull() ?: 0.0,
            commission = form.commissionText.toDoubleOrNull() ?: 0.0,
            total = computeTotal(form),
            currency = form.currency,
        )
}