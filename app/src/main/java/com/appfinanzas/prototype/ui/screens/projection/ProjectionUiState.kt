package com.appfinanzas.prototype.ui.screens.projection

import com.appfinanzas.prototype.domain.model.Currency

data class ProjectionUiState(
    val isLoading: Boolean = true,
    val baseCurrency: Currency = Currency.MXN,
    val currentValue: Double = 0.0,
    val expectedReturnText: String = "",
    val inflationText: String = "",
    val isrText: String = "",
    val nominal: Double = 0.0,
    val afterIsr: Double = 0.0,
    val real: Double = 0.0,
    val scenarios: List<ProjectionScenarioUi> = emptyList(),
    val chartPoints: List<Float> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null,
)

data class ProjectionScenarioUi(
    val name: String,
    val nominal: Double,
    val real: Double,
)
