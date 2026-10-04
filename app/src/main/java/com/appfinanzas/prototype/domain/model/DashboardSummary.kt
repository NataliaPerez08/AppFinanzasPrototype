package com.appfinanzas.prototype.domain.model

data class DashboardSummary(
    val baseCurrency: Currency = Currency.MXN,
    val portfolioValue: Double,
    val dailyChange: Double,
    val dailyChangePercentage: Double,
    val investedCapital: Double,
    val profit: Double,
    val performance: Double,
    val allocation: List<CategoryAllocation>,
    val history: List<Float>,
    val investmentCount: Int,
)

data class CategoryAllocation(
    val category: InvestmentCategory,
    val value: Double,
)
