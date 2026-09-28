package com.appfinanzas.prototype.domain.model

data class AppSettings(
    val baseCurrency: Currency = Currency.MXN,
    val estimatedInflation: Double = 4.0,
    val estimatedIsr: Double = 2.0,
) {
    companion object {
        const val KEY_BASE_CURRENCY = "base_currency"
        const val KEY_ESTIMATED_INFLATION = "estimated_inflation"
        const val KEY_ESTIMATED_ISR = "estimated_isr"
    }
}