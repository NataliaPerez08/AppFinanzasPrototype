package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import kotlin.math.pow

class CompoundInterestProjectionEngine : ProjectionEngine {

    override val strategy: ProjectionStrategy = ProjectionStrategy.COMPOUND_INTEREST

    override fun project(input: ProjectionInput): ProjectionResult {
        val months = input.horizonMonths.coerceAtLeast(0)
        val parameters = input.parameters
        val annualReturn = parameters.annualReturn.finiteOrZero() / 100.0
        val periods = parameters.compoundingFrequency.coerceAtLeast(1)
        val base = (1.0 + annualReturn / periods).coerceAtLeast(0.0)
        val monthlyRate = if (base == 0.0) -1.0 else base.pow(periods / 12.0) - 1.0

        val principal = input.currentValue.finiteOrZero()
        var value = principal
        var contributed = 0.0
        var withdrawn = 0.0

        for (month in 0 until months) {
            val (contribution, withdrawal) = cashFlowAt(month, input.contributions, input.withdrawals)
            value = value * (1.0 + monthlyRate) + contribution - withdrawal
            contributed += contribution
            withdrawn += withdrawal
        }

        value = value.finiteOrZero()
        return ProjectionResult(
            currentValue = principal,
            projectedValue = value,
            expectedGain = (value - principal - contributed + withdrawn).finiteOrZero(),
            contributions = contributed.finiteOrZero(),
            withdrawals = withdrawn.finiteOrZero(),
            strategy = strategy,
        )
    }
}
