package com.appfinanzas.prototype.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import com.appfinanzas.prototype.domain.usecase.AddInstitutionResult
import com.appfinanzas.prototype.domain.usecase.AddInstitutionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstitutionsViewModel(
    private val addInstitutionUseCase: AddInstitutionUseCase,
    private val repository: InvestmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstitutionsUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameText = value, formError = null) }
    }

    fun onKindChange(value: String) {
        _uiState.update { it.copy(kindText = value, formError = null) }
    }

    fun toggleAddForm() {
        _uiState.update { it.copy(showAddForm = !it.showAddForm, formError = null) }
    }

    fun submitAdd() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = addInstitutionUseCase.execute(state.nameText, state.kindText)) {
                is AddInstitutionResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, nameText = "", kindText = "", showAddForm = false)
                }
                is AddInstitutionResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, formError = result.message)
                }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = InstitutionsUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            try {
                repository.observeInstitutions().collect { institutions ->
                    _uiState.update { it.copy(isLoading = false, institutions = institutions) }
                }
            } catch (e: Exception) {
                _uiState.value = InstitutionsUiState(isLoading = false, error = e.message ?: "Error inesperado")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                InstitutionsViewModel(
                    addInstitutionUseCase = AddInstitutionUseCase(AppContainer.investmentRepository),
                    repository = AppContainer.investmentRepository,
                )
            }
        }
    }
}