package com.appfinanzas.prototype.ui.screens.more

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceErrorState
import com.appfinanzas.prototype.ui.components.FinanceErrorText
import com.appfinanzas.prototype.ui.components.FinanceLoadingState
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.FinanceTextField
import com.appfinanzas.prototype.ui.components.InstitutionRow
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun InstitutionsScreen(onNavigate: (String) -> Unit) {
    val viewModel: InstitutionsViewModel = viewModel(factory = InstitutionsViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    FinanceScreen(
        title = "Instituciones",
        subtitle = "Administra tus instituciones",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        when {
            state.isLoading -> FinanceLoadingState()
            state.error != null -> FinanceErrorState(state.error!!, onRetry = viewModel::retry)
            else -> {
                state.institutions.forEach { institution ->
                    InstitutionRow(
                        name = institution.name,
                        detail = institution.kind,
                        status = "ACTIVA",
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(8.dp))
                FinanceButton(
                    text = if (state.showAddForm) "CERRAR FORMULARIO" else "+ Agregar institución",
                    onClick = viewModel::toggleAddForm,
                )
                if (state.showAddForm) {
                    Spacer(Modifier.height(8.dp))
                    FinanceTextField(
                        label = "Nombre",
                        value = state.nameText,
                        onValueChange = viewModel::onNameChange,
                    )
                    FinanceTextField(
                        label = "Tipo",
                        value = state.kindText,
                        onValueChange = viewModel::onKindChange,
                    )
                    val formError = state.formError
                    if (formError != null) {
                        FinanceErrorText(formError)
                    }
                    Spacer(Modifier.height(8.dp))
                    FinanceButton(
                        text = "Guardar institución",
                        enabled = !state.isSubmitting,
                        onClick = viewModel::submitAdd,
                    )
                }
            }
        }
    }
}