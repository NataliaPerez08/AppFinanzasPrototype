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

    private const val MAX_AMOUNT = 1_000_000_000.0

    fun validate(form: TransactionForm): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        if (form.type == null) {
            errors += FieldError(FIELD_TYPE, "Selecciona un tipo de movimiento")
        }
        val date = parseDate(form.dateText)
        when {
            date == null -> errors += FieldError(FIELD_DATE, "Fecha inválida (dd/mm/aaaa)")
            date.isAfter(LocalDate.now()) -> errors += FieldError(FIELD_DATE, "La fecha no puede ser futura")
        }

        val requiresQuantity = form.type == TransactionType.COMPRA || form.type == TransactionType.VENTA
        val quantity = form.quantityText.toDoubleOrNull()
        if (requiresQuantity) {
            when {
                quantity == null || quantity <= 0.0 -> errors += FieldError(FIELD_QUANTITY, "Ingresa una cantidad mayor a cero")
                quantity > MAX_AMOUNT -> errors += FieldError(FIELD_QUANTITY, "La cantidad es demasiado grande")
            }
        } else if (form.type != null) {
            when {
                quantity == null || quantity <= 0.0 -> errors += FieldError(FIELD_QUANTITY, "Ingresa un monto mayor a cero")
                quantity > MAX_AMOUNT -> errors += FieldError(FIELD_QUANTITY, "El monto es demasiado grande")
            }
        }

        if (requiresQuantity) {
            val price = form.priceText.toDoubleOrNull()
            when {
                price == null || price <= 0.0 -> errors += FieldError(FIELD_PRICE, "Ingresa un precio mayor a cero")
                price > MAX_AMOUNT -> errors += FieldError(FIELD_PRICE, "El precio es demasiado grande")
            }
        }

        val commission = form.commissionText.toDoubleOrNull() ?: 0.0
        when {
            form.commissionText.isNotBlank() && commission < 0.0 ->
                errors += FieldError(FIELD_COMMISSION, "La comisión no puede ser negativa")
            commission > MAX_AMOUNT ->
                errors += FieldError(FIELD_COMMISSION, "La comisión es demasiado grande")
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