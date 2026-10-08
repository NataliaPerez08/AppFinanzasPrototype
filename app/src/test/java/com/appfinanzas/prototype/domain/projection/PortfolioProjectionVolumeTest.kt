package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioProjectionVolumeTest {

    @Test
    fun `hundreds of positions project and reconcile`() {
        val engine = PortfolioProjectionEngine(
            monteCarloEngine = MonteCarloProjectionEngine(simulations = 200),
        )
        val investments = (1..300).map { id ->
            sampleInvestment(
                id = id.toLong(),
                name = "Activo $id",
                type = if (id % 3 == 0) InvestmentType.CETES else InvestmentType.ETF_FONDO,
                currentValue = 1_000.0,
            )
        }
        val parameters = ProjectionParameters(annualReturn = 8.0, annualVolatility = 20.0)

        val projection = engine.project(investments, AppSettings(), 12, parameters)
        val series = engine.projectSeries(investments, AppSettings(), 12, parameters)
        val distribution = engine.projectDistribution(investments, AppSettings(), 12, parameters)

        assertEquals(300, projection.assets.size)
        assertEquals(projection.projectedValue, projection.assets.sumOf { it.projectedValue }, 0.01)
        assertEquals(13, series.months.size)
        assertTrue(series.hasProbability)
        assertNotNull(distribution)
        assertTrue(distribution!!.p10 <= distribution.p90)
    }
}
