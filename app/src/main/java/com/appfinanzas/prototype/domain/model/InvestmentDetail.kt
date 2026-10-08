package com.appfinanzas.prototype.domain.model

data class InvestmentDetail(
    val investment: Investment,
    val transactions: List<Transaction>,
    val priceHistory: List<PricePoint> = emptyList(),
)