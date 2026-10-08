package com.appfinanzas.prototype.domain.projection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectionAdjustmentsTest {

    @Test
    fun `splits nominal into taxes, after tax and real value`() {
        val adjusted = ProjectionAdjustments.adjust(
            nominal = 110_000.0,
            isrPercent = 2.0,
            inflationPercent = 4.0,
            years = 1.0,
        )

        assertEquals(110_000.0, adjusted.nominal, 0.01)
        assertEquals(2_200.0, adjusted.estimatedTaxes, 0.01)
        assertEquals(107_800.0, adjusted.afterTax, 0.01)
        assertEquals(107_800.0 / 1.04, adjusted.real, 0.01)
        assertEquals(107_800.0 - 107_800.0 / 1.04, adjusted.inflationImpact, 0.01)
        assertTrue(adjusted.real <= adjusted.afterTax)
        assertTrue(adjusted.afterTax <= adjusted.nominal)
    }

    @Test
    fun `full tax or full deflation stay finite`() {
        val allTax = ProjectionAdjustments.adjust(1_000.0, isrPercent = 100.0, inflationPercent = 0.0, years = 1.0)
        val fullDeflation = ProjectionAdjustments.adjust(1_000.0, isrPercent = 0.0, inflationPercent = -100.0, years = 1.0)

        assertEquals(0.0, allTax.afterTax, 0.001)
        assertEquals(0.0, fullDeflation.real, 0.001)
    }
}
