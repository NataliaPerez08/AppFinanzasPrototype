package com.appfinanzas.prototype.domain.projection

data class GrowthAttribution(
    val label: String,
    val currentValue: Double,
    val projectedValue: Double,
    val investmentReturns: Double,
    val contributions: Double,
    val withdrawals: Double,
)

data class ProjectedGrowthBreakdown(
    val contributions: Double,
    val investmentReturns: Double,
    val withdrawals: Double,
    val estimatedTaxes: Double,
    val byInstitution: List<GrowthAttribution>,
    val byAsset: List<GrowthAttribution>,
    val byAssetType: List<GrowthAttribution>,
) {
    val netGrowth: Double get() = contributions + investmentReturns - withdrawals
    val netAfterTax: Double get() = netGrowth - estimatedTaxes
}

object GrowthAttributionBuilder {

    fun build(projection: PortfolioProjection, estimatedTaxes: Double = 0.0): ProjectedGrowthBreakdown =
        ProjectedGrowthBreakdown(
            contributions = projection.contributions,
            investmentReturns = projection.expectedGain,
            withdrawals = projection.withdrawals,
            estimatedTaxes = estimatedTaxes,
            byInstitution = projection.institutions.map { institution ->
                GrowthAttribution(
                    label = institution.name,
                    currentValue = institution.currentValue,
                    projectedValue = institution.projectedValue,
                    investmentReturns = institution.expectedGain,
                    contributions = institution.contributions,
                    withdrawals = institution.withdrawals,
                )
            },
            byAsset = projection.assets.map { asset ->
                GrowthAttribution(
                    label = asset.label,
                    currentValue = asset.currentValue,
                    projectedValue = asset.projectedValue,
                    investmentReturns = asset.expectedGain,
                    contributions = asset.contributions,
                    withdrawals = asset.withdrawals,
                )
            },
            byAssetType = projection.assets
                .groupBy { it.investmentType }
                .map { (type, group) ->
                    GrowthAttribution(
                        label = type.name,
                        currentValue = group.sumOf { it.currentValue },
                        projectedValue = group.sumOf { it.projectedValue },
                        investmentReturns = group.sumOf { it.expectedGain },
                        contributions = group.sumOf { it.contributions },
                        withdrawals = group.sumOf { it.withdrawals },
                    )
                },
        )
}
