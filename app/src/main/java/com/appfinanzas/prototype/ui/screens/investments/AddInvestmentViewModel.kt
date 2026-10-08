package com.appfinanzas.prototype.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import com.appfinanzas.prototype.domain.projection.ProjectionStrategyResolver
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.AddInvestmentResult
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.UpdateInvestmentResult
import com.appfinanzas.prototype.domain.usecase.UpdateInvestmentUseCase
import com.appfinanzas.prototype.domain.validation.InvestmentEditForm
import com.appfinanzas.prototype.domain.validation.InvestmentForm
import com.appfinanzas.prototype.domain.validation.InvestmentFormValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddInvestmentViewModel(
    private val investmentId: Long?,
    private val addInvestmentUseCase: AddInvestmentUseCase,
    private val updateInvestmentUseCase: UpdateInvestmentUseCase,
    repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInvestmentUiState(isEditing = investmentId != null))
    val uiState = _uiState.asStateFlow()

    private val saveEvents = Channel<Long>(Channel.BUFFERED)
    val savedEvents: Flow<Long> = saveEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeInstitutions().collect { institutions ->
                _uiState.update { it.copy(institutions = institutions) }
            }
        }
        if (investmentId != null) {
            viewModelScope.launch {
                repository.observeInvestment(investmentId).first().let { investment ->
                    if (investment == null) {
                        _uiState.update { it.copy(formError = "No existe la inversión solicitada") }
                    } else {
                        _uiState.update {
                            it.copy(
                                type = investment.type,
                                institutionId = investment.institution.id,
                                name = investment.name,
                                symbol = investment.symbol,
                                currency = investment.currency,
                                projectionStrategy = investment.projectionStrategy,
                                projectionReturnText = investment.projectionReturn?.toString().orEmpty(),
                                projectionVolatilityText = investment.projectionVolatility?.toString().orEmpty(),
                                recommendedStrategyLabel = ProjectionStrategyResolver.resolve(investment.type).label,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onTypeSelected(value: String) {
        val type = InvestmentType.valueOf(value)
        _uiState.update {
            it.copy(
                type = type,
                recommendedStrategyLabel = ProjectionStrategyResolver.resolve(type).label,
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_TYPE,
            )
        }
    }

    fun onProjectionStrategySelected(value: String) {
        _uiState.update {
            it.copy(
                projectionStrategy = if (value == STRATEGY_AUTOMATIC) null else ProjectionStrategy.valueOf(value),
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_PROJECTION_RETURN,
            )
        }
    }

    fun onProjectionReturnChange(value: String) {
        _uiState.update {
            it.copy(
                projectionReturnText = value,
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_PROJECTION_RETURN,
            )
        }
    }

    fun onProjectionVolatilityChange(value: String) {
        _uiState.update {
            it.copy(
                projectionVolatilityText = value,
                fieldErrors = it.fieldErrors - InvestmentFormValidator.FIELD_PROJECTION_VOLATILITY,
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
        _uiState.update { it.copy(isSubmitting = true, formError = null) }
        if (investmentId == null) submitCreate(state) else submitUpdate(state)
    }

    private fun submitCreate(state: AddInvestmentUiState) {
        val form = InvestmentForm(
            type = state.type,
            institutionId = state.institutionId,
            name = state.name,
            symbol = state.symbol,
            currency = state.currency,
            initialValueText = state.initialValueText,
            dateText = state.dateText,
            projectionStrategy = state.projectionStrategy,
            projectionReturnText = state.projectionReturnText,
            projectionVolatilityText = state.projectionVolatilityText,
        )
        viewModelScope.launch {
            when (val result = addInvestmentUseCase.execute(form)) {
                is AddInvestmentResult.Success -> saveEvents.send(result.investmentId)
                is AddInvestmentResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, fieldErrors = result.errors.associate { e -> e.field to e.message }) }
                }
                is AddInvestmentResult.BusinessError -> {
                    _uiState.update { it.copy(isSubmitting = false, formError = result.message) }
                }
            }
        }
    }

    private fun submitUpdate(state: AddInvestmentUiState) {
        val form = InvestmentEditForm(
            type = state.type,
            institutionId = state.institutionId,
            name = state.name,
            symbol = state.symbol,
            currency = state.currency,
            projectionStrategy = state.projectionStrategy,
            projectionReturnText = state.projectionReturnText,
            projectionVolatilityText = state.projectionVolatilityText,
        )
        viewModelScope.launch {
            when (val result = updateInvestmentUseCase.execute(investmentId!!, form)) {
                is UpdateInvestmentResult.Success -> saveEvents.send(result.investmentId)
                is UpdateInvestmentResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, fieldErrors = result.errors.associate { e -> e.field to e.message }) }
                }
                is UpdateInvestmentResult.BusinessError -> {
                    _uiState.update { it.copy(isSubmitting = false, formError = result.message) }
                }
            }
        }
    }

    companion object {
        const val STRATEGY_AUTOMATIC = "AUTOMATIC"

        fun factory(investmentId: Long? = null): androidx.lifecycle.ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddInvestmentViewModel(
                    investmentId = investmentId,
                    addInvestmentUseCase = AddInvestmentUseCase(AppContainer.investmentRepository),
                    updateInvestmentUseCase = UpdateInvestmentUseCase(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }

        val Factory = factory()
    }
}
