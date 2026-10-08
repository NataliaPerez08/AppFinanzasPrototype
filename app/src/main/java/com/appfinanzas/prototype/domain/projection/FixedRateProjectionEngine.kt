package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy

class FixedRateProjectionEngine : ProjectionEngine {

    override val strategy: ProjectionStrategy = ProjectionStrategy.FIXED_RATE

    override fun project(input: ProjectionInput): ProjectionResult {
        val months = input.horizonMonths.coerceAtLeast(0)
        val annualReturn = input.parameters.annualReturn.finiteOrZero() / 100.0
        val monthlyRate = annualReturn / 12.0

        val principal = input.currentValue.finiteOrZero()
        var interestBase = principal
        var interest = 0.0
        var contributed = 0.0
        var withdrawn = 0.0

        for (month in 0 until months) {
            interest += interestBase * monthlyRate
            val (contribution, withdrawal) = cashFlowAt(month, input.contributions, input.withdrawals)
            interestBase += contribution - withdrawal
            contributed += contribution
            withdrawn += withdrawal
        }

        val projected = (principal + contributed - withdrawn + interest).finiteOrZero()
        return ProjectionResult(
            currentValue = principal,
            projectedValue = projected,
            expectedGain = interest.finiteOrZero(),
            contributions = contributed.finiteOrZero(),
            withdrawals = withdrawn.finiteOrZero(),
            strategy = strategy,
        )
    }
}
