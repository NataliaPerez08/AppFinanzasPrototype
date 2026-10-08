package com.appfinanzas.prototype.domain.projection

enum class ProjectionScenarioType(val label: String) {
    BASE("Base"),
    PESSIMISTIC("Pesimista"),
    OPTIMISTIC("Optimista"),
    CUSTOM("Personalizado"),
}

data class ProjectionScenario(
    val type: ProjectionScenarioType,
    val expectedReturn: Double,
    val inflation: Double,
    val volatility: Double,
    val isr: Double,
    val monthlyContribution: Double,
) {
    companion object {

        fun base(
            expectedReturn: Double,
            inflation: Double,
            volatility: Double,
            isr: Double,
            monthlyContribution: Double,
        ): ProjectionScenario = ProjectionScenario(
            type = ProjectionScenarioType.BASE,
            expectedReturn = expectedReturn,
            inflation = inflation,
            volatility = volatility,
            isr = isr,
            monthlyContribution = monthlyContribution,
        )

        fun pessimistic(base: ProjectionScenario): ProjectionScenario = base.copy(
            type = ProjectionScenarioType.PESSIMISTIC,
            expectedReturn = base.expectedReturn - 2.0,
            inflation = base.inflation + 0.5,
        )

        fun optimistic(base: ProjectionScenario): ProjectionScenario = base.copy(
            type = ProjectionScenarioType.OPTIMISTIC,
            expectedReturn = base.expectedReturn + 2.0,
            inflation = base.inflation - 0.5,
        )

        fun presets(base: ProjectionScenario): List<ProjectionScenario> = listOf(
            base.copy(type = ProjectionScenarioType.BASE),
            pessimistic(base),
            optimistic(base),
        )
    }
}
