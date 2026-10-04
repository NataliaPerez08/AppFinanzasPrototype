package com.appfinanzas.prototype.ui.screens.projection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.money.CurrencyConverter
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.GetProjection
import com.appfinanzas.prototype.domain.usecase.GetSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectionViewModel(
    private val getProjection: GetProjection,
    private val getSettings: GetSettings,
    private val repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectionUiState())
    val uiState = _uiState.asStateFlow()

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

    private fun load() {
        loadJob?.cancel()
        _uiState.value = ProjectionUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            try {
                combine(
                    repository.observeInvestments(),
                    getSettings.observe(),
                ) { investments, settings ->
                    val currentValue = investments.sumOf {
                        CurrencyConverter.convert(it.currentValue, it.currency, settings.baseCurrency, settings.usdToMxnRate)
                    }
                    currentValue to settings
                }.collect { (currentValue, settings) ->
                        _uiState.update { state ->
                            state.copy(
                                currentValue = currentValue,
                                baseCurrency = settings.baseCurrency,
                                isLoading = false,
                                isEmpty = currentValue == 0.0,
                                inflationText = state.inflationText.ifBlank { settings.estimatedInflation.toString() },
                                isrText = state.isrText.ifBlank { settings.estimatedIsr.toString() },
                                expectedReturnText = state.expectedReturnText.ifBlank { settings.expectedReturn.toString() },
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
        val expectedReturn = state.expectedReturnText.toDoubleOrNull() ?: 8.5
        val inflation = state.inflationText.toDoubleOrNull() ?: 0.0
        val isr = state.isrText.toDoubleOrNull() ?: 0.0
        val result = getProjection.compute(state.currentValue, expectedReturn, inflation, isr)
        _uiState.update {
            it.copy(
                nominal = result.nominal,
                afterIsr = result.afterIsr,
                real = result.real,
                scenarios = result.scenarios.map { scenario ->
                    ProjectionScenarioUi(
                        name = scenario.scenario.name,
                        nominal = scenario.nominal,
                        real = scenario.real,
                    )
                },
                chartPoints = getProjection.chartSeries(state.currentValue, expectedReturn),
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ProjectionViewModel(
                    getProjection = GetProjection(),
                    getSettings = GetSettings(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }
    }
}
