package com.appfinanzas.prototype.domain.model

enum class InvestmentType(val category: InvestmentCategory) {
    ACCION(InvestmentCategory.RENTA_VARIABLE),
    ETF_FONDO(InvestmentCategory.RENTA_VARIABLE),
    FIBRA(InvestmentCategory.RENTA_VARIABLE),
    CETES(InvestmentCategory.RENTA_FIJA),
    SOFIPO(InvestmentCategory.SOFIPO),
    OTRO(InvestmentCategory.OTROS),
}