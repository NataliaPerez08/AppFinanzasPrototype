package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.projection.AdjustedProjection
import com.appfinanzas.prototype.domain.projection.AssetProjection
import com.appfinanzas.prototype.domain.projection.DEFAULT_ANNUAL_VOLATILITY
import com.appfinanzas.prototype.domain.projection.GrowthAttributionBuilder
import com.appfinanzas.prototype.domain.projection.InstitutionProjection
import com.appfinanzas.prototype.domain.projection.PortfolioProjectionEngine
import com.appfinanzas.prototype.domain.projection.ProjectedGrowthBreakdown
import com.appfinanzas.prototype.domain.projection.ProjectionAdjustments
import com.appfinanzas.prototype.domain.projection.ProjectionDistribution
import com.appfinanzas.prototype.domain.projection.ProjectionParameters
import com.appfinanzas.prototype.domain.projection.ProjectionScenario
import com.appfinanzas.prototype.domain.projection.ProjectionScenarioType
import com.appfinanzas.prototype.domain.projection.ProjectionSeries

data class HybridProjectionResult(
    val currentValue: Double,
    val nominal: Double,
    val adjusted: AdjustedProjection,
    val distribution: ProjectionDistribution?,
    val series: ProjectionSeries,
    val growth: ProjectedGrowthBreakdown,
    val assets: List<AssetProjection>,
    val institutions: List<InstitutionProjection>,
)

class GetHybridProjection(
    private val portfolioEngine: PortfolioProjectionEngine = PortfolioProjectionEngine(),
) {

    fun compute(
        investments: List<Investment>,
        settings: AppSettings,
        expectedReturn: Double,
        inflation: Double,
        isr: Double,
        volatility: Double = DEFAULT_ANNUAL_VOLATILITY,
        horizonMonths: Int = 12,
        historyByInvestment: Map<Long, List<PricePoint>> = emptyMap(),
        monthlyContribution: Double = settings.monthlyContribution,
    ): HybridProjectionResult {
        val parameters = ProjectionParameters(
            annualReturn = expectedReturn,
            annualVolatility = volatility,
            inflation = inflation,
            isr = isr,
            compoundingFrequency = 1,
        )
        val portfolio = portfolioEngine.project(
            investments,
            settings,
            horizonMonths,
            parameters,
            historyByInvestment,
            monthlyContribution,
        )
        val series = portfolioEngine.projectSeries(
            investments,
            settings,
            horizonMonths,
            parameters,
            historyByInvestment,
            monthlyContribution,
        )
        val distribution = if (series.hasProbability) series.final else null
        val adjusted = ProjectionAdjustments.adjust(
            nominal = portfolio.projectedValue,
            isrPercent = isr,
            inflationPercent = inflation,
            years = horizonMonths / 12.0,
        )
        return HybridProjectionResult(
            currentValue = portfolio.currentValue,
            nominal = portfolio.projectedValue,
            adjusted = adjusted,
            distribution = distribution,
            series = series,
            growth = GrowthAttributionBuilder.build(portfolio, adjusted.estimatedTaxes),
            assets = portfolio.assets,
            institutions = portfolio.institutions,
        )
    }

    fun computeScenario(
        investments: List<Investment>,
        settings: AppSettings,
        scenario: ProjectionScenario,
        horizonMonths: Int = 12,
        historyByInvestment: Map<Long, List<PricePoint>> = emptyMap(),
    ): ProjectionScenarioResult {
        val parameters = ProjectionParameters(
            annualReturn = scenario.expectedReturn,
            annualVolatility = scenario.volatility,
            inflation = scenario.inflation,
            isr = scenario.isr,
            compoundingFrequency = 1,
        )
        val portfolio = portfolioEngine.project(
            investments,
            settings,
            horizonMonths,
            parameters,
            historyByInvestment,
            scenario.monthlyContribution,
        )
        return ProjectionScenarioResult(
            type = scenario.type,
            nominal = portfolio.projectedValue,
            adjusted = ProjectionAdjustments.adjust(
                nominal = portfolio.projectedValue,
                isrPercent = scenario.isr,
                inflationPercent = scenario.inflation,
                years = horizonMonths / 12.0,
            ),
        )
    }
}

data class ProjectionScenarioResult(
    val type: ProjectionScenarioType,
    val nominal: Double,
    val adjusted: AdjustedProjection,
)
