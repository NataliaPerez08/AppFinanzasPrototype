package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy

class HistoricalReturnProjectionEngine(
    private val eligibility: HistoricalEligibility = HistoricalEligibility(),
    private val compound: CompoundInterestProjectionEngine = CompoundInterestProjectionEngine(),
) : ProjectionEngine {

    override val strategy: ProjectionStrategy = ProjectionStrategy.HISTORICAL_RETURN

    override fun project(input: ProjectionInput): ProjectionResult {
        val stats = HistoricalReturnAnalyzer.analyze(input.history, eligibility)
            ?: return compound.project(input)

        val parameters = input.parameters.copy(
            annualReturn = stats.annualizedReturn * 100.0,
            annualVolatility = stats.annualVolatility * 100.0,
        )
        return compound.project(input.copy(parameters = parameters)).copy(strategy = strategy)
    }
}
