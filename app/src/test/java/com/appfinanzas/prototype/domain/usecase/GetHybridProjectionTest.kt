package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.projection.ProjectionScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHybridProjectionTest {

    private val useCase = GetHybridProjection()
    private val settings = AppSettings()

    @Test
    fun `probabilistic portfolio produces a distribution and adjusted values`() {
        val investments = listOf(
            sampleInvestment(
                id = 1,
                name = "VOO",
                type = InvestmentType.ETF_FONDO,
                currentValue = 1_000.0,
                institution = Institution(1, "GBM", "Casa de Bolsa"),
            ),
            sampleInvestment(
                id = 2,
                name = "CETES",
                type = InvestmentType.CETES,
                currentValue = 500.0,
                institution = Institution(2, "CETES Directo", "Renta fija"),
            ),
        )

        val result = useCase.compute(
            investments = investments,
            settings = settings,
            expectedReturn = 10.0,
            inflation = 4.0,
            isr = 2.0,
            volatility = 20.0,
        )

        assertEquals(1_500.0, result.currentValue, 0.01)
        assertNotNull(result.distribution)
        assertTrue(result.distribution!!.p10 <= result.distribution!!.p50)
        assertTrue(result.adjusted.real <= result.adjusted.afterTax)
        assertTrue(result.adjusted.afterTax <= result.adjusted.nominal)
        assertEquals(2, result.assets.size)
        assertEquals(2, result.institutions.size)
        assertEquals(0.0, result.growth.contributions, 0.001)
    }

    @Test
    fun `deterministic portfolio has no distribution`() {
        val investments = listOf(
            sampleInvestment(
                id = 1,
                name = "CETES",
                type = InvestmentType.CETES,
                currentValue = 1_000.0,
                institution = Institution(2, "CETES Directo", "Renta fija"),
            ),
        )

        val result = useCase.compute(
            investments = investments,
            settings = settings,
            expectedReturn = 10.0,
            inflation = 4.0,
            isr = 2.0,
        )

        assertNull(result.distribution)
        assertEquals(1_100.0, result.nominal, 0.01)
    }

    @Test
    fun `scenarios are projection only and do not mutate the inputs`() {
        val investments = listOf(
            sampleInvestment(
                id = 1,
                name = "VOO",
                type = InvestmentType.ETF_FONDO,
                currentValue = 1_000.0,
                institution = Institution(1, "GBM", "Casa de Bolsa"),
            ),
        )
        val snapshot = investments.toList()
        val base = ProjectionScenario.base(
            expectedReturn = 10.0,
            inflation = 4.0,
            volatility = 20.0,
            isr = 2.0,
            monthlyContribution = 0.0,
        )

        val pessimistic = useCase.computeScenario(investments, settings, ProjectionScenario.pessimistic(base))
        val optimistic = useCase.computeScenario(investments, settings, ProjectionScenario.optimistic(base))

        assertTrue(pessimistic.nominal < optimistic.nominal)
        assertEquals(snapshot, investments)
    }
}
