package com.appfinanzas.prototype.domain.validation

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate

data class TransactionForm(
    val type: TransactionType? = null,
    val dateText: String = "",
    val quantityText: String = "",
    val priceText: String = "",
    val commissionText: String = "",
    val currency: Currency = Currency.MXN,
)

object TransactionFormValidator {

    const val FIELD_TYPE = "type"
    const val FIELD_DATE = "date"
    const val FIELD_QUANTITY = "quantity"
    const val FIELD_PRICE = "price"
    const val FIELD_COMMISSION = "commission"

    fun validate(form: TransactionForm): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        if (form.type == null) {
            errors += FieldError(FIELD_TYPE, "Selecciona un tipo de movimiento")
        }
        val date = parseDate(form.dateText)
        if (date == null) {
            errors += FieldError(FIELD_DATE, "Fecha inválida (dd/mm/aaaa)")
        }

        val requiresQuantity = form.type == TransactionType.COMPRA || form.type == TransactionType.VENTA
        val quantity = form.quantityText.toDoubleOrNull()
        if (requiresQuantity && (quantity == null || quantity <= 0.0)) {
            errors += FieldError(FIELD_QUANTITY, "Ingresa una cantidad mayor a cero")
        }

        val requiresPrice = form.type == TransactionType.COMPRA || form.type == TransactionType.VENTA
        val price = form.priceText.toDoubleOrNull()
        if (requiresPrice && (price == null || price <= 0.0)) {
            errors += FieldError(FIELD_PRICE, "Ingresa un precio mayor a cero")
        }

        val commission = form.commissionText.toDoubleOrNull() ?: 0.0
        if (form.commissionText.isNotBlank() && commission < 0.0) {
            errors += FieldError(FIELD_COMMISSION, "La comisión no puede ser negativa")
        }

        return errors
    }

    fun parseDate(text: String): LocalDate? =
        runCatching {
            val parts = text.trim().split("/")
            require(parts.size == 3)
            LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        }.getOrNull()
}