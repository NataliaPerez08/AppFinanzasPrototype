package com.appfinanzas.prototype.ui.security

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

@Composable
fun SetupPinScreen(
    mode: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    val isChange = mode == Routes.MODE_CHANGE
    val viewModel: SetupPinViewModel = viewModel(factory = SetupPinViewModel.factory(isChange))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    FinanceScreen(
        title = if (isChange) "Cambiar PIN" else "Crear PIN",
        subtitle = "Bloqueo de PULSO",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        SectionHeader(
            title = when (state.step) {
                PinSetupStep.CURRENT -> "Ingresa tu PIN actual"
                PinSetupStep.CREATE -> "Crea un PIN de 4 dígitos"
                PinSetupStep.CONFIRM -> "Confirma el PIN"
            },
        )
        Spacer(Modifier.height(12.dp))
        PinDots(filled = state.pinLength)
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            FinanceLabel(it)
        }
        Spacer(Modifier.height(16.dp))
        PinKeypad(
            onDigit = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
        )
        Spacer(Modifier.height(16.dp))
        FinanceButton(text = "Cancelar", onClick = onBack)
    }
}
