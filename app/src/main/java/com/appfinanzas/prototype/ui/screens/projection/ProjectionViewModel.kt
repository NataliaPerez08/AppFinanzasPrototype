package com.appfinanzas.prototype.ui.screens.projection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.projection.ProjectionDistribution
import com.appfinanzas.prototype.domain.projection.ProjectionScenario
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.GetHybridProjection
import com.appfinanzas.prototype.domain.usecase.GetSettings
import com.appfinanzas.prototype.ui.components.ProjectionChartBand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectionViewModel(
    private val getHybridProjection: GetHybridProjection,
    private val getSettings: GetSettings,
    private val repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectionUiState())
    val uiState = _uiState.asStateFlow()

    private var latestInvestments: List<Investment> = emptyList()
    private var latestSettings: AppSettings = AppSettings()
    private var latestPriceHistory: Map<Long, List<PricePoint>> = emptyMap()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun onExpectedReturnChange(value: String) {
        _uiState.update { it.copy(expectedReturnText = value) }
        recompute()
    }

    fun onInflationChange(value: String) {
        _uiState.update { it.copy(inflationText = value) }
        recompute()
    }

    fun onIsrChange(value: String) {
        _uiState.update { it.copy(isrText = value) }
        recompute()
    }

    fun onVolatilityChange(value: String) {
        _uiState.update { it.copy(volatilityText = value) }
        recompute()
    }

    fun onHorizonSelected(months: Int) {
        _uiState.update { it.copy(horizonMonths = months) }
        recompute()
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                combine(
                    repository.observeInvestments(),
                    getSettings.observe(),
                    repository.observePriceHistory(),
                ) { investments, settings, priceHistory ->
                    Triple(investments, settings, priceHistory)
                }.collect { (investments, settings, priceHistory) ->
                    latestInvestments = investments
                    latestSettings = settings
                    latestPriceHistory = priceHistory
                    _uiState.update { state ->
                        state.copy(
                            baseCurrency = settings.baseCurrency,
                            isLoading = false,
                            inflationText = state.inflationText.ifBlank { settings.estimatedInflation.toString() },
                            isrText = state.isrText.ifBlank { settings.estimatedIsr.toString() },
                            expectedReturnText = state.expectedReturnText.ifBlank { settings.expectedReturn.toString() },
                            volatilityText = state.volatilityText.ifBlank { settings.estimatedVolatility.toString() },
                        )
                    }
                    recompute()
                }
            } catch (e: Exception) {
                _uiState.value = ProjectionUiState(isLoading = false, error = e.message ?: "Error inesperado")
            }
        }
    }

    private fun recompute() {
        val state = _uiState.value
        val expectedReturn = state.expectedReturnText.toDoubleOrNull() ?: latestSettings.expectedReturn
        val inflation = state.inflationText.toDoubleOrNull() ?: 0.0
        val isr = state.isrText.toDoubleOrNull() ?: 0.0
        val volatility = state.volatilityText.toDoubleOrNull() ?: latestSettings.estimatedVolatility

        val result = getHybridProjection.compute(
            investments = latestInvestments,
            settings = latestSettings,
            expectedReturn = expectedReturn,
            inflation = inflation,
            isr = isr,
            volatility = volatility,
            horizonMonths = state.horizonMonths,
            historyByInvestment = latestPriceHistory,
        )

        val distribution = result.distribution
        val baseScenario = ProjectionScenario.base(
            expectedReturn = expectedReturn,
            inflation = inflation,
            volatility = volatility,
            isr = isr,
            monthlyContribution = latestSettings.monthlyContribution,
        )
        val scenarios = ProjectionScenario.presets(baseScenario).map { preset ->
            val scenarioResult = getHybridProjection.computeScenario(
                investments = latestInvestments,
                settings = latestSettings,
                scenario = preset,
                horizonMonths = state.horizonMonths,
                historyByInvestment = latestPriceHistory,
            )
            ProjectionScenarioUi(
                label = preset.type.label,
                nominal = scenarioResult.nominal,
                real = scenarioResult.adjusted.real,
            )
        }
        _uiState.update {
            it.copy(
                currentValue = result.currentValue,
                isEmpty = result.currentValue == 0.0,
                nominal = result.nominal,
                expectedGain = result.growth.investmentReturns,
                p10 = distribution?.p10,
                p50 = distribution?.p50,
                p90 = distribution?.p90,
                afterTax = result.adjusted.afterTax,
                real = result.adjusted.real,
                estimatedTaxes = result.adjusted.estimatedTaxes,
                contributions = result.growth.contributions,
                investmentReturns = result.growth.investmentReturns,
                inflationImpact = result.adjusted.inflationImpact,
                institutions = result.institutions.map { institution ->
                    ProjectionInstitutionUi(
                        name = institution.name,
                        currentValue = institution.currentValue,
                        projectedValue = institution.projectedValue,
                        expectedGain = institution.expectedGain,
                        assets = institution.assets.map { asset ->
                            ProjectionAssetUi(
                                label = asset.label,
                                strategyLabel = asset.strategy.label,
                                currentValue = asset.currentValue,
                                projectedValue = asset.projectedValue,
                                expectedGain = asset.expectedGain,
                                contributions = asset.contributions,
                            )
                        },
                    )
                },
                chartBand = result.series.months.toChartBand(),
                scenarios = scenarios,
            )
        }
    }

    private fun List<ProjectionDistribution>.toChartBand(): ProjectionChartBand? {
        if (size < 2) return null
        return ProjectionChartBand(
            low = map { it.p10.toFloat() },
            mid = map { it.p50.toFloat() },
            high = map { it.p90.toFloat() },
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ProjectionViewModel(
                    getHybridProjection = GetHybridProjection(),
                    getSettings = GetSettings(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }
    }
}
