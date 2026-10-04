package com.appfinanzas.prototype.domain.model

data class InvestmentsData(
    val baseCurrency: Currency = Currency.MXN,
    val totalValue: Double,
    val totalProfit: Double,
    val investments: List<Investment>,
)
