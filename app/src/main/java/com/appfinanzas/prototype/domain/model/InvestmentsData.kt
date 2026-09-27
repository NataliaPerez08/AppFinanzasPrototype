package com.appfinanzas.prototype.domain.model

data class InvestmentsData(
    val totalValue: Double,
    val totalProfit: Double,
    val investments: List<Investment>,
)