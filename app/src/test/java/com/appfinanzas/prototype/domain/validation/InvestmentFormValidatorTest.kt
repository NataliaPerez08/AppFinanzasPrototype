package com.appfinanzas.prototype.domain.validation

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestmentFormValidatorTest {

    private fun validForm() = InvestmentForm(
        type = InvestmentType.ACCION,
        institutionId = 1,
        name = "Apple",
        symbol = "AAPL",
        currency = Currency.USD,
        initialValueText = "10000",
        dateText = "21/09/2026",
    )

    @Test
    fun `valid form has no errors`() {
        assertTrue(InvestmentFormValidator.validate(validForm()).isEmpty())
    }

    @Test
    fun `missing type is an error`() {
        val errors = InvestmentFormValidator.validate(validForm().copy(type = null))
        assertTrue(errors.any { it.field == InvestmentFormValidator.FIELD_TYPE })
    }

    @Test
    fun `missing institution is an error`() {
        val errors = InvestmentFormValidator.validate(validForm().copy(institutionId = null))
        assertTrue(errors.any { it.field == InvestmentFormValidator.FIELD_INSTITUTION })
    }

    @Test
    fun `blank name and symbol are errors`() {
        val errors = InvestmentFormValidator.validate(validForm().copy(name = "  ", symbol = ""))
        assertTrue(errors.any { it.field == InvestmentFormValidator.FIELD_NAME })
        assertTrue(errors.any { it.field == InvestmentFormValidator.FIELD_SYMBOL })
    }

    @Test
    fun `non positive initial value is an error`() {
        val zero = InvestmentFormValidator.validate(validForm().copy(initialValueText = "0"))
        val invalid = InvestmentFormValidator.validate(validForm().copy(initialValueText = "abc"))
        assertTrue(zero.any { it.field == InvestmentFormValidator.FIELD_INITIAL_VALUE })
        assertTrue(invalid.any { it.field == InvestmentFormValidator.FIELD_INITIAL_VALUE })
    }

    @Test
    fun `invalid date is an error`() {
        val errors = InvestmentFormValidator.validate(validForm().copy(dateText = "31/13/2026"))
        assertTrue(errors.any { it.field == InvestmentFormValidator.FIELD_DATE })
    }

    @Test
    fun `parses valid date`() {
        val date = InvestmentFormValidator.parseDate("21/09/2026")
        assertEquals(2026, date!!.year)
        assertEquals(9, date.monthValue)
        assertEquals(21, date.dayOfMonth)
    }

    @Test
    fun `empty form reports all required fields`() {
        val errors = InvestmentFormValidator.validate(InvestmentForm())
        val fields = errors.map { it.field }
        assertTrue(InvestmentFormValidator.FIELD_TYPE in fields)
        assertTrue(InvestmentFormValidator.FIELD_INSTITUTION in fields)
        assertTrue(InvestmentFormValidator.FIELD_NAME in fields)
        assertTrue(InvestmentFormValidator.FIELD_CURRENCY in fields)
        assertTrue(InvestmentFormValidator.FIELD_INITIAL_VALUE in fields)
    }
}