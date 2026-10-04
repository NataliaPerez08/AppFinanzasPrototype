package com.appfinanzas.prototype.domain.money

import com.appfinanzas.prototype.domain.model.Currency

object CurrencyConverter {
    fun convert(
        amount: Double,
        from: Currency,
        to: Currency,
        usdToMxnRate: Double,
    ): Double {
        require(usdToMxnRate > 0.0 && usdToMxnRate.isFinite()) { "El tipo de cambio debe ser mayor a cero" }
        if (from == to) return amount
        return if (from == Currency.USD) amount * usdToMxnRate else amount / usdToMxnRate
    }
}
