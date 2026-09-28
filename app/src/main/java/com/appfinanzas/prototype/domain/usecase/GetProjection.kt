package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.ProjectionResult

class GetProjection {
    fun compute(
        currentValue: Double,
        expectedReturn: Double,
        inflation: Double,
        isr: Double,
    ): ProjectionResult = ProjectionCalculator.project(currentValue, expectedReturn, inflation, isr)

    fun chartSeries(
        currentValue: Double,
        expectedReturn: Double,
    ): List<Float> = ProjectionCalculator.monthlySeries(currentValue, expectedReturn)
}