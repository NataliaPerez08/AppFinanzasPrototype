package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.ProjectionStrategy

object ProjectionStrategyResolver {

    fun resolve(type: InvestmentType): ProjectionStrategy = when (type) {
        InvestmentType.CETES -> ProjectionStrategy.FIXED_RATE
        InvestmentType.SOFIPO -> ProjectionStrategy.COMPOUND_INTEREST
        InvestmentType.ACCION,
        InvestmentType.ETF_FONDO,
        InvestmentType.FIBRA,
        -> ProjectionStrategy.MONTE_CARLO
        InvestmentType.OTRO -> ProjectionStrategy.HISTORICAL_RETURN
    }
}
