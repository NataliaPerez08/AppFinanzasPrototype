package com.appfinanzas.prototype.ui.format

import com.appfinanzas.prototype.domain.model.Currency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency as JavaCurrency

object MoneyFormatter {
    private const val PATTERN = "$#,##0.00"

    fun format(amount: Double, currency: Currency = Currency.MXN): String =
        formatter(currency).format(amount) + " " + currency.code

    fun format(amount: BigDecimal, currency: Currency = Currency.MXN): String =
        format(amount.toDouble(), currency)

    private fun formatter(currency: Currency): DecimalFormat =
        DecimalFormat(PATTERN).apply {
            this.currency = JavaCurrency.getInstance(currency.code)
        }
}