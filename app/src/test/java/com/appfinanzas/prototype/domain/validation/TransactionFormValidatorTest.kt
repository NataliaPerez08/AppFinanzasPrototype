package com.appfinanzas.prototype.domain.validation

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.TransactionType
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFormValidatorTest {

    private fun validCompra() = TransactionForm(
        type = TransactionType.COMPRA,
        dateText = "21/09/2026",
        quantityText = "2",
        priceText = "100",
        commissionText = "1",
        currency = Currency.MXN,
    )

    @Test
    fun `valid compra has no errors`() {
        assertTrue(TransactionFormValidator.validate(validCompra()).isEmpty())
    }

    @Test
    fun `missing type is an error`() {
        val errors = TransactionFormValidator.validate(validCompra().copy(type = null))
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_TYPE })
    }

    @Test
    fun `invalid date is an error`() {
        val errors = TransactionFormValidator.validate(validCompra().copy(dateText = "xx"))
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_DATE })
    }

    @Test
    fun `compra requires positive quantity and price`() {
        val errors = TransactionFormValidator.validate(
            validCompra().copy(quantityText = "0", priceText = "-5"),
        )
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_QUANTITY })
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_PRICE })
    }

    @Test
    fun `negative commission is an error`() {
        val errors = TransactionFormValidator.validate(validCompra().copy(commissionText = "-1"))
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_COMMISSION })
    }

    @Test
    fun `dividendo requires a positive amount`() {
        val blank = TransactionForm(
            type = TransactionType.DIVIDENDO,
            dateText = "21/09/2026",
            quantityText = "",
            priceText = "",
            commissionText = "",
            currency = Currency.MXN,
        )
        assertTrue(TransactionFormValidator.validate(blank).any { it.field == TransactionFormValidator.FIELD_QUANTITY })

        val filled = blank.copy(quantityText = "50")
        assertTrue(TransactionFormValidator.validate(filled).isEmpty())
    }

    @Test
    fun `deposito does not require price`() {
        val form = TransactionForm(
            type = TransactionType.DEPOSITO,
            dateText = "21/09/2026",
            quantityText = "1000",
            priceText = "",
            commissionText = "",
            currency = Currency.MXN,
        )
        assertTrue(TransactionFormValidator.validate(form).isEmpty())
    }

    @Test
    fun `future date is an error`() {
        val future = java.time.LocalDate.now().plusDays(1)
        val text = "%02d/%02d/%04d".format(future.dayOfMonth, future.monthValue, future.year)
        val errors = TransactionFormValidator.validate(validCompra().copy(dateText = text))
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_DATE })
    }

    @Test
    fun `extremely large amount is an error`() {
        val errors = TransactionFormValidator.validate(validCompra().copy(quantityText = "99999999999"))
        assertTrue(errors.any { it.field == TransactionFormValidator.FIELD_QUANTITY })
    }
}