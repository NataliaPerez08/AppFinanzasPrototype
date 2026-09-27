package com.appfinanzas.prototype.ui.screens.investments

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceErrorText
import com.appfinanzas.prototype.ui.components.FinanceOption
import com.appfinanzas.prototype.ui.components.FinanceOptionSelector
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.navigation.Routes

private val typeOptions = InvestmentType.entries.map { type ->
    val label = when (type) {
        InvestmentType.ACCION -> "Acción"
        InvestmentType.ETF_FONDO -> "ETF / Fondo"
        InvestmentType.FIBRA -> "FIBRA"
        InvestmentType.CETES -> "CETES"
        InvestmentType.SOFIPO -> "SOFIPO"
        InvestmentType.OTRO -> "Otro"
    }
    FinanceOption(value = type.name, label = label)
}

private val currencyOptions = Currency.entries.map { FinanceOption(value = it.name, label = it.code) }

@Composable
fun AddInvestmentScreen(onNavigate: (String) -> Unit) {
    val viewModel: AddInvestmentViewModel = viewModel(factory = AddInvestmentViewModel.Factory)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.savedEvents.collect { investmentId ->
            onNavigate(Routes.investmentDetail(investmentId))
        }
    }

    FinanceScreen(
        title = "Nueva inversión",
        subtitle = "Paso 1 de 3",
        selectedTab = Routes.INVESTMENTS,
        onNavigate = onNavigate,
    ) {
        FinanceOptionSelector(
            label = "Tipo de instrumento",
            options = typeOptions,
            selectedValue = state.type?.name,
            onSelect = viewModel::onTypeSelected,
        )
        FinanceOptionSelector(
            label = "Institución",
            options = state.institutions.map { FinanceOption(value = it.id.toString(), label = it.name) },
            selectedValue = state.institutionId?.toString(),
            onSelect = viewModel::onInstitutionSelected,
        )
        FinanceTextField(
            label = "Símbolo / nombre",
            value = state.name,
            onValueChange = viewModel::onNameChange,
            error = state.fieldErrors["name"],
        )
        FinanceTextField(
            label = "Símbolo",
            value = state.symbol,
            onValueChange = viewModel::onSymbolChange,
            error = state.fieldErrors["symbol"],
        )
        FinanceOptionSelector(
            label = "Moneda",
            options = currencyOptions,
            selectedValue = state.currency?.name,
            onSelect = viewModel::onCurrencySelected,
        )
        FinanceTextField(
            label = "Valor inicial",
            value = state.initialValueText,
            onValueChange = viewModel::onInitialValueChange,
            error = state.fieldErrors["initialValue"],
        )
        FinanceTextField(
            label = "Fecha",
            value = state.dateText,
            onValueChange = viewModel::onDateChange,
            error = state.fieldErrors["date"],
        )
        val formError = state.formError
        if (formError != null) {
            FinanceErrorText(formError)
        }
        Spacer(Modifier.height(8.dp))
        FinanceButton(
            text = "Guardar inversión",
            enabled = !state.isSubmitting,
            onClick = viewModel::submit,
        )
    }
}