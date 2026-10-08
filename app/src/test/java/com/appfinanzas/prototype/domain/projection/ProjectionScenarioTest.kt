package com.appfinanzas.prototype.domain.projection

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectionScenarioTest {

    @Test
    fun `presets apply the documented deltas`() {
        val base = ProjectionScenario.base(
            expectedReturn = 8.0,
            inflation = 4.0,
            volatility = 15.0,
            isr = 2.0,
            monthlyContribution = 1_000.0,
        )

        val presets = ProjectionScenario.presets(base)

        assertEquals(ProjectionScenarioType.BASE, presets[0].type)
        assertEquals(8.0, presets[0].expectedReturn, 0.001)
        assertEquals(ProjectionScenarioType.PESSIMISTIC, presets[1].type)
        assertEquals(6.0, presets[1].expectedReturn, 0.001)
        assertEquals(4.5, presets[1].inflation, 0.001)
        assertEquals(ProjectionScenarioType.OPTIMISTIC, presets[2].type)
        assertEquals(10.0, presets[2].expectedReturn, 0.001)
        assertEquals(3.5, presets[2].inflation, 0.001)
        assertEquals(1_000.0, presets[2].monthlyContribution, 0.001)
    }
}
