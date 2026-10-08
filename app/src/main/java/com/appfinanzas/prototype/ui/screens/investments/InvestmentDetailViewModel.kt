package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.usecase.AddPricePointResult
import com.appfinanzas.prototype.domain.usecase.AddPricePointUseCase
import com.appfinanzas.prototype.domain.usecase.DeleteInvestmentResult
import com.appfinanzas.prototype.domain.usecase.DeleteInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.GetInvestmentDetail
import com.appfinanzas.prototype.ui.format.DateFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class InvestmentDetailViewModel(
    private val investmentId: Long,
    private val getInvestmentDetail: GetInvestmentDetail,
    private val deleteInvestment: DeleteInvestmentUseCase,
    private val addPricePoint: AddPricePointUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InvestmentDetailUiState(isLoading = true, priceDateText = DateFormatter.format(LocalDate.now())),
    )
    val uiState = _uiState.asStateFlow()

    private val deleteEvents = Channel<Unit>(Channel.BUFFERED)
    val deletedEvents: Flow<Unit> = deleteEvents.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun delete() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true, error = null) }
        viewModelScope.launch {
            when (deleteInvestment.execute(investmentId)) {
                is DeleteInvestmentResult.Success -> deleteEvents.send(Unit)
                is DeleteInvestmentResult.BusinessError ->
                    _uiState.update { it.copy(isDeleting = false, error = "No se pudo eliminar la inversión") }
            }
        }
    }

    fun onPriceChange(value: String) {
        _uiState.update { it.copy(priceText = value, priceError = null, priceSaved = false) }
    }

    fun onPriceDateChange(value: String) {
        _uiState.update { it.copy(priceDateText = value, priceError = null, priceSaved = false) }
    }

    fun savePricePoint() {
        if (_uiState.value.isSavingPrice) return
        _uiState.update { it.copy(isSavingPrice = true, priceError = null) }
        viewModelScope.launch {
            val state = _uiState.value
            when (val result = addPricePoint.execute(investmentId, state.priceText, state.priceDateText)) {
                is AddPricePointResult.Success ->
                    _uiState.update {
                        it.copy(
                            isSavingPrice = false,
                            priceText = "",
                            priceError = null,
                            priceSaved = true,
                        )
                    }
                is AddPricePointResult.Invalid ->
                    _uiState.update { it.copy(isSavingPrice = false, priceError = result.message) }
                is AddPricePointResult.BusinessError ->
                    _uiState.update { it.copy(isSavingPrice = false, priceError = result.message) }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadJob = viewModelScope.launch {
            getInvestmentDetail.observe(investmentId)
                .map { detail ->
                    detail?.toUiState() ?: InvestmentDetailUiState(isEmpty = true)
                }
                .catch { emit(InvestmentDetailUiState(error = it.message ?: "Error inesperado")) }
                .collect { latest ->
                    _uiState.update { current ->
                        latest.copy(
                            priceText = current.priceText,
                            priceDateText = current.priceDateText,
                            priceError = current.priceError,
                            priceSaved = current.priceSaved,
                            isSavingPrice = current.isSavingPrice,
                            isDeleting = current.isDeleting,
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(investmentId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InvestmentDetailViewModel(
                    investmentId = investmentId,
                    getInvestmentDetail = GetInvestmentDetail(AppContainer.investmentRepository),
                    deleteInvestment = DeleteInvestmentUseCase(AppContainer.investmentRepository),
                    addPricePoint = AddPricePointUseCase(AppContainer.investmentRepository),
                )
            }
        }
    }
}
