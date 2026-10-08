package com.appfinanzas.prototype.domain.model

enum class ProjectionStrategy(val label: String) {
    FIXED_RATE("Tasa fija"),
    COMPOUND_INTEREST("Interés compuesto"),
    HISTORICAL_RETURN("Histórico"),
    MONTE_CARLO("Monte Carlo"),
    MANUAL("Manual"),
}
