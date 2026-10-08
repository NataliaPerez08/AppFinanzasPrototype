package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.model.ProjectionStrategy

const val DEFAULT_ANNUAL_VOLATILITY = 15.0

data class Contribution(
    val monthlyAmount: Double,
    val startMonth: Int = 0,
)

data class Withdrawal(
    val monthlyAmount: Double,
    val startMonth: Int = 0,
)

data class ProjectionParameters(
    val annualReturn: Double = 0.0,
    val annualVolatility: Double = 0.0,
    val inflation: Double = 0.0,
    val isr: Double = 0.0,
    val compoundingFrequency: Int = 1,
)

data class ProjectionInput(
    val currentValue: Double,
    val horizonMonths: Int,
    val contributions: List<Contribution> = emptyList(),
    val withdrawals: List<Withdrawal> = emptyList(),
    val parameters: ProjectionParameters,
    val history: List<PricePoint> = emptyList(),
)

data class ProjectionResult(
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val contributions: Double,
    val withdrawals: Double,
    val strategy: ProjectionStrategy,
    val p10: Double? = null,
    val p25: Double? = null,
    val p50: Double? = null,
    val p75: Double? = null,
    val p90: Double? = null,
    val mean: Double? = null,
)

interface ProjectionEngine {
    val strategy: ProjectionStrategy
    fun project(input: ProjectionInput): ProjectionResult
}

internal fun Double.finiteOrZero(): Double = if (isFinite()) this else 0.0

internal fun cashFlowAt(
    month: Int,
    contributions: List<Contribution>,
    withdrawals: List<Withdrawal>,
): Pair<Double, Double> {
    val contribution = contributions.filter { it.startMonth <= month }.sumOf { it.monthlyAmount.finiteOrZero() }
    val withdrawal = withdrawals.filter { it.startMonth <= month }.sumOf { it.monthlyAmount.finiteOrZero() }
    return contribution to withdrawal
}

internal fun totalCashFlow(
    months: Int,
    contributions: List<Contribution>,
    withdrawals: List<Withdrawal>,
): Pair<Double, Double> {
    var contributed = 0.0
    var withdrawn = 0.0
    for (month in 0 until months.coerceAtLeast(0)) {
        val (contribution, withdrawal) = cashFlowAt(month, contributions, withdrawals)
        contributed += contribution
        withdrawn += withdrawal
    }
    return contributed to withdrawn
}
