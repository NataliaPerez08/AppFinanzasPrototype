package com.appfinanzas.prototype.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.security.AppLockManager
import com.appfinanzas.prototype.security.LockPolicy
import com.appfinanzas.prototype.security.UnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SecurityUiState(
    val enabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val autoLockMillis: Long = LockPolicy.DEFAULT_AUTO_LOCK_MILLIS,
    val confirmingDisable: Boolean = false,
    val pinLength: Int = 0,
    val error: String? = null,
)

class SecuritySettingsViewModel(
    private val manager: AppLockManager,
    biometricAvailable: Boolean = false,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SecurityUiState(
            enabled = manager.isEnabled(),
            biometricEnabled = manager.isBiometricEnabled(),
            biometricAvailable = biometricAvailable,
            autoLockMillis = manager.autoLockMillis(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    private var pin = StringBuilder()

    fun startDisable() {
        pin = StringBuilder()
        _uiState.update { it.copy(confirmingDisable = true, pinLength = 0, error = null) }
    }

    fun cancelDisable() {
        pin = StringBuilder()
        _uiState.update { it.copy(confirmingDisable = false, pinLength = 0, error = null) }
    }

    fun onDigit(digit: Char) {
        if (!_uiState.value.confirmingDisable || pin.length >= MAX_PIN) return
        pin.append(digit)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
        if (pin.length == MAX_PIN) confirmDisable()
    }

    fun onBackspace() {
        if (pin.isEmpty()) return
        pin.deleteCharAt(pin.length - 1)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
    }

    private fun confirmDisable() {
        val entered = pin.toString()
        pin = StringBuilder()
        when (manager.disable(entered)) {
            is UnlockResult.Success ->
                _uiState.update { it.copy(enabled = false, biometricEnabled = false, confirmingDisable = false, pinLength = 0) }
            is UnlockResult.Cooldown ->
                _uiState.update { it.copy(pinLength = 0, error = "ESPERA UN MOMENTO") }
            is UnlockResult.Incorrect ->
                _uiState.update { it.copy(pinLength = 0, error = "PIN INCORRECTO") }
        }
    }

    fun onBiometricToggled(enabled: Boolean) {
        if (!manager.isEnabled() || !_uiState.value.biometricAvailable) return
        manager.setBiometricEnabled(enabled)
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun onAutoLockSelected(millis: Long) {
        manager.setAutoLockMillis(millis)
        _uiState.update { it.copy(autoLockMillis = millis) }
    }

    companion object {
        private const val MAX_PIN = 4

        fun factory(biometricAvailable: Boolean) = viewModelFactory {
            initializer {
                SecuritySettingsViewModel(AppContainer.appLockManager, biometricAvailable)
            }
        }
    }
}
