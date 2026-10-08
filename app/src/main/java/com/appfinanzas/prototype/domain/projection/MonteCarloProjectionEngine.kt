package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

class MonteCarloProjectionEngine(
    val simulations: Int = 10_000,
    private val seed: Long = DEFAULT_SEED,
) : ProjectionEngine {

    override val strategy: ProjectionStrategy = ProjectionStrategy.MONTE_CARLO

    override fun project(input: ProjectionInput): ProjectionResult {
        val random = Random(seed)
        val finals = ArrayList<Double>(simulations)
        repeat(simulations) { finals += simulateFinal(input, random) }
        return distributionOf(finals, input)
    }

    fun simulateFinal(input: ProjectionInput, random: Random): Double = simulatePath(input, random).last()

    fun simulatePath(input: ProjectionInput, random: Random): DoubleArray {
        val months = input.horizonMonths.coerceAtLeast(0)
        val parameters = input.parameters
        val annualReturn = parameters.annualReturn.finiteOrZero() / 100.0
        val path = DoubleArray(months + 1)
        if (1.0 + annualReturn <= 0.0) return path

        val monthlyLogMean = ln(1.0 + annualReturn) / 12.0
        val monthlyLogSd = (parameters.annualVolatility.finiteOrZero() / 100.0) / sqrt(12.0)

        var value = input.currentValue.finiteOrZero()
        path[0] = value
        for (month in 0 until months) {
            val shock = if (monthlyLogSd > 0.0) monthlyLogMean + monthlyLogSd * random.nextGaussian() else monthlyLogMean
            value = value * exp(shock)
            val (contribution, withdrawal) = cashFlowAt(month, input.contributions, input.withdrawals)
            value += contribution - withdrawal
            path[month + 1] = value.finiteOrZero()
        }
        return path
    }

    private fun distributionOf(finals: List<Double>, input: ProjectionInput): ProjectionResult {
        val distribution = ProjectionDistribution.of(finals)
        val (contributed, withdrawn) = totalCashFlow(
            input.horizonMonths,
            input.contributions,
            input.withdrawals,
        )
        return ProjectionResult(
            currentValue = input.currentValue.finiteOrZero(),
            projectedValue = distribution.p50,
            expectedGain = (distribution.p50 - input.currentValue - contributed + withdrawn).finiteOrZero(),
            contributions = contributed.finiteOrZero(),
            withdrawals = withdrawn.finiteOrZero(),
            strategy = strategy,
            p10 = distribution.p10,
            p25 = distribution.p25,
            p50 = distribution.p50,
            p75 = distribution.p75,
            p90 = distribution.p90,
            mean = distribution.mean,
        )
    }

    companion object {
        // ponytail: fixed seed keeps projections reproducible; expose per-call seed when scenarios need variation
        const val DEFAULT_SEED = 20240101L
    }
}

private fun Random.nextGaussian(): Double {
    val u1 = nextDouble().coerceAtLeast(1e-12)
    val u2 = nextDouble()
    return sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
}
