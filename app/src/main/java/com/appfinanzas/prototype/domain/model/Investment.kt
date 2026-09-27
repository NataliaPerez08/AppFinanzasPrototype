package com.appfinanzas.prototype.domain.model

data class Investment(
    val id: Long,
    val name: String,
    val description: String,
    val symbol: String,
    val type: InvestmentType,
    val institution: Institution,
    val currency: Currency,
    val currentPrice: Double,
    val priceChange: Double,
    val dailyChangePercentage: Double,
    val quantity: Double,
    val investedCapital: Double,
    val currentValue: Double,
    val dailyValueChange: Double,
    val returnPercentage: Double,
    val history: List<Float>,
)