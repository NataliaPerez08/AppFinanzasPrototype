package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.AddTransactionResult
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import com.appfinanzas.prototype.domain.usecase.TransactionCalculator
import com.appfinanzas.prototype.domain.validation.TransactionForm
import com.appfinanzas.prototype.domain.validation.TransactionFormValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val investmentId: Long,
    private val addTransactionUseCase: AddTransactionUseCase,
    repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState = _uiState.asStateFlow()

    private val saveEvents = Channel<Long>(Channel.BUFFERED)
    val savedEvents: Flow<Long> = saveEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeInvestment(investmentId)
                .filterNotNull()
                .map { it.toHeaderUi() }
                .collect { header ->
                    _uiState.update { it.copy(investmentHeader = header) }
                }
        }
    }

    fun onTypeSelected(value: String) {
        _uiState.update {
            it.copy(
                type = TransactionType.valueOf(value),
                fieldErrors = it.fieldErrors - TransactionFormValidator.FIELD_TYPE,
            )
        }
        recomputeTotal()
    }

    fun onDateChange(value: String) {
        _uiState.update {
            it.copy(dateText = value, fieldErrors = it.fieldErrors - TransactionFormValidator.FIELD_DATE)
        }
    }

    fun onQuantityChange(value: String) {
        _uiState.update {
            it.copy(quantityText = value, fieldErrors = it.fieldErrors - TransactionFormValidator.FIELD_QUANTITY)
        }
        recomputeTotal()
    }

    fun onPriceChange(value: String) {
        _uiState.update {
            it.copy(priceText = value, fieldErrors = it.fieldErrors - TransactionFormValidator.FIELD_PRICE)
        }
        recomputeTotal()
    }

    fun onCommissionChange(value: String) {
        _uiState.update {
            it.copy(commissionText = value, fieldErrors = it.fieldErrors - TransactionFormValidator.FIELD_COMMISSION)
        }
        recomputeTotal()
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val form = state.toForm()
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, formError = null) }
            when (val result = addTransactionUseCase.execute(investmentId, form)) {
                is AddTransactionResult.Success -> saveEvents.send(result.investmentId)
                is AddTransactionResult.Error -> {
                    val errors = result.errors.associate { it.field to it.message }
                    _uiState.update { it.copy(isSubmitting = false, fieldErrors = errors) }
                }
                is AddTransactionResult.BusinessError -> {
                    _uiState.update { it.copy(isSubmitting = false, formError = result.message) }
                }
            }
        }
    }

    private fun recomputeTotal() {
        val total = TransactionCalculator.computeTotal(_uiState.value.toForm())
        _uiState.update { it.copy(total = total) }
    }

    private fun AddTransactionUiState.toForm(): TransactionForm =
        TransactionForm(
            type = type,
            dateText = dateText,
            quantityText = quantityText,
            priceText = priceText,
            commissionText = commissionText,
            currency = currency,
        )

    companion object {
        fun factory(investmentId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddTransactionViewModel(
                    investmentId = investmentId,
                    addTransactionUseCase = AddTransactionUseCase(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }
    }
}