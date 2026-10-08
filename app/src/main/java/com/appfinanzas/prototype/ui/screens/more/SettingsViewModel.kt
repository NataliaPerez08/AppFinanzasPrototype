package com.appfinanzas.prototype.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.usecase.GetSettings
import com.appfinanzas.prototype.domain.usecase.SaveSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettings: GetSettings,
    private val saveSettings: SaveSettings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = SettingsUiState(isLoading = true)
        viewModelScope.launch {
            try {
                getSettings.observe().first().let { settings ->
                    _uiState.value = SettingsUiState(
                        isLoading = false,
                        baseCurrency = settings.baseCurrency,
                        usdToMxnRateText = settings.usdToMxnRate.toString(),
                        inflationText = settings.estimatedInflation.toString(),
                        isrText = settings.estimatedIsr.toString(),
                        expectedReturnText = settings.expectedReturn.toString(),
                        volatilityText = settings.estimatedVolatility.toString(),
                        contributionText = settings.monthlyContribution.toString(),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState(isLoading = false, error = e.message ?: "Error inesperado")
            }
        }
    }

    fun onCurrencySelected(value: String) {
        _uiState.update { it.copy(baseCurrency = Currency.valueOf(value)) }
    }

    fun onInflationChange(value: String) {
        _uiState.update { it.copy(inflationText = value, saved = false) }
    }

    fun onUsdToMxnRateChange(value: String) {
        _uiState.update { it.copy(usdToMxnRateText = value, saved = false) }
    }

    fun onIsrChange(value: String) {
        _uiState.update { it.copy(isrText = value, saved = false) }
    }

    fun onExpectedReturnChange(value: String) {
        _uiState.update { it.copy(expectedReturnText = value, saved = false) }
    }

    fun onVolatilityChange(value: String) {
        _uiState.update { it.copy(volatilityText = value, saved = false) }
    }

    fun onContributionChange(value: String) {
        _uiState.update { it.copy(contributionText = value, saved = false) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val inflation = state.inflationText.toDoubleOrNull()
        val isr = state.isrText.toDoubleOrNull()
        val expectedReturn = state.expectedReturnText.toDoubleOrNull()
        val usdToMxnRate = state.usdToMxnRateText.toDoubleOrNull()
        val volatility = state.volatilityText.toDoubleOrNull()
        val contribution = state.contributionText.toDoubleOrNull()
        if (inflation == null || inflation < 0.0) {
            _uiState.update { it.copy(formError = "Inflación inválida") }
            return
        }
        if (isr == null || isr < 0.0) {
            _uiState.update { it.copy(formError = "ISR inválido") }
            return
        }
        if (expectedReturn == null || expectedReturn < 0.0) {
            _uiState.update { it.copy(formError = "Rendimiento inválido") }
            return
        }
        if (volatility == null || volatility < 0.0) {
            _uiState.update { it.copy(formError = "Volatilidad inválida") }
            return
        }
        if (contribution == null || contribution < 0.0) {
            _uiState.update { it.copy(formError = "Aporte inválido") }
            return
        }
        if (usdToMxnRate == null || usdToMxnRate <= 0.0) {
            _uiState.update { it.copy(formError = "Tipo de cambio inválido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, formError = null) }
            saveSettings.execute(
                AppSettings(
                    baseCurrency = state.baseCurrency,
                    estimatedInflation = inflation,
                    estimatedIsr = isr,
                    expectedReturn = expectedReturn,
                    estimatedVolatility = volatility,
                    monthlyContribution = contribution,
                    usdToMxnRate = usdToMxnRate,
                ),
            )
            _uiState.update { it.copy(isSubmitting = false, saved = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    getSettings = GetSettings(AppContainer.investmentRepository),
                    saveSettings = SaveSettings(AppContainer.investmentRepository),
                )
            }
        }
    }
}
