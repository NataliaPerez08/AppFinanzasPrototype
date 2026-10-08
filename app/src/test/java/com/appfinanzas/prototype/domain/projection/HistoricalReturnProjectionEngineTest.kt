package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.priceHistory
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalReturnProjectionEngineTest {

    private val eligibility = HistoricalEligibility(minObservations = 3, minSpanDays = 30)

    @Test
    fun `projects using the observed annualized return`() {
        val history = priceHistory(prices = List(13) { 100.0 + it }, stepDays = 30)
        val stats = HistoricalReturnAnalyzer.analyze(history, eligibility)!!
        val engine = HistoricalReturnProjectionEngine(eligibility)

        val result = engine.project(
            ProjectionInput(
                currentValue = 100_000.0,
                horizonMonths = 12,
                parameters = ProjectionParameters(annualReturn = 5.0, compoundingFrequency = 1),
                history = history,
            ),
        )

        assertEquals(ProjectionStrategy.HISTORICAL_RETURN, result.strategy)
        assertEquals(100_000.0 * (1.0 + stats.annualizedReturn), result.projectedValue, 1.0)
    }

    @Test
    fun `falls back to compound when history is insufficient`() {
        val engine = HistoricalReturnProjectionEngine(eligibility)

        val result = engine.project(
            ProjectionInput(
                currentValue = 100_000.0,
                horizonMonths = 12,
                parameters = ProjectionParameters(annualReturn = 5.0, compoundingFrequency = 1),
                history = emptyList(),
            ),
        )

        assertEquals(ProjectionStrategy.COMPOUND_INTEREST, result.strategy)
        assertEquals(105_000.0, result.projectedValue, 0.01)
        assertTrue(result.expectedGain.isFinite())
    }
}
