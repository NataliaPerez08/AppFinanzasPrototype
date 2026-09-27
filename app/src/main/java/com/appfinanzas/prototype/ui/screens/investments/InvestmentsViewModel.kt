package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.usecase.GetInvestments
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InvestmentsViewModel(
    private val getInvestments: GetInvestments,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun selectFilter(category: InvestmentCategory?) {
        _uiState.value = _uiState.value.copy(filter = category)
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        val selectedFilter = _uiState.value.filter
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            getInvestments.observe(selectedFilter)
                .map { it.toUiState(selectedFilter) }
                .catch { emit(InvestmentsUiState(filter = selectedFilter, error = it.message ?: "Error inesperado")) }
                .collect { _uiState.value = it }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                InvestmentsViewModel(GetInvestments(AppContainer.investmentRepository))
            }
        }
    }
}