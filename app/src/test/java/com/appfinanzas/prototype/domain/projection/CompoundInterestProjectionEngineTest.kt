package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class CompoundInterestProjectionEngineTest {

    private val engine = CompoundInterestProjectionEngine()

    private fun input(
        currentValue: Double,
        annualReturn: Double,
        months: Int = 12,
        compoundingFrequency: Int = 1,
        contributions: List<Contribution> = emptyList(),
        withdrawals: List<Withdrawal> = emptyList(),
    ) = ProjectionInput(
        currentValue = currentValue,
        horizonMonths = months,
        contributions = contributions,
        withdrawals = withdrawals,
        parameters = ProjectionParameters(
            annualReturn = annualReturn,
            compoundingFrequency = compoundingFrequency,
        ),
    )

    @Test
    fun `projects principal at an annual compounding rate`() {
        val result = engine.project(input(currentValue = 100_000.0, annualReturn = 10.0))

        assertEquals(ProjectionStrategy.COMPOUND_INTEREST, result.strategy)
        assertEquals(110_000.0, result.projectedValue, 0.01)
        assertEquals(10_000.0, result.expectedGain, 0.01)
    }

    @Test
    fun `monthly compounding applies the periodic rate each month`() {
        val result = engine.project(
            input(currentValue = 100_000.0, annualReturn = 10.0, compoundingFrequency = 12),
        )

        assertEquals(100_000.0 * (1.0 + 0.10 / 12.0).pow(12), result.projectedValue, 0.01)
    }

    @Test
    fun `adds monthly contributions without counting them as return`() {
        val result = engine.project(
            input(
                currentValue = 100_000.0,
                annualReturn = 0.0,
                contributions = listOf(Contribution(monthlyAmount = 1_000.0)),
            ),
        )

        assertEquals(112_000.0, result.projectedValue, 0.01)
        assertEquals(12_000.0, result.contributions, 0.01)
        assertEquals(0.0, result.expectedGain, 0.01)
    }

    @Test
    fun `withdrawals reduce the projected value`() {
        val result = engine.project(
            input(
                currentValue = 100_000.0,
                annualReturn = 0.0,
                withdrawals = listOf(Withdrawal(monthlyAmount = 1_000.0)),
            ),
        )

        assertEquals(88_000.0, result.projectedValue, 0.01)
        assertEquals(12_000.0, result.withdrawals, 0.01)
    }

    @Test
    fun `zero rate keeps the value`() {
        val result = engine.project(input(currentValue = 1_000.0, annualReturn = 0.0))

        assertEquals(1_000.0, result.projectedValue, 0.001)
        assertEquals(0.0, result.expectedGain, 0.001)
    }

    @Test
    fun `negative rate reduces the value`() {
        val result = engine.project(input(currentValue = 100_000.0, annualReturn = -10.0))

        assertEquals(90_000.0, result.projectedValue, 0.01)
    }

    @Test
    fun `supports horizons shorter and longer than a year`() {
        val sixMonths = engine.project(input(currentValue = 100_000.0, annualReturn = 10.0, months = 6))
        val twoYears = engine.project(input(currentValue = 100_000.0, annualReturn = 10.0, months = 24))

        assertEquals(100_000.0 * 1.10.pow(0.5), sixMonths.projectedValue, 0.01)
        assertEquals(121_000.0, twoYears.projectedValue, 0.01)
    }

    @Test
    fun `non finite inputs stay finite`() {
        val result = engine.project(input(currentValue = 1_000.0, annualReturn = -500.0))

        assertTrue(result.projectedValue.isFinite())
        assertTrue(result.expectedGain.isFinite())
    }
}
