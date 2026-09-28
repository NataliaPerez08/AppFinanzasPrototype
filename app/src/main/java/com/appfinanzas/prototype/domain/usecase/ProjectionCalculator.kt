package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.ProjectionResult
import com.appfinanzas.prototype.domain.model.ProjectionScenario
import com.appfinanzas.prototype.domain.model.ScenarioProjection
import kotlin.math.pow

object ProjectionCalculator {

    fun project(
        currentValue: Double,
        expectedReturn: Double,
        inflation: Double,
        isr: Double,
        years: Int = 1,
    ): ProjectionResult {
        val nominal = currentValue * (1.0 + expectedReturn / 100.0).pow(years)
        val afterIsr = nominal * (1.0 - isr / 100.0).pow(years)
        val real = afterIsr / (1.0 + inflation / 100.0).pow(years)
        return ProjectionResult(
            currentValue = currentValue,
            nominal = nominal,
            afterIsr = afterIsr,
            real = real,
            scenarios = scenarios(currentValue, expectedReturn, inflation, isr, years),
        )
    }

    fun scenarios(
        currentValue: Double,
        expectedReturn: Double,
        inflation: Double,
        isr: Double,
        years: Int = 1,
    ): List<ScenarioProjection> {
        val configs = listOf(
            Triple(ProjectionScenario.CONSERVADOR, expectedReturn - 2.0, inflation + 0.5),
            Triple(ProjectionScenario.BASE, expectedReturn, inflation),
            Triple(ProjectionScenario.OPTIMISTA, expectedReturn + 2.0, inflation - 0.5),
        )
        return configs.map { (scenario, scenarioReturn, scenarioInflation) ->
            val nominal = currentValue * (1.0 + scenarioReturn / 100.0).pow(years)
            val afterIsr = nominal * (1.0 - isr / 100.0).pow(years)
            val real = afterIsr / (1.0 + scenarioInflation / 100.0).pow(years)
            ScenarioProjection(
                scenario = scenario,
                expectedReturn = scenarioReturn,
                inflation = scenarioInflation,
                isr = isr,
                nominal = nominal,
                afterIsr = afterIsr,
                real = real,
            )
        }
    }

    fun monthlySeries(
        currentValue: Double,
        expectedReturn: Double,
        months: Int = 12,
    ): List<Float> {
        if (currentValue <= 0.0) return emptyList()
        val monthlyRate = (expectedReturn / 100.0) / months
        val values = (0..months).map { currentValue * (1.0 + monthlyRate * it) }
        val min = values.min()
        val max = values.max()
        return values.map { value ->
            val progress = if (max > min) (value - min) / (max - min) else 0.0
            (0.9 - progress * 0.8).toFloat()
        }
    }
}