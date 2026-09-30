package com.appfinanzas.prototype.domain.validation

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import java.time.LocalDate

data class FieldError(
    val field: String,
    val message: String,
)

data class InvestmentForm(
    val type: InvestmentType? = null,
    val institutionId: Long? = null,
    val name: String = "",
    val symbol: String = "",
    val currency: Currency? = null,
    val initialValueText: String = "",
    val dateText: String = "",
)

data class InvestmentEditForm(
    val type: InvestmentType? = null,
    val institutionId: Long? = null,
    val name: String = "",
    val symbol: String = "",
    val currency: Currency? = null,
)

object InvestmentFormValidator {

    const val FIELD_TYPE = "type"
    const val FIELD_INSTITUTION = "institution"
    const val FIELD_NAME = "name"
    const val FIELD_SYMBOL = "symbol"
    const val FIELD_CURRENCY = "currency"
    const val FIELD_INITIAL_VALUE = "initialValue"
    const val FIELD_DATE = "date"

    private const val MAX_AMOUNT = 1_000_000_000.0

    fun validate(form: InvestmentForm): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        if (form.type == null) {
            errors += FieldError(FIELD_TYPE, "Selecciona un tipo de instrumento")
        }
        if (form.institutionId == null) {
            errors += FieldError(FIELD_INSTITUTION, "Selecciona una institución")
        }
        if (form.name.isBlank()) {
            errors += FieldError(FIELD_NAME, "El símbolo / nombre es obligatorio")
        }
        if (form.symbol.isBlank()) {
            errors += FieldError(FIELD_SYMBOL, "El símbolo es obligatorio")
        }
        if (form.currency == null) {
            errors += FieldError(FIELD_CURRENCY, "Selecciona una moneda")
        }
        val initialValue = form.initialValueText.toDoubleOrNull()
        when {
            initialValue == null || initialValue <= 0.0 ->
                errors += FieldError(FIELD_INITIAL_VALUE, "Ingresa un valor inicial mayor a cero")
            initialValue > MAX_AMOUNT ->
                errors += FieldError(FIELD_INITIAL_VALUE, "El valor inicial es demasiado grande")
        }
        val date = parseDate(form.dateText)
        when {
            date == null -> errors += FieldError(FIELD_DATE, "Fecha inválida (dd/mm/aaaa)")
            date.isAfter(LocalDate.now()) -> errors += FieldError(FIELD_DATE, "La fecha no puede ser futura")
        }

        return errors
    }

    fun validateEdit(form: InvestmentEditForm): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        if (form.type == null) {
            errors += FieldError(FIELD_TYPE, "Selecciona un tipo de instrumento")
        }
        if (form.institutionId == null) {
            errors += FieldError(FIELD_INSTITUTION, "Selecciona una institución")
        }
        if (form.name.isBlank()) {
            errors += FieldError(FIELD_NAME, "El nombre es obligatorio")
        }
        if (form.symbol.isBlank()) {
            errors += FieldError(FIELD_SYMBOL, "El símbolo es obligatorio")
        }
        if (form.currency == null) {
            errors += FieldError(FIELD_CURRENCY, "Selecciona una moneda")
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