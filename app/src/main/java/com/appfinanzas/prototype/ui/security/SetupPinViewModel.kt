package com.appfinanzas.prototype.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appfinanzas.prototype.di.AppContainer
import com.appfinanzas.prototype.security.AppLockManager
import com.appfinanzas.prototype.security.UnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PinSetupStep { CURRENT, CREATE, CONFIRM }

data class SetupPinUiState(
    val step: PinSetupStep,
    val pinLength: Int = 0,
    val error: String? = null,
    val done: Boolean = false,
)

class SetupPinViewModel(
    private val manager: AppLockManager,
    private val isChange: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SetupPinUiState(step = if (isChange) PinSetupStep.CURRENT else PinSetupStep.CREATE),
    )
    val uiState = _uiState.asStateFlow()

    private var pin = StringBuilder()
    private var currentPin = ""
    private var firstPin = ""

    fun onDigit(digit: Char) {
        if (_uiState.value.done || pin.length >= MAX_PIN) return
        pin.append(digit)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
        if (pin.length == MAX_PIN) handleEntry()
    }

    fun onBackspace() {
        if (pin.isEmpty()) return
        pin.deleteCharAt(pin.length - 1)
        _uiState.update { it.copy(pinLength = pin.length, error = null) }
    }

    private fun handleEntry() {
        val entered = pin.toString()
        pin = StringBuilder()
        when (_uiState.value.step) {
            PinSetupStep.CURRENT -> submitCurrent(entered)
            PinSetupStep.CREATE -> {
                firstPin = entered
                _uiState.update { it.copy(step = PinSetupStep.CONFIRM, pinLength = 0, error = null) }
            }
            PinSetupStep.CONFIRM -> submitConfirmation(entered)
        }
    }

    private fun submitCurrent(entered: String) {
        if (manager.verifyCurrentPin(entered)) {
            currentPin = entered
            _uiState.update { it.copy(step = PinSetupStep.CREATE, pinLength = 0, error = null) }
        } else {
            _uiState.update { it.copy(pinLength = 0, error = "PIN INCORRECTO") }
        }
    }

    private fun submitConfirmation(entered: String) {
        if (entered != firstPin) {
            firstPin = ""
            _uiState.update { it.copy(step = PinSetupStep.CREATE, pinLength = 0, error = "LOS PIN NO COINCIDEN") }
            return
        }
        val ok = if (isChange) {
            manager.changePin(currentPin, firstPin) is UnlockResult.Success
        } else {
            manager.enable(firstPin)
        }
        if (ok) {
            _uiState.update { it.copy(pinLength = 0, done = true, error = null) }
        } else {
            firstPin = ""
            _uiState.update { it.copy(step = PinSetupStep.CREATE, pinLength = 0, error = "NO SE PUDO GUARDAR") }
        }
    }

    companion object {
        private const val MAX_PIN = 4

        fun factory(isChange: Boolean) = viewModelFactory {
            initializer {
                SetupPinViewModel(AppContainer.appLockManager, isChange)
            }
        }
    }
}
