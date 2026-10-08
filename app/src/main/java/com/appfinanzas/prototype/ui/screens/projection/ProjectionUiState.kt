package com.appfinanzas.prototype.ui.screens.projection

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.components.ProjectionChartBand

val projectionHorizons: List<Int> = listOf(3, 6, 12, 36, 60, 120)

fun horizonLabel(months: Int): String = when (months) {
    3 -> "3M"
    6 -> "6M"
    12 -> "1A"
    36 -> "3A"
    60 -> "5A"
    120 -> "10A"
    else -> "${months}M"
}

data class ProjectionUiState(
    val isLoading: Boolean = true,
    val baseCurrency: Currency = Currency.MXN,
    val currentValue: Double = 0.0,
    val expectedReturnText: String = "",
    val inflationText: String = "",
    val isrText: String = "",
    val volatilityText: String = "",
    val horizonMonths: Int = 12,
    val nominal: Double = 0.0,
    val expectedGain: Double = 0.0,
    val p10: Double? = null,
    val p50: Double? = null,
    val p90: Double? = null,
    val afterTax: Double = 0.0,
    val real: Double = 0.0,
    val estimatedTaxes: Double = 0.0,
    val contributions: Double = 0.0,
    val investmentReturns: Double = 0.0,
    val inflationImpact: Double = 0.0,
    val institutions: List<ProjectionInstitutionUi> = emptyList(),
    val scenarios: List<ProjectionScenarioUi> = emptyList(),
    val chartBand: ProjectionChartBand? = null,
    val isEmpty: Boolean = false,
    val error: String? = null,
)

data class ProjectionScenarioUi(
    val label: String,
    val nominal: Double,
    val real: Double,
)

data class ProjectionInstitutionUi(
    val name: String,
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val assets: List<ProjectionAssetUi>,
)

data class ProjectionAssetUi(
    val label: String,
    val strategyLabel: String,
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val contributions: Double,
)
