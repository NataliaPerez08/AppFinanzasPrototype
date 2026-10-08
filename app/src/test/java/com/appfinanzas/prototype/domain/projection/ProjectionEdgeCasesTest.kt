package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.priceHistory
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectionEdgeCasesTest {

    private val engine = PortfolioProjectionEngine(
        monteCarloEngine = MonteCarloProjectionEngine(simulations = 500),
    )
    private val settings = AppSettings()
    private val gbm = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa")

    @Test
    fun `empty portfolio produces zeroes and a degenerate band`() {
        val projection = engine.project(emptyList(), settings, 12, ProjectionParameters(annualReturn = 10.0))
        val series = engine.projectSeries(emptyList(), settings, 12, ProjectionParameters(annualReturn = 10.0))

        assertEquals(0.0, projection.projectedValue, 0.001)
        assertTrue(projection.assets.isEmpty())
        assertFalse(series.hasProbability)
        assertEquals(13, series.months.size)
        assertEquals(0.0, series.final.p50, 0.001)
        assertNull(engine.projectDistribution(emptyList(), settings, 12, ProjectionParameters(annualReturn = 10.0)))
    }

    @Test
    fun `single investment and zero value stay finite`() {
        val investments = listOf(
            sampleInvestment(id = 1, type = InvestmentType.CETES, currentValue = 0.0, institution = gbm),
        )

        val projection = engine.project(investments, settings, 12, ProjectionParameters(annualReturn = 10.0))

        assertEquals(1, projection.assets.size)
        assertEquals(0.0, projection.projectedValue, 0.001)
        assertTrue(projection.expectedGain.isFinite())
    }

    @Test
    fun `zero and negative returns stay finite`() {
        val zero = CompoundInterestProjectionEngine().project(
            ProjectionInput(10_000.0, 24, parameters = ProjectionParameters(annualReturn = 0.0)),
        )
        val negative = CompoundInterestProjectionEngine().project(
            ProjectionInput(10_000.0, 24, parameters = ProjectionParameters(annualReturn = -15.0)),
        )

        assertEquals(10_000.0, zero.projectedValue, 0.01)
        assertTrue(negative.projectedValue < 10_000.0)
        assertTrue(negative.projectedValue.isFinite())
    }

    @Test
    fun `very high volatility keeps percentiles finite and ordered`() {
        val result = MonteCarloProjectionEngine(simulations = 500).project(
            ProjectionInput(
                100_000.0,
                12,
                parameters = ProjectionParameters(annualReturn = 8.0, annualVolatility = 300.0),
            ),
        )

        assertTrue(result.p10!!.isFinite())
        assertTrue(result.p10!! <= result.p25!!)
        assertTrue(result.p25!! <= result.p50!!)
        assertTrue(result.p50!! <= result.p75!!)
        assertTrue(result.p75!! <= result.p90!!)
    }

    @Test
    fun `missing and insufficient history are ineligible`() {
        assertNull(HistoricalReturnAnalyzer.analyze(emptyList()))
        assertNull(HistoricalReturnAnalyzer.analyze(priceHistory(prices = listOf(100.0, 101.0), stepDays = 1)))
    }

    @Test
    fun `very long horizon stays finite`() {
        val projection = engine.project(
            listOf(sampleInvestment(id = 1, type = InvestmentType.SOFIPO, currentValue = 1_000.0, institution = gbm)),
            settings,
            horizonMonths = 120,
            parameters = ProjectionParameters(annualReturn = 8.0),
        )

        assertTrue(projection.projectedValue.isFinite())
        assertTrue(projection.projectedValue > 1_000.0)
    }

    @Test
    fun `large contributions are kept out of returns`() {
        val result = CompoundInterestProjectionEngine().project(
            ProjectionInput(
                1_000.0,
                12,
                contributions = listOf(Contribution(monthlyAmount = 100_000.0)),
                parameters = ProjectionParameters(annualReturn = 0.0),
            ),
        )

        assertEquals(1_201_000.0, result.projectedValue, 0.01)
        assertEquals(1_200_000.0, result.contributions, 0.01)
        assertEquals(0.0, result.expectedGain, 0.01)
    }

    @Test
    fun `withdrawals exceeding gains stay finite`() {
        val result = CompoundInterestProjectionEngine().project(
            ProjectionInput(
                1_000.0,
                12,
                withdrawals = listOf(Withdrawal(monthlyAmount = 2_000.0)),
                parameters = ProjectionParameters(annualReturn = 5.0),
            ),
        )

        assertTrue(result.projectedValue.isFinite())
        assertEquals(24_000.0, result.withdrawals, 0.01)
        assertTrue(result.expectedGain.isFinite())
    }
}
