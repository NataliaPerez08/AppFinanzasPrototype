package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.ProjectionScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectionCalculatorTest {

    @Test
    fun `projects nominal after isr and real values`() {
        val result = ProjectionCalculator.project(
            currentValue = 1_245_320.0,
            expectedReturn = 8.5,
            inflation = 4.0,
            isr = 2.0,
        )

        assertEquals(1_245_320.0, result.currentValue, 0.01)
        assertEquals(1_351_172.2, result.nominal, 1.0)
        assertEquals(1_324_148.5, result.afterIsr, 1.0)
        assertEquals(1_273_219.7, result.real, 1.0)
    }

    @Test
    fun `returns three scenarios in order`() {
        val result = ProjectionCalculator.project(
            currentValue = 100_000.0,
            expectedReturn = 8.0,
            inflation = 4.0,
            isr = 2.0,
        )

        assertEquals(3, result.scenarios.size)
        assertEquals(ProjectionScenario.CONSERVADOR, result.scenarios[0].scenario)
        assertEquals(ProjectionScenario.BASE, result.scenarios[1].scenario)
        assertEquals(ProjectionScenario.OPTIMISTA, result.scenarios[2].scenario)
        assertTrue(result.scenarios[0].nominal < result.scenarios[1].nominal)
        assertTrue(result.scenarios[1].nominal < result.scenarios[2].nominal)
    }

    @Test
    fun `scenarios apply deltas to return and inflation`() {
        val result = ProjectionCalculator.project(
            currentValue = 100_000.0,
            expectedReturn = 8.0,
            inflation = 4.0,
            isr = 2.0,
        )

        val base = result.scenarios[1]
        val conservador = result.scenarios[0]
        val optimista = result.scenarios[2]

        assertEquals(8.0, base.expectedReturn, 0.001)
        assertEquals(6.0, conservador.expectedReturn, 0.001)
        assertEquals(10.0, optimista.expectedReturn, 0.001)
        assertEquals(4.5, conservador.inflation, 0.001)
        assertEquals(3.5, optimista.inflation, 0.001)
    }

    @Test
    fun `zero current value yields zero results`() {
        val result = ProjectionCalculator.project(
            currentValue = 0.0,
            expectedReturn = 8.5,
            inflation = 4.0,
            isr = 2.0,
        )

        assertEquals(0.0, result.nominal, 0.001)
        assertEquals(0.0, result.afterIsr, 0.001)
        assertEquals(0.0, result.real, 0.001)
    }

    @Test
    fun `monthly series rises over time and is empty for zero value`() {
        val series = ProjectionCalculator.monthlySeries(currentValue = 1_000.0, expectedReturn = 12.0)

        assertEquals(13, series.size)
        assertEquals(0.9f, series.first(), 0.001f)
        assertEquals(0.1f, series.last(), 0.001f)
        assertTrue(series.zipWithNext().all { (a, b) -> b <= a })

        assertTrue(ProjectionCalculator.monthlySeries(0.0, 12.0).isEmpty())
    }
}