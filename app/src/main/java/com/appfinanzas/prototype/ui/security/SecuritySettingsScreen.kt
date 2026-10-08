package com.appfinanzas.prototype.ui.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appfinanzas.prototype.security.BiometricAuthenticator
import com.appfinanzas.prototype.security.LockPolicy
import com.appfinanzas.prototype.ui.components.FinanceButton
import com.appfinanzas.prototype.ui.components.FinanceLabel
import com.appfinanzas.prototype.ui.components.FinanceOption
import com.appfinanzas.prototype.ui.components.FinanceOptionSelector
import com.appfinanzas.prototype.ui.components.FinancePanel
import com.appfinanzas.prototype.ui.components.FinanceScreen
import com.appfinanzas.prototype.ui.components.SectionHeader
import com.appfinanzas.prototype.ui.navigation.Routes

private val onOffOptions = listOf(
    FinanceOption(value = "ON", label = "ON"),
    FinanceOption(value = "OFF", label = "OFF"),
)

private val autoLockOptions = LockPolicy.AUTO_LOCK_OPTIONS.map {
    FinanceOption(value = it.millis.toString(), label = it.label)
}

@Composable
fun SecuritySettingsScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val biometricAvailable = remember { BiometricAuthenticator.canAuthenticate(context) }
    val viewModel: SecuritySettingsViewModel =
        viewModel(factory = SecuritySettingsViewModel.factory(biometricAvailable))
    val state by viewModel.uiState.collectAsState()

    FinanceScreen(
        title = "Seguridad",
        subtitle = "Bloqueo local de PULSO",
        selectedTab = Routes.MORE,
        onNavigate = onNavigate,
    ) {
        FinanceOptionSelector(
            label = "Bloqueo de PULSO",
            options = onOffOptions,
            selectedValue = if (state.enabled) "ON" else "OFF",
            onSelect = { value ->
                when {
                    value == "ON" && !state.enabled -> onNavigate(Routes.securityPin(Routes.MODE_CREATE))
                    value == "OFF" && state.enabled -> viewModel.startDisable()
                }
            },
        )
        if (state.enabled) {
            if (state.biometricAvailable) {
                FinanceOptionSelector(
                    label = "Biometría",
                    options = onOffOptions,
                    selectedValue = if (state.biometricEnabled) "ON" else "OFF",
                    onSelect = { value -> viewModel.onBiometricToggled(value == "ON") },
                )
            } else {
                FinanceLabel("Biometría no disponible en este dispositivo")
            }
            FinanceOptionSelector(
                label = "Bloqueo automático",
                options = autoLockOptions,
                selectedValue = state.autoLockMillis.toString(),
                onSelect = { value -> value.toLongOrNull()?.let(viewModel::onAutoLockSelected) },
            )
            Spacer(Modifier.height(8.dp))
            FinanceButton(text = "Cambiar PIN") {
                onNavigate(Routes.securityPin(Routes.MODE_CHANGE))
            }
            Spacer(Modifier.height(8.dp))
            FinanceButton(text = "Olvidé mi PIN") {
                onNavigate(Routes.SECURITY_FORGOT)
            }
        }

        if (state.confirmingDisable) {
            Spacer(Modifier.height(8.dp))
            FinancePanel {
                SectionHeader(title = "Confirma tu PIN para desactivar")
                Spacer(Modifier.height(8.dp))
                PinDots(filled = state.pinLength)
                state.error?.let {
                    Spacer(Modifier.height(4.dp))
                    FinanceLabel(it)
                }
                Spacer(Modifier.height(8.dp))
                PinKeypad(
                    onDigit = viewModel::onDigit,
                    onBackspace = viewModel::onBackspace,
                )
                Spacer(Modifier.height(8.dp))
                FinanceButton(text = "Cancelar", onClick = viewModel::cancelDisable)
            }
        }
    }
}
