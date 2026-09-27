package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.AddInvestmentResult
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
import com.appfinanzas.prototype.domain.validation.InvestmentForm
import com.appfinanzas.prototype.domain.validation.InvestmentFormValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInvestmentViewModel(
    private val addInvestmentUseCase: AddInvestmentUseCase,
    repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInvestmentUiState())
    val uiState = _uiState.asStateFlow()

    private val saveEvents = Channel<Long>(Channel.BUFFERED)
    val savedEvents: Flow<Long> = saveEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeInstitutions().collect { institutions ->
                _uiState.update { it.copy(institutions = institutions) }
            }
        }
    }

    fun onTypeSelected(value: String) {
        _uiState.update {
            it.copy(
                type = InvestmentType.valueOf(value),
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_TYPE,
            )
        }
    }

    fun onInstitutionSelected(value: String) {
        _uiState.update {
            it.copy(
                institutionId = value.toLongOrNull(),
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_INSTITUTION,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(name = value, fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_NAME)
        }
    }

    fun onSymbolChange(value: String) {
        _uiState.update {
            it.copy(symbol = value, fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_SYMBOL)
        }
    }

    fun onCurrencySelected(value: String) {
        _uiState.update {
            it.copy(
                currency = Currency.valueOf(value),
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_CURRENCY,
            )
        }
    }

    fun onInitialValueChange(value: String) {
        _uiState.update {
            it.copy(
                initialValueText = value,
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_INITIAL_VALUE,
            )
        }
    }

    fun onDateChange(value: String) {
        _uiState.update {
            it.copy(dateText = value, fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_DATE)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val form = InvestmentForm(
            type = state.type,
            institutionId = state.institutionId,
            name = state.name,
            symbol = state.symbol,
            currency = state.currency,
            initialValueText = state.initialValueText,
            dateText = state.dateText,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, formError = null) }
            when (val result = addInvestmentUseCase.execute(form)) {
                is AddInvestmentResult.Success -> saveEvents.send(result.investmentId)
                is AddInvestmentResult.Error -> {
                    val errors = result.errors.associate { it.field to it.message }
                    _uiState.update { it.copy(isSubmitting = false, fieldErrors = errors) }
                }
                is AddInvestmentResult.BusinessError -> {
                    _uiState.update { it.copy(isSubmitting = false, formError = result.message) }
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AddInvestmentViewModel(
                    addInvestmentUseCase = AddInvestmentUseCase(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }
    }
}