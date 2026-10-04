package com.appfinanzas.prototype.domain.money

import com.appfinanzas.prototype.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyConverterTest {
    @Test
    fun convertsUsdToMxnUsingConfiguredRate() {
        assertEquals(
            4_000.0,
            CurrencyConverter.convert(200.0, Currency.USD, Currency.MXN, usdToMxnRate = 20.0),
            0.001,
        )
    }

    @Test
    fun convertsMxnToUsdUsingConfiguredRate() {
        assertEquals(
            200.0,
            CurrencyConverter.convert(4_000.0, Currency.MXN, Currency.USD, usdToMxnRate = 20.0),
            0.001,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveRate() {
        CurrencyConverter.convert(1.0, Currency.USD, Currency.MXN, usdToMxnRate = 0.0)
    }
}
