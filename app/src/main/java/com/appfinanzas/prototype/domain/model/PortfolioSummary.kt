package com.appfinanzas.prototype.domain.model

data class PortfolioSummary(
    val baseCurrency: Currency = Currency.MXN,
    val portfolioValue: Double,
    val investedCapital: Double,
    val profit: Double,
    val performance: Double,
    val byCategory: List<CategoryAllocation>,
    val byInstitution: List<NamedAllocation>,
    val byCurrency: List<NamedAllocation>,
    val investmentCount: Int,
)

data class NamedAllocation(
    val label: String,
    val value: Double,
)
