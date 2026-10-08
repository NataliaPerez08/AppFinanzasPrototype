package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonteCarloProjectionEngineTest {

    private fun input(
        currentValue: Double,
        annualReturn: Double,
        volatility: Double = 15.0,
        months: Int = 12,
        contributions: List<Contribution> = emptyList(),
        withdrawals: List<Withdrawal> = emptyList(),
    ) = ProjectionInput(
        currentValue = currentValue,
        horizonMonths = months,
        contributions = contributions,
        withdrawals = withdrawals,
        parameters = ProjectionParameters(
            annualReturn = annualReturn,
            annualVolatility = volatility,
            compoundingFrequency = 1,
        ),
    )

    @Test
    fun `zero volatility collapses to the deterministic projection`() {
        val result = MonteCarloProjectionEngine(simulations = 2_000)
            .project(input(currentValue = 100_000.0, annualReturn = 10.0, volatility = 0.0))

        assertEquals(ProjectionStrategy.MONTE_CARLO, result.strategy)
        assertEquals(110_000.0, result.projectedValue, 0.01)
        assertEquals(110_000.0, result.p10!!, 0.01)
        assertEquals(110_000.0, result.p90!!, 0.01)
        assertEquals(10_000.0, result.expectedGain, 0.01)
    }

    @Test
    fun `percentiles are ordered`() {
        val result = MonteCarloProjectionEngine().project(input(currentValue = 100_000.0, annualReturn = 8.0))

        assertTrue(result.p10!! <= result.p25!!)
        assertTrue(result.p25!! <= result.p50!!)
        assertTrue(result.p50!! <= result.p75!!)
        assertTrue(result.p75!! <= result.p90!!)
    }

    @Test
    fun `mean sits above the median for positive volatility`() {
        val result = MonteCarloProjectionEngine().project(input(currentValue = 100_000.0, annualReturn = 8.0))

        assertTrue(result.mean!! >= result.p50!!)
    }

    @Test
    fun `higher volatility widens the range`() {
        val calibrated = MonteCarloProjectionEngine()
        val low = calibrated.project(input(currentValue = 100_000.0, annualReturn = 8.0, volatility = 5.0))
        val high = calibrated.project(input(currentValue = 100_000.0, annualReturn = 8.0, volatility = 40.0))

        assertTrue((high.p90!! - high.p10!!) > (low.p90!! - low.p10!!))
    }

    @Test
    fun `is reproducible with a fixed seed`() {
        val first = MonteCarloProjectionEngine(seed = 7L).project(input(currentValue = 1_000.0, annualReturn = 8.0))
        val second = MonteCarloProjectionEngine(seed = 7L).project(input(currentValue = 1_000.0, annualReturn = 8.0))

        assertEquals(first.p50!!, second.p50!!, 0.0)
        assertEquals(first.p90!!, second.p90!!, 0.0)
    }

    @Test
    fun `tracks contributions without counting them as return`() {
        val result = MonteCarloProjectionEngine(simulations = 1_000).project(
            input(
                currentValue = 100_000.0,
                annualReturn = 0.0,
                volatility = 0.0,
                contributions = listOf(Contribution(monthlyAmount = 1_000.0)),
            ),
        )

        assertEquals(112_000.0, result.projectedValue, 0.01)
        assertEquals(12_000.0, result.contributions, 0.01)
        assertEquals(0.0, result.expectedGain, 0.01)
    }

    @Test
    fun `non finite growth inputs stay finite`() {
        val result = MonteCarloProjectionEngine(simulations = 500)
            .project(input(currentValue = 1_000.0, annualReturn = -100.0, volatility = 30.0))

        assertTrue(result.projectedValue.isFinite())
        assertTrue(result.mean!!.isFinite())
    }

    @Test
    fun `path matches deterministic monthly growth at zero volatility`() {
        val engine = MonteCarloProjectionEngine(simulations = 1)

        val path = engine.simulatePath(
            input(currentValue = 1_000.0, annualReturn = 10.0, volatility = 0.0),
            kotlin.random.Random(1),
        )

        assertEquals(13, path.size)
        assertEquals(1_000.0, path[0], 0.001)
        assertEquals(1_100.0, path[12], 0.01)
    }
}
