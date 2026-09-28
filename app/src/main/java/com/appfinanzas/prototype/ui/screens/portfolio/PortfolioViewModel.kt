package com.appfinanzas.prototype.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.usecase.GetPortfolioSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val getPortfolioSummary: GetPortfolioSummary,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _uiState.value = PortfolioUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            getPortfolioSummary.observe()
                .map { it.toUiState() }
                .catch { emit(PortfolioUiState(error = it.message ?: "Error inesperado")) }
                .collect { _uiState.value = it }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PortfolioViewModel(GetPortfolioSummary(AppContainer.investmentRepository))
            }
        }
    }
}