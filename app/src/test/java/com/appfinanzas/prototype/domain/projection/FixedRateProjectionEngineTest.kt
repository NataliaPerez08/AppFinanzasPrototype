package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedRateProjectionEngineTest {

    private val engine = FixedRateProjectionEngine()

    private fun input(
        currentValue: Double,
        annualReturn: Double,
        months: Int = 12,
        contributions: List<Contribution> = emptyList(),
        withdrawals: List<Withdrawal> = emptyList(),
    ) = ProjectionInput(
        currentValue = currentValue,
        horizonMonths = months,
        contributions = contributions,
        withdrawals = withdrawals,
        parameters = ProjectionParameters(annualReturn = annualReturn),
    )

    @Test
    fun `projects simple interest to the horizon`() {
        val result = engine.project(input(currentValue = 100_000.0, annualReturn = 10.0))

        assertEquals(ProjectionStrategy.FIXED_RATE, result.strategy)
        assertEquals(110_000.0, result.projectedValue, 0.01)
        assertEquals(10_000.0, result.expectedGain, 0.01)
    }

    @Test
    fun `simple interest does not compound over multiple years`() {
        val result = engine.project(input(currentValue = 100_000.0, annualReturn = 10.0, months = 24))

        assertEquals(120_000.0, result.projectedValue, 0.01)
    }

    @Test
    fun `contributions accrue simple interest from their month`() {
        val result = engine.project(
            input(
                currentValue = 100_000.0,
                annualReturn = 10.0,
                contributions = listOf(Contribution(monthlyAmount = 1_000.0)),
            ),
        )

        assertEquals(122_550.0, result.projectedValue, 0.01)
        assertEquals(12_000.0, result.contributions, 0.01)
    }

    @Test
    fun `zero rate keeps the value`() {
        val result = engine.project(input(currentValue = 1_000.0, annualReturn = 0.0))

        assertEquals(1_000.0, result.projectedValue, 0.001)
        assertEquals(0.0, result.expectedGain, 0.001)
    }

    @Test
    fun `withdrawals reduce the principal`() {
        val result = engine.project(
            input(
                currentValue = 100_000.0,
                annualReturn = 0.0,
                withdrawals = listOf(Withdrawal(monthlyAmount = 1_000.0)),
            ),
        )

        assertEquals(88_000.0, result.projectedValue, 0.01)
        assertEquals(0.0, result.expectedGain, 0.01)
    }

    @Test
    fun `negative rate stays finite`() {
        val result = engine.project(input(currentValue = 100_000.0, annualReturn = -10.0))

        assertEquals(90_000.0, result.projectedValue, 0.01)
        assertTrue(result.expectedGain.isFinite())
    }
}
