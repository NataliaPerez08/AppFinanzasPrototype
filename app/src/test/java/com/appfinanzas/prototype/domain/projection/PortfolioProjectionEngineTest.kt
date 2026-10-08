package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.priceHistory
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioProjectionEngineTest {

    private val engine = PortfolioProjectionEngine()
    private val settings = AppSettings()
    private val parameters = ProjectionParameters(annualReturn = 10.0)

    private val gbm = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa")
    private val cetesDirecto = Institution(id = 2, name = "CETES Directo", kind = "Renta fija")

    @Test
    fun `aggregates assets that use different strategies`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm),
            sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, institution = cetesDirecto),
        )

        val projection = engine.project(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(1_500.0, projection.currentValue, 0.01)
        assertEquals(1_650.0, projection.projectedValue, 0.01)
        assertEquals(150.0, projection.expectedGain, 0.01)
        assertEquals(2, projection.assets.size)
        assertEquals(ProjectionStrategy.MONTE_CARLO, projection.assets[0].strategy)
        assertEquals(ProjectionStrategy.FIXED_RATE, projection.assets[1].strategy)
    }

    @Test
    fun `groups assets by institution`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm),
            sampleInvestment(id = 2, name = "AAPL", type = InvestmentType.ACCION, currentValue = 500.0, institution = gbm),
            sampleInvestment(id = 3, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, institution = cetesDirecto),
        )

        val projection = engine.project(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(2, projection.institutions.size)
        val byName = projection.institutions.associateBy { it.name }
        assertEquals(1_500.0, byName.getValue("GBM").currentValue, 0.01)
        assertEquals(1_650.0, byName.getValue("GBM").projectedValue, 0.01)
        assertEquals(2, byName.getValue("GBM").assets.size)
        assertEquals(500.0, byName.getValue("CETES Directo").currentValue, 0.01)
    }

    @Test
    fun `converts foreign currency to the base currency before projecting`() {
        val investments = listOf(
            sampleInvestment(
                id = 1,
                name = "AAPL",
                type = InvestmentType.ACCION,
                currency = Currency.USD,
                currentValue = 100.0,
                institution = gbm,
            ),
        )

        val projection = engine.project(
            investments,
            settings.copy(usdToMxnRate = 20.0),
            horizonMonths = 12,
            parameters = ProjectionParameters(annualReturn = 0.0),
        )

        assertEquals(2_000.0, projection.currentValue, 0.01)
        assertEquals(2_000.0, projection.projectedValue, 0.01)
    }

    @Test
    fun `empty portfolio produces zero projections`() {
        val projection = engine.project(emptyList(), settings, horizonMonths = 12, parameters = parameters)

        assertEquals(0.0, projection.currentValue, 0.001)
        assertEquals(0.0, projection.projectedValue, 0.001)
        assertTrue(projection.assets.isEmpty())
        assertTrue(projection.institutions.isEmpty())
    }

    @Test
    fun `resolver maps instrument types to their strategy`() {
        assertEquals(ProjectionStrategy.FIXED_RATE, ProjectionStrategyResolver.resolve(InvestmentType.CETES))
        assertEquals(ProjectionStrategy.COMPOUND_INTEREST, ProjectionStrategyResolver.resolve(InvestmentType.SOFIPO))
        assertEquals(ProjectionStrategy.MONTE_CARLO, ProjectionStrategyResolver.resolve(InvestmentType.ACCION))
        assertEquals(ProjectionStrategy.MONTE_CARLO, ProjectionStrategyResolver.resolve(InvestmentType.FIBRA))
    }

    @Test
    fun `deterministic portfolio has no distribution`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "CETES", type = InvestmentType.CETES, currentValue = 1_000.0, institution = cetesDirecto),
            sampleInvestment(id = 2, name = "SOFIPO", type = InvestmentType.SOFIPO, currentValue = 1_000.0, institution = gbm),
        )

        val distribution = engine.projectDistribution(investments, settings, horizonMonths = 12, parameters = parameters)

        assertNull(distribution)
    }

    @Test
    fun `probabilistic portfolio yields an ordered distribution around the deterministic assets`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm),
            sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, institution = cetesDirecto),
        )

        val distribution = engine.projectDistribution(
            investments,
            settings,
            horizonMonths = 12,
            parameters = parameters.copy(annualVolatility = 20.0),
        )

        assertNotNull(distribution)
        val d = distribution!!
        assertTrue(d.p10 <= d.p25)
        assertTrue(d.p25 <= d.p50)
        assertTrue(d.p50 <= d.p75)
        assertTrue(d.p75 <= d.p90)
        assertTrue(d.p10 >= 500.0)
    }

    @Test
    fun `distribution is reproducible for a fixed seed`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm),
        )

        val first = engine.projectDistribution(investments, settings, horizonMonths = 12, parameters = parameters)
        val second = engine.projectDistribution(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(first!!.p50, second!!.p50, 0.0)
    }

    @Test
    fun `custom asset with sufficient history uses the historical strategy`() {
        val engine = PortfolioProjectionEngine(
            eligibility = HistoricalEligibility(minObservations = 3, minSpanDays = 30),
        )
        val history = priceHistory(prices = List(13) { 100.0 + it }, stepDays = 30)
        val investments = listOf(
            sampleInvestment(id = 1, name = "Oro", type = InvestmentType.OTRO, currentValue = 1_000.0, institution = gbm),
        )

        val projection = engine.project(
            investments,
            settings,
            horizonMonths = 12,
            parameters = parameters,
            historyByInvestment = mapOf(1L to history),
        )

        assertEquals(ProjectionStrategy.HISTORICAL_RETURN, projection.assets[0].strategy)
        assertTrue(projection.assets[0].projectedValue > 1_100.0)
    }

    @Test
    fun `custom asset without enough history falls back to compound`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "Oro", type = InvestmentType.OTRO, currentValue = 1_000.0, institution = gbm),
        )

        val projection = engine.project(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(ProjectionStrategy.COMPOUND_INTEREST, projection.assets[0].strategy)
        assertEquals(1_100.0, projection.assets[0].projectedValue, 0.01)
    }

    @Test
    fun `series is degenerate for deterministic portfolios`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "CETES", type = InvestmentType.CETES, currentValue = 1_000.0, institution = cetesDirecto),
            sampleInvestment(id = 2, name = "SOFIPO", type = InvestmentType.SOFIPO, currentValue = 500.0, institution = gbm),
        )

        val series = engine.projectSeries(investments, settings, horizonMonths = 12, parameters = parameters)

        assertFalse(series.hasProbability)
        assertEquals(13, series.months.size)
        assertEquals(1_500.0, series.months.first().p50, 0.01)
        assertEquals(1_650.0, series.final.p50, 0.01)
        assertEquals(series.months.first().p10, series.months.first().p90, 0.0)
    }

    @Test
    fun `series widens over time for probabilistic portfolios`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm),
            sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, institution = cetesDirecto),
        )

        val series = engine.projectSeries(
            investments,
            settings,
            horizonMonths = 12,
            parameters = parameters.copy(annualVolatility = 20.0),
        )

        assertTrue(series.hasProbability)
        assertEquals(13, series.months.size)
        series.months.forEach { month ->
            assertTrue(month.p10 <= month.p25)
            assertTrue(month.p25 <= month.p50)
            assertTrue(month.p50 <= month.p75)
            assertTrue(month.p75 <= month.p90)
        }
        val firstWidth = series.months.first().p90 - series.months.first().p10
        val lastWidth = series.final.p90 - series.final.p10
        assertTrue(lastWidth > firstWidth)
    }

    @Test
    fun `monthly contribution is split pro rata and kept out of returns`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "CETES A", type = InvestmentType.CETES, currentValue = 1_000.0, institution = cetesDirecto),
            sampleInvestment(id = 2, name = "CETES B", type = InvestmentType.CETES, currentValue = 1_000.0, institution = cetesDirecto),
        )

        val projection = engine.project(
            investments,
            settings,
            horizonMonths = 12,
            parameters = ProjectionParameters(annualReturn = 0.0),
            monthlyContribution = 1_000.0,
        )

        assertEquals(12_000.0, projection.contributions, 0.01)
        assertEquals(0.0, projection.expectedGain, 0.01)
        assertEquals(6_000.0, projection.assets[0].contributions, 0.01)
        assertEquals(6_000.0, projection.assets[1].contributions, 0.01)
    }

    @Test
    fun `explicit strategy override is honored`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm)
                .copy(projectionStrategy = ProjectionStrategy.COMPOUND_INTEREST),
        )

        val projection = engine.project(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(ProjectionStrategy.COMPOUND_INTEREST, projection.assets[0].strategy)
    }

    @Test
    fun `per investment return overrides the global parameter`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "CETES", type = InvestmentType.CETES, currentValue = 1_000.0, institution = cetesDirecto)
                .copy(projectionReturn = 20.0),
        )

        val projection = engine.project(investments, settings, horizonMonths = 12, parameters = parameters)

        assertEquals(1_200.0, projection.assets[0].projectedValue, 0.01)
    }

    @Test
    fun `per investment volatility drives the probabilistic range`() {
        val investments = listOf(
            sampleInvestment(id = 1, name = "VOO", type = InvestmentType.ETF_FONDO, currentValue = 1_000.0, institution = gbm)
                .copy(projectionReturn = 10.0, projectionVolatility = 0.0),
        )

        val series = engine.projectSeries(investments, settings, horizonMonths = 12, parameters = parameters)

        assertTrue(series.hasProbability)
        assertEquals(series.final.p10, series.final.p90, 0.01)
        assertEquals(1_100.0, series.final.p50, 0.01)
    }
}
