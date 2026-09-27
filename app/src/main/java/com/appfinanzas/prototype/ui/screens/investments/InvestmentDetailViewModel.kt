package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.usecase.GetInvestmentDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InvestmentDetailViewModel(
    private val investmentId: Long,
    private val getInvestmentDetail: GetInvestmentDetail,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentDetailUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _uiState.value = InvestmentDetailUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            getInvestmentDetail.observe(investmentId)
                .map { detail ->
                    detail?.toUiState() ?: InvestmentDetailUiState(isEmpty = true)
                }
                .catch { emit(InvestmentDetailUiState(error = it.message ?: "Error inesperado")) }
                .collect { _uiState.value = it }
        }
    }

    companion object {
        fun factory(investmentId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InvestmentDetailViewModel(
                    investmentId = investmentId,
                    getInvestmentDetail = GetInvestmentDetail(AppContainer.investmentRepository),
                )
            }
        }
    }
}