package com.appfinanzas.prototype.ui.format

import com.appfinanzas.prototype.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `formats mxn amount with currency code`() {
        assertEquals("$1,245,320.00 MXN", MoneyFormatter.format(1_245_320.0))
    }

    @Test
    fun `formats decimals with two places`() {
        assertEquals("$1,842.30 MXN", MoneyFormatter.format(1_842.30))
    }

    @Test
    fun `formats usd amount with currency code`() {
        assertEquals("$865.36 USD", MoneyFormatter.format(865.36, Currency.USD))
    }

    @Test
    fun `formats zero`() {
        assertEquals("$0.00 MXN", MoneyFormatter.format(0.0))
    }

    @Test
    fun `formats negative amount`() {
        assertEquals("-$100.00 MXN", MoneyFormatter.format(-100.0))
    }
}