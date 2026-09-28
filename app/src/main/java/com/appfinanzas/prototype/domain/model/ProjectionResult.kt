package com.appfinanzas.prototype.domain.model

enum class ProjectionScenario {
    CONSERVADOR,
    BASE,
    OPTIMISTA,
}

data class ProjectionResult(
    val currentValue: Double,
    val nominal: Double,
    val afterIsr: Double,
    val real: Double,
    val scenarios: List<ScenarioProjection>,
)

data class ScenarioProjection(
    val scenario: ProjectionScenario,
    val expectedReturn: Double,
    val inflation: Double,
    val isr: Double,
    val nominal: Double,
    val afterIsr: Double,
    val real: Double,
)