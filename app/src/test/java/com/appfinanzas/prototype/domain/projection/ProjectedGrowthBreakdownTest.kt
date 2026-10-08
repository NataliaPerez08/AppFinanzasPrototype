package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectedGrowthBreakdownTest {

    @Test
    fun `deposits are not reported as investment returns`() {
        val assets = listOf(
            AssetProjection(
                investmentId = 1,
                label = "VOO",
                investmentType = InvestmentType.ETF_FONDO,
                strategy = ProjectionStrategy.MONTE_CARLO,
                currentValue = 1_000.0,
                projectedValue = 1_200.0,
                expectedGain = 100.0,
                contributions = 100.0,
                withdrawals = 0.0,
            ),
        )
        val projection = PortfolioProjection(
            currentValue = 1_000.0,
            projectedValue = 1_200.0,
            expectedGain = 100.0,
            contributions = 100.0,
            withdrawals = 0.0,
            assets = assets,
            institutions = listOf(
                InstitutionProjection("GBM", 1_000.0, 1_200.0, 100.0, 100.0, 0.0, assets),
            ),
        )

        val growth = GrowthAttributionBuilder.build(projection, estimatedTaxes = 20.0)

        assertEquals(100.0, growth.investmentReturns, 0.001)
        assertEquals(100.0, growth.contributions, 0.001)
        assertEquals(200.0, growth.netGrowth, 0.001)
        assertEquals(180.0, growth.netAfterTax, 0.001)
    }

    @Test
    fun `attributes returns by institution, asset and asset type`() {
        val engine = PortfolioProjectionEngine()
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
        val projection = engine.project(
            investments,
            AppSettings(),
            horizonMonths = 12,
            parameters = ProjectionParameters(annualReturn = 10.0),
        )

        val growth = GrowthAttributionBuilder.build(projection)

        assertEquals(projection.expectedGain, growth.investmentReturns, 0.001)
        assertEquals(150.0, growth.investmentReturns, 0.01)
        assertTrue(growth.byInstitution.any { it.label == "GBM" })
        assertTrue(growth.byAsset.any { it.label == "VOO" })
        assertTrue(growth.byAssetType.any { it.label == InvestmentType.CETES.name })
        assertTrue(growth.byAssetType.any { it.label == InvestmentType.ETF_FONDO.name })
    }
}
