package com.appfinanzas.prototype.ui.screens.more

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceErrorText
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinanceOption
import com.appfinanzas.prototype.ui.components.FinanceOptionSelector
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.navigation.Routes
import com.appfinanzas.prototype.ui.theme.FinanzasColors

private val currencyOptions = Currency.entries.map { FinanceOption(value = it.name, label = it.code) }

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Configuración",
        subtitle = "Parámetros generales",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                FinanceOptionSelector(
                    label = "Moneda base",
                    options = currencyOptions,
                    selectedValue = state.baseCurrency.name,
                    onSelect = viewModel::onCurrencySelected,
                )
                FinanceTextField(
                    label = "Inflación estimada",
                    value = state.inflationText,
                    onValueChange = viewModel::onInflationChange,
                )
                FinanceTextField(
                    label = "ISR estimado",
                    value = state.isrText,
                    onValueChange = viewModel::onIsrChange,
                )
                val formError = state.formError
                if (formError != null) {
                    FinanceErrorText(formError)
                }
                if (state.saved) {
                    Text(
                        text = "GUARDADO",
                        style = MaterialTheme.typography.labelMedium,
                        color = FinanzasColors.Accent,
                    )
                }
                Spacer(Modifier.height(8.dp))
                FinanceButton(
                    text = "Guardar",
                    enabled = !state.isSubmitting,
                    onClick = viewModel::save,
                )
            }
        }
    }
}