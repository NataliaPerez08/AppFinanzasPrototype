package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import org.junit.Assert.assertEquals
import org.junit.Test

class ManualProjectionEngineTest {

    @Test
    fun `projects at the configured annual return`() {
        val result = ManualProjectionEngine().project(
            ProjectionInput(
                currentValue = 100_000.0,
                horizonMonths = 12,
                parameters = ProjectionParameters(annualReturn = 7.0, compoundingFrequency = 1),
            ),
        )

        assertEquals(ProjectionStrategy.MANUAL, result.strategy)
        assertEquals(107_000.0, result.projectedValue, 0.01)
        assertEquals(7_000.0, result.expectedGain, 0.01)
    }
}
